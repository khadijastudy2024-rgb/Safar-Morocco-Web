import { Component, OnInit, OnDestroy, NgZone, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
    standalone: false,
    selector: 'app-event-detail',
    templateUrl: './event-detail.component.html',
    styleUrls: ['./event-detail.component.css']
})
export class EventDetailComponent implements OnInit, OnDestroy {
    event: any;
    loading = true;
    booking = false;
    alreadyReserved = false;

    // Countdown
    countdown = { days: 0, hours: 0, minutes: 0, seconds: 0 };
    private countdownInterval?: any;
    eventStarted = false;
    eventPast = false;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private apiService: ApiService,
        private authService: AuthService,
        private snackBar: MatSnackBar,
        private ngZone: NgZone,
        private cdr: ChangeDetectorRef
    ) {
        this.ngZone.run(() => {
            const nav = this.router.getCurrentNavigation();
            if (nav?.extras?.state?.['data']) {
                this.event = { ...nav.extras.state['data'] };
                this.event.dateDebut = this.parseDate(this.event.dateDebut);
                this.event.dateFin = this.parseDate(this.event.dateFin);
                this.loading = false;
            } else if (history.state?.data) {
                this.event = { ...history.state.data };
                this.event.dateDebut = this.parseDate(this.event.dateDebut);
                this.event.dateFin = this.parseDate(this.event.dateFin);
                this.loading = false;
            }
        });
    }

    ngOnInit(): void {
        this.route.paramMap.subscribe(params => {
            this.ngZone.run(() => {
                const id = params.get('id');
                if (id) {
                    const needsLoad = !this.event || this.event.id !== +id;
                    if (!needsLoad) {
                        this.startCountdown();
                        this.checkReservationStatus();
                    }
                    this.loadEvent(+id, needsLoad);
                } else {
                    this.loading = false;
                    this.cdr.detectChanges();
                }
            });
        });
    }

    ngOnDestroy(): void {
        if (this.countdownInterval) {
            clearInterval(this.countdownInterval);
        }
    }

    loadEvent(id: number, showLoading: boolean): void {
        if (showLoading) {
            this.loading = true;
            this.cdr.detectChanges();
        }

        this.apiService.getEventById(id).subscribe({
            next: (data) => {
                this.ngZone.run(() => {
                    this.event = { ...data };
                    this.event.dateDebut = this.parseDate(data.dateDebut);
                    this.event.dateFin = this.parseDate(data.dateFin);
                    this.loading = false;
                    this.checkReservationStatus();
                    this.startCountdown();
                    this.cdr.detectChanges();
                });
            },
            error: (err: any) => {
                this.ngZone.run(() => {
                    console.error('Error loading event:', err);
                    this.loading = false;
                    this.cdr.detectChanges();
                });
            }
        });
    }

    checkReservationStatus(): void {
        if (this.authService.isLoggedIn && this.event) {
            this.apiService.getMyEventReservations().subscribe({
                next: (reservations) => {
                    const existing = reservations.find((r: any) => r.evenement.id === this.event.id && r.status === 'CONFIRMED');
                    this.alreadyReserved = !!existing;
                    this.cdr.detectChanges();
                },
                error: (err: any) => console.error('Failed to load reservations:', err)
            });
        }
    }

    bookSpot(): void {
        if (!this.authService.isLoggedIn) {
            this.router.navigate(['/login']);
            return;
        }

        if (this.alreadyReserved) return;

        this.booking = true;
        this.apiService.bookEvent(this.event.id).subscribe({
            next: () => {
                this.booking = false;
                this.alreadyReserved = true;
                this.snackBar.open('Réservation réussie!', 'Fermer', { duration: 3000 });
                this.cdr.detectChanges();
            },
            error: (err: any) => {
                this.booking = false;
                const msg = err.error?.message || 'Erreur lors de la réservation.';
                this.snackBar.open(msg, 'Fermer', { duration: 3000 });
                this.cdr.detectChanges();
            }
        });
    }

    // --- Enrichment helpers ---

    private parseDate(dateStr: any): Date {
        if (!dateStr) return new Date();
        if (dateStr instanceof Date) return dateStr;
        // Handle array format e.g., [2026, 3, 30, 22, 42, 27]
        if (Array.isArray(dateStr)) {
            const [y, m, d, h = 0, min = 0, s = 0] = dateStr;
            return new Date(y, m - 1, d, h, min, s);
        }
        // Handle string format e.g., "2026-04-29T22:42:26"
        // In Safari, missing timezone might cause issues, so we can replace T with space or parse safe.
        // Actually native Date parser handles ISO 8601 fine in modern browsers.
        const parsed = new Date(dateStr);
        if (isNaN(parsed.getTime()) && typeof dateStr === 'string') {
            // Fallback for Safari if needed: "2026-04-29 22:42:26"
            return new Date(dateStr.replace('T', ' ').replace(/\.\d+/, ''));
        }
        return parsed;
    }

    /** Returns event duration in days */
    getEventDuration(): number {
        if (!this.event) return 0;
        const start = this.parseDate(this.event.dateDebut);
        const end = this.parseDate(this.event.dateFin);
        const diff = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
        return diff > 0 ? diff : 1;
    }

    /** Returns an array of Date objects for each day of the event */
    getEventDays(): Date[] {
        if (!this.event) return [];
        const days: Date[] = [];
        const start = this.parseDate(this.event.dateDebut);
        const end = this.parseDate(this.event.dateFin);
        const current = new Date(start);
        while (current <= end) {
            days.push(new Date(current));
            current.setDate(current.getDate() + 1);
        }
        return days.slice(0, 10); // Limit to 10 days max for display
    }

    /** Returns tags based on eventType */
    getEventTags(): string[] {
        const baseTags = ['#Maroc', '#Culture', '#Voyage'];
        const typeMap: { [key: string]: string[] } = {
            'FESTIVAL': ['#Festival', '#Fête', '#Animation'],
            'CULTURAL': ['#Culturel', '#Patrimoine', '#Art'],
            'MUSIC': ['#Musique', '#Concert', '#Live'],
            'TRADITIONAL': ['#Tradition', '#Folklore', '#Artisanat'],
        };
        const type = (this.event?.eventType || '').toUpperCase();
        return [...(typeMap[type] || ['#Événement']), ...baseTags];
    }

    /** Event type display label */
    getEventTypeLabel(): string {
        const map: { [k: string]: string } = {
            'FESTIVAL': 'Festival',
            'CULTURAL': 'Culturel',
            'MUSIC': 'Musique',
            'TRADITIONAL': 'Traditionnel',
        };
        return map[(this.event?.eventType || '').toUpperCase()] || this.event?.eventType || 'Événement';
    }

    /** Event type icon class */
    getEventTypeIcon(): string {
        const map: { [k: string]: string } = {
            'FESTIVAL': 'bi-stars',
            'CULTURAL': 'bi-building-fill',
            'MUSIC': 'bi-music-note-beamed',
            'TRADITIONAL': 'bi-patch-star-fill',
        };
        return map[(this.event?.eventType || '').toUpperCase()] || 'bi-calendar-event';
    }

    /** Starts a real-time countdown to dateDebut */
    startCountdown(): void {
        if (this.countdownInterval) clearInterval(this.countdownInterval);
        const update = () => {
            const now = new Date().getTime();
            const start = this.parseDate(this.event.dateDebut).getTime();
            const end = this.parseDate(this.event.dateFin).getTime();
            const diff = start - now;

            if (isNaN(diff)) {
                this.eventStarted = false;
                this.eventPast = false;
                clearInterval(this.countdownInterval);
            } else if (now > end) {
                // Event is fully over
                this.eventPast = true;
                this.eventStarted = false;
                this.countdown = { days: 0, hours: 0, minutes: 0, seconds: 0 };
                clearInterval(this.countdownInterval);
            } else if (diff <= 0) {
                // Event has started but not yet finished
                this.eventStarted = true;
                this.eventPast = false;
                this.countdown = { days: 0, hours: 0, minutes: 0, seconds: 0 };
                clearInterval(this.countdownInterval);
            } else {
                this.eventStarted = false;
                this.eventPast = false;
                this.countdown = {
                    days: Math.floor(diff / (1000 * 60 * 60 * 24)),
                    hours: Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)),
                    minutes: Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60)),
                    seconds: Math.floor((diff % (1000 * 60)) / 1000),
                };
            }
            this.cdr.detectChanges();
        };
        update();
        this.countdownInterval = setInterval(update, 1000);
    }
}
