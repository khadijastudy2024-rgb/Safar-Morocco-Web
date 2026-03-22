import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { ReservationService } from '../../core/services/reservation.service';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
    standalone: false,
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
    user: any = {};
    isEditing = false;
    selectedFile: File | null = null;
    imagePreview: string | null = null;
    activities: any[] = [];
    myItineraries: any[] = []; // History of booked itineraries
    createdItineraries: any[] = []; // Itineraries user physically created
    itinerariesLoading = false;
    loading = true;
    uploadingPhoto = false;

    // Tabs & Reservations
    activeTab: string = 'overview';
    eventReservations: any[] = [];
    offerReservations: any[] = [];
    reservationsLoading = false;
    selectedSection: string = 'reservations'; // For stat cards navigation

    totalReservationsCount: number = 0;
    upcomingEventsCount: number = 0;

    get hotels() { return this.offerReservations.filter(r => r.offer?.type === 'HOTEL'); }
    get restaurants() { return this.offerReservations.filter(r => r.offer?.type === 'RESTO'); } // Use whatever type is defined in backend
    get myActivities() { return this.offerReservations.filter(r => r.offer?.type === 'ACTIVITE' || r.offer?.type === 'ACTIVITY'); } // Accommodate FR/EN

    get upcomingEvents() {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        return this.eventReservations.filter(r =>
            r.status === 'CONFIRMED' &&
            new Date(r.evenement?.dateDebut || r.evenement?.date) >= today
        );
    }

    constructor(
        private apiService: ApiService,
        public authService: AuthService,
        private reservationService: ReservationService,
        private snackBar: MatSnackBar,
        private cdr: ChangeDetectorRef,
        private http: HttpClient,
        private router: Router
    ) { }

    ngOnInit(): void {
        this.loadProfile();
        this.loadActivities();
        this.loadMyItineraries();
    }

    loadProfile() {
        this.loading = true;
        this.apiService.getCurrentUser().subscribe({
            next: (data) => {
                this.user = data;
                this.loading = false;
                // Initialize default fields if missing (using backend names)
                if (!this.user.description) this.user.description = '';
                if (!this.user.telephone) this.user.telephone = '';
                if (!this.user.langue) this.user.langue = 'en';
                this.loadMyReservations();
                this.loadCreatedItineraries();
                this.cdr.detectChanges(); // Force update view
            },
            error: (err: any) => {
                console.error('Failed to load profile', err);
                this.loading = false;
            }
        });
    }

    loadActivities() {
        this.apiService.getActivityLogs().subscribe({
            next: (data) => {
                this.activities = data;
            },
            error: (err) => {
                console.error('❌ Failed to load activities', err);
            }
        });
    }

    loadMyItineraries() {
        this.itinerariesLoading = true;
        this.apiService.getMyItineraries().subscribe({
            next: (data) => {
                this.myItineraries = data;
                this.itinerariesLoading = false;
            },
            error: (err) => {
                console.error('❌ Failed to load itineraries', err);
                this.itinerariesLoading = false;
            }
        });
    }

    loadCreatedItineraries() {
        if (!this.user?.id) return;
        this.apiService.getUserItineraries(this.user.id).subscribe({
            next: (data) => {
                this.createdItineraries = data;
                this.cdr.detectChanges();
            },
            error: (err) => console.error('❌ Failed to load created itineraries', err)
        });
    }

    loadMyReservations() {
        this.reservationsLoading = true;
        if (!this.user?.id) return;

        // Use forkJoin to load both and calculate counts once finished
        forkJoin({
            events: this.apiService.getMyEventReservations().pipe(catchError(e => { console.error(e); return of([]); })),
            offers: this.reservationService.getUserReservations(this.user.id).pipe(catchError(e => { console.error(e); return of([]); }))
        }).subscribe({
            next: (data) => {
                this.eventReservations = data.events;
                this.offerReservations = data.offers;
                this.calculateStats();
                this.reservationsLoading = false;
                this.cdr.detectChanges();
            },
            error: (err) => {
                console.error('❌ Failed to load reservations:', err);
                this.reservationsLoading = false;
            }
        });
    }

    calculateStats() {
        // Total Reservations
        const activeEventRes = this.eventReservations.filter(r => r.status !== 'CANCELLED').length;
        const activeOfferRes = this.offerReservations.filter(r => r.status !== 'CANCELLED' && r.status !== 'REJECTED').length;
        this.totalReservationsCount = activeEventRes + activeOfferRes;

        // Upcoming Events
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        this.upcomingEventsCount = this.eventReservations.filter(r =>
            r.status === 'CONFIRMED' &&
            new Date(r.evenement?.dateDebut || r.evenement?.date) >= today
        ).length;
    }

    cancelEvent(reservation: any) {
        if (confirm('Etes-vous sûr de vouloir annuler cette réservation ?')) {
            this.apiService.cancelEventReservation(reservation.id).subscribe({
                next: () => {
                    reservation.status = 'CANCELLED';
                    this.snackBar.open('Réservation annulée.', 'Fermer', { duration: 3000 });
                    this.cdr.detectChanges();
                },
                error: (err) => this.snackBar.open('Erreur lors de l\'annulation.', 'Fermer', { duration: 3000 })
            });
        }
    }

    cancelOffer(reservation: any) {
        if (confirm('Etes-vous sûr de vouloir annuler cette réservation ?')) {
            this.http.put(`/api/reservations/${reservation.id}/status?status=CANCELLED`, {}).subscribe({
                next: () => {
                    reservation.status = 'CANCELLED';
                    this.snackBar.open('Réservation annulée.', 'Fermer', { duration: 3000 });
                    this.cdr.detectChanges();
                },
                error: (err) => {
                    console.error('Failed to cancel offer reservation', err);
                    this.snackBar.open('Erreur lors de l\'annulation.', 'Fermer', { duration: 3000 });
                }
            });
        }
    }

    viewItinerary(id: number) {
        this.router.navigate(['/itineraries/detail', id]);
    }

    editProfile() {
        this.isEditing = true;
    }

    cancelEdit() {
        this.isEditing = false;
        this.loadProfile();
    }

    onFileSelected(event: any) {
        const file = event.target.files[0];
        if (file) {
            this.uploadNewPhoto(file);
        }
    }

    uploadNewPhoto(file: File) {
        this.uploadingPhoto = true;
        // Immediate preview
        const reader = new FileReader();
        reader.onload = (e: any) => this.imagePreview = e.target.result;
        reader.readAsDataURL(file);

        this.apiService.uploadProfilePhoto(file).subscribe({
            next: (res) => {
                this.user.photoUrl = res.photoUrl;
                this.uploadingPhoto = false;
                this.snackBar.open('Photo updated!', 'Close', { duration: 2000 });
                // If not editing, also update local storage user
                if (!this.isEditing) {
                    const localUser = JSON.parse(localStorage.getItem('user') || '{}');
                    localUser.photoUrl = res.photoUrl;
                    localStorage.setItem('user', JSON.stringify(localUser));
                    this.authService.updateUser(localUser);
                }
            },
            error: (err) => {
                console.error('❌ Photo upload failed', err);
                this.uploadingPhoto = false;
                this.snackBar.open('Failed to upload photo', 'Close', { duration: 3000 });
            }
        });
    }

    saveProfile() {
        this.updateUserData();
    }

    updateUserData() {
        // Create a clean DTO with only allowed fields matching UtilisateurDTO
        const updateRequest = {
            nom: this.user.nom,
            telephone: this.user.telephone,
            langue: this.user.langue,
            description: this.user.description,
            photoUrl: this.user.photoUrl
        };


        this.apiService.updateProfile(updateRequest).subscribe({
            next: (updatedUser) => {
                this.user = updatedUser;
                this.authService.updateUser(updatedUser); // Update local storage too
                this.isEditing = false;
                this.snackBar.open('Profile updated successfully!', 'Close', { duration: 3000 });
            },
            error: (err: any) => {
                this.snackBar.open('Failed to update profile', 'Close', { duration: 3000 });
                console.error('❌ Profile update error:', err);
                if (err.error && err.error.message) {
                    console.error('Error details:', err.error.message);
                }
            }
        });
    }
}
