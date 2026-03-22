import { Component, OnInit, AfterViewInit, NgZone, ChangeDetectorRef }  from '@angular/core';
import { DomSanitizer, SafeResourceUrl }  from '@angular/platform-browser';
import { MatTabChangeEvent }  from '@angular/material/tabs';
import { ActivatedRoute, Router }  from '@angular/router';
import { ApiService }  from '../../core/services/api.service';
import { AuthService }  from '../../core/services/auth.service';
import { MatSnackBar }  from '@angular/material/snack-bar';
import { MatDialog }  from '@angular/material/dialog';
import { DestinationDialogComponent }  from '../dialog/destination-dialog.component';
import { DestinationService }  from '../../core/services/destination.service';
import { WeatherService }  from '../../services/weather.service'; // Import WeatherService
import { OfferService }  from '../../core/services/offer.service';
import { ReservationService }  from '../../core/services/reservation.service';
import { Offer }  from '../../core/models/offer.model';
import { ReservationDialogComponent }  from '../dialog/reservation-dialog.component';
import * as L from 'leaflet';

@Component({
    standalone: false,
    selector: 'app-destination-detail',
    templateUrl: './detail.component.html',
    styleUrls: ['./detail.component.css']
})
export class DestinationDetailComponent implements OnInit, AfterViewInit {
    destination: any;
    reviews: any[] = [];
    loading = true;
    newReview = { note: 5, commentaire: '' };
    map: any;
    lightboxImage: string | null = null;
    currentImageIndex: number = -1;
    showReviewForm = false;

    // Weather Data
    weather: any;
    forecast: any;
    weatherLoading = false;

    // Offers
    offers: Offer[] = [];
    selectedOfferCategory: string = 'ALL';

    get filteredOffers(): Offer[] {
        if (this.selectedOfferCategory === 'ALL') return this.offers;
        return this.offers.filter(o => o.type === this.selectedOfferCategory);
    }

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private apiService: ApiService,
        private destinationService: DestinationService,
        private weatherService: WeatherService, // Inject WeatherService
        private offerService: OfferService,
        private reservationService: ReservationService,
        public authService: AuthService,
        private snackBar: MatSnackBar,
        private dialog: MatDialog,
        private sanitizer: DomSanitizer,
        private ngZone: NgZone,
        private cdr: ChangeDetectorRef
    ) {
        // INSTANT LOAD LOGIC: Check if data was passed via state
        this.ngZone.run(() => {
            const nav = this.router.getCurrentNavigation();
            if (nav?.extras?.state?.['data']) {
                this.destination = nav.extras.state['data'];
                this.loading = false; // Show immediately
                this.loadWeather(); // Load weather if data is already available
            } else if (history.state.data) {
                this.destination = history.state.data;
                this.loading = false;
                this.loadWeather();
            }
        });
    }

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.ngZone.run(() => {
                const id = params.get('id');
                if (id) {
                    // Always fetch fresh data to get full details (reviews, etc.)
                    // If we already have destination (from state), we don't show loading spinner
                    this.loadDestination(+id, !this.destination);
                    this.loadReviews(+id);
                    this.loadOffers(+id);
                } else {
                    console.warn('âš ï¸ DestinationDetail: No ID provided in route');
                    this.loading = false;
                    this.cdr.detectChanges();
                }
            });
        });
    }

    ngAfterViewInit(): void {
        // Map init handled after data load
    }

    loadDestination(id: number, showLoading: boolean) {
        if (showLoading) this.loading = true;

        this.apiService.getDestination(id).subscribe({
            next: (data) => {
                this.ngZone.run(() => {
                    this.destination = data;
                    this.loading = false;
                    this.loadWeather(); // Load weather after fetching destination
                    this.cdr.detectChanges();
                    setTimeout(() => this.initMap(), 100);
                });
            },
            error: (err: any) => {
                this.ngZone.run(() => {
                    console.error('Error loading destination:', err);
                    this.loading = false;
                    if (!this.destination) {
                        this.snackBar.open('Failed to load destination details.', 'Close', { duration: 5000 });
                    }
                    this.cdr.detectChanges();
                });
            }
        });
    }

    loadWeather() {
        if (this.destination && this.destination.id) {
            this.weatherLoading = true;
            this.weatherService.getCurrentWeather(this.destination.id).subscribe({
                next: (data) => {
                    this.ngZone.run(() => {
                        this.weather = data;
                        this.weatherLoading = false;
                        this.cdr.detectChanges();
                    });
                },
                error: (err: any) => {
                    this.ngZone.run(() => {
                        console.error('Error loading weather:', err);
                        this.weatherLoading = false;
                        this.cdr.detectChanges();
                    });
                }
            });

            this.weatherService.getForecast(this.destination.id).subscribe({
                next: (data) => {
                    this.ngZone.run(() => {
                        // Backend returns list of MeteoDTO
                        this.forecast = data.slice(0, 7);
                        this.cdr.detectChanges();
                    });
                },
                error: (err: any) => {
                    this.ngZone.run(() => {
                        console.error('Error loading forecast:', err);
                        this.cdr.detectChanges();
                    });
                }
            });
        }
    }

    loadReviews(id: number) {
        this.apiService.getReviews(id).subscribe({
            next: (data) => {
                this.ngZone.run(() => {
                    this.reviews = data || [];
                    this.cdr.detectChanges();
                });
            },
            error: (err: any) => {
                console.error('Error loading reviews:', err);
                this.cdr.detectChanges();
            }
        });
    }

    loadOffers(id: number) {
        this.offerService.getOffersByDestination(id).subscribe({
            next: (data) => {
                this.ngZone.run(() => {
                    this.offers = data || [];
                    this.cdr.detectChanges();
                });
            },
            error: (err: any) => {
                console.error('Error loading offers:', err);
                this.cdr.detectChanges();
            }
        });
    }

    initMap() {
        if (!this.destination || !this.destination.latitude || !this.destination.longitude) return;

        // Ensure map container exists
        const mapContainer = document.getElementById('map');
        if (!mapContainer) return;

        if (this.map) {
            this.map.remove();
        }

        try {
            this.map = L.map('map').setView([this.destination.latitude, this.destination.longitude], 13);

            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19,
                attribution: 'Â© OpenStreetMap contributors'
            }).addTo(this.map);

            // Use Local Assets for markers to avoid CDN issues/delay
            const defaultIcon = L.icon({
                iconUrl: 'assets/marker-icon.png',
                shadowUrl: 'assets/marker-shadow.png',
                iconSize: [25, 41],
                iconAnchor: [12, 41],
                popupAnchor: [1, -34],
                shadowSize: [41, 41]
            });

            L.marker([this.destination.latitude, this.destination.longitude], { icon: defaultIcon })
                .addTo(this.map)
                .bindPopup(this.destination.name)
                .openPopup();

            // Force resize in case of tab switches
            setTimeout(() => { this.map.invalidateSize(); }, 200);
        } catch (e) {
            console.error("Map initialization error", e);
        }
    }

    submitReview() {
        if (!this.newReview.commentaire) return;

        const reviewPayload = {
            ...this.newReview,
            destination: { id: this.destination.id }
        };
        this.apiService.addReview(reviewPayload).subscribe({
            next: (review) => {
                this.reviews.push(review);
                this.newReview = { note: 5, commentaire: '' };
                this.showReviewForm = false;
                this.snackBar.open('Review added!', 'Close', { duration: 3000 });
                this.loadReviews(this.destination.id);
            },
            error: (err: any) => {
                console.error('Error adding review:', err);
                this.snackBar.open('Failed to add review', 'Close', { duration: 3000 });
            }
        });
    }

    editDestination() {
        if (!this.authService.isAdmin) return;
        const dialogRef = this.dialog.open(DestinationDialogComponent, {
            width: '600px',
            data: this.destination
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.destination = result;
                this.snackBar.open('Destination updated successfully', 'Close', { duration: 3000 });
                this.initMap();
                this.loadWeather(); // Reload weather in case coords changed
            }
        });
    }

    deleteDestination() {
        if (!this.authService.isAdmin) return;
        if (confirm('Are you sure you want to delete this destination? This cannot be undone.')) {
            this.destinationService.deleteDestination(this.destination.id).subscribe({
                next: () => {
                    this.snackBar.open('Destination deleted', 'Close', { duration: 3000 });
                    this.router.navigate(['/destinations']);
                },
                error: () => this.snackBar.open('Failed to delete', 'Close', { duration: 3000 })
            });
        }
    }

    onTabChange(event: MatTabChangeEvent) {
        // Instead of checking localized textLabel, check if the map container is in the DOM
        setTimeout(() => {
            const mapContainer = document.getElementById('map');
            if (mapContainer) {
                if (this.map) {
                    this.map.invalidateSize();
                } else {
                    this.initMap();
                }
            }
        }, 100);
    }

    // Lightbox Logic
    openLightbox(index: number) {
        this.currentImageIndex = index;
        if (this.destination.medias && this.destination.medias[index]) {
            this.lightboxImage = this.destination.medias[index].url;
        }
    }

    closeLightbox() {
        this.lightboxImage = null;
        this.currentImageIndex = -1;
    }

    nextImage(event: Event) {
        event.stopPropagation();
        if (this.destination.medias && this.currentImageIndex < this.destination.medias.length - 1) {
            this.currentImageIndex++;
            this.lightboxImage = this.destination.medias[this.currentImageIndex].url;
        }
    }

    prevImage(event: Event) {
        event.stopPropagation();
        if (this.destination.medias && this.currentImageIndex > 0) {
            this.currentImageIndex--;
            this.lightboxImage = this.destination.medias[this.currentImageIndex].url;
        }
    }

    // Media Upload Logic
    onFileSelected(event: any) {
        const file: File = event.target.files[0];
        if (file) {
            this.uploadImage(file);
        }
    }

    private uploadImage(file: File) {
        this.snackBar.open('Uploading image...', 'Close', { duration: 2000 });
        this.apiService.uploadFile(this.destination.id, file).subscribe({
            next: (response) => {
                this.snackBar.open('Photo added to gallery!', 'Close', { duration: 3000 });
                // Reload destination to see new image
                this.loadDestination(this.destination.id, false);
            },
            error: (err) => {
                console.error('Upload failed:', err);
                this.snackBar.open('Failed to upload image. Please try again.', 'Close', { duration: 5000 });
            }
        });
    }

    getSafeVideoUrl(url: string | undefined): SafeResourceUrl | null {
        if (!url) return null;
        let embedUrl = url;

        // Simple YouTube converter
        if (url.includes('youtube.com/watch?v=')) {
            embedUrl = url.replace('watch?v=', 'embed/');
        } else if (url.includes('youtu.be/')) {
            embedUrl = url.replace('youtu.be/', 'youtube.com/embed/');
        }

        return this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl);
    }

    /**
     * Crée un nouvel itinéraire avec cette destination
     * Appelé quand l'utilisateur clique sur le bouton "Planifier mon voyage"
     * Redirige vers la page de création avec la destination pré-sélectionnée
     */
    planTrip() {
        console.log('🔍 planTrip() called');
        console.log('🔍 User logged in:', this.authService.isLoggedIn);
        console.log('🔍 Destination:', this.destination);
        console.log('🔍 Destination ID:', this.destination?.id);

        // Vérifier que l'utilisateur est connecté
        if (!this.authService.isLoggedIn) {
            console.log('🔍 User not logged in, redirecting to login');
            this.snackBar.open('⚠️ Veuillez vous connecter pour planifier un voyage', 'Fermer', { duration: 5000 });
            this.router.navigate(['/auth/login']);
            return;
        }

        // Vérifier que la destination existe
        if (!this.destination || !this.destination.id) {
            console.log('🔍 No destination found');
            this.snackBar.open('Erreur: Destination non trouvée', 'Fermer', { duration: 3000 });
            return;
        }

        console.log('🔍 Navigating to create itinerary with destination ID:', this.destination.id);

        // Rediriger vers la page de création avec la destination en paramètre
        this.router.navigate(['/itineraries/create'], {
            queryParams: {
                destinationId: this.destination.id
            }
        }).then(nav => {
            console.log('🔍 Navigation successful:', nav);
        }).catch(err => {
            console.error('🔍 Navigation error:', err);
            this.snackBar.open('Erreur lors de la navigation', 'Fermer', { duration: 3000 });
        });
    }

    openReservationMenu(offer: Offer) {
        if (!this.authService.isLoggedIn) {
            this.snackBar.open('Please log in to make a reservation.', 'Close', { duration: 5000 });
            this.router.navigate(['/auth/login']);
            return;
        }

        const dialogRef = this.dialog.open(ReservationDialogComponent, {
            width: '500px',
            data: { offer: offer },
            disableClose: true // Prevent closing while saving
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result && result.success && result.itineraryId) {
                this.snackBar.open('Reservation submitted! Waiting for admin approval.', 'Close', { duration: 5000 });
                // Redirect user to the itinerary page where they can see their reservations and generate invoices
                this.router.navigate(['/itineraires/detail', result.itineraryId]);
            }
        });
    }
}
