import { Component, Inject, OnInit, ChangeDetectorRef }  from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef }  from '@angular/material/dialog';
import { Offer }  from '../../core/models/offer.model';
import { ReservationService }  from '../../core/services/reservation.service';
import { AuthService }  from '../../core/services/auth.service';
import { ItineraryService } from '../../core/services/itinerary.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
    standalone: false,
    selector: 'app-reservation-dialog',
    templateUrl: './reservation-dialog.component.html'
})
export class ReservationDialogComponent implements OnInit {
    reservationData = {
        startDate: '',
        endDate: '',
        quantity: 1,
        itineraryId: null as number | null
    };

    myItineraries: any[] = [];
    loading = false;
    isSaving = false;
    errorMessage: string | null = null;

    constructor(
        public dialogRef: MatDialogRef<ReservationDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: { offer: Offer },
        private reservationService: ReservationService,
        private authService: AuthService,
        private itineraryService: ItineraryService,
        private translate: TranslateService,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit() {
        this.loading = true;
        this.itineraryService.getMyItineraries().subscribe({
            next: (data) => {
                this.myItineraries = data || [];
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: (err) => {
                console.error('Error fetching itineraries', err);
                this.loading = false;
                this.cdr.detectChanges();
            }
        });
    }

    onNoClick(): void {
        this.dialogRef.close();
    }

    onSubmit(): void {
        if (this.isSaving) return;

        this.isSaving = true;
        this.errorMessage = null;

        // Create reservation without itinerary requirement
        const reservationPayload = {
            offerId: this.data.offer.id,
            startDate: this.reservationData.startDate,
            endDate: this.reservationData.endDate,
            quantity: this.reservationData.quantity
        };

        // Frontend validation
        if (!this.reservationData.itineraryId) {
            this.errorMessage = this.translate.instant('DESTINATIONS.RESERVATION.ERROR_SELECT_ITINERARY');
            this.isSaving = false;
            return;
        }

        if (this.data.offer.type === 'HOTEL' && (!this.reservationData.startDate || !this.reservationData.endDate)) {
            this.errorMessage = this.translate.instant('DESTINATIONS.RESERVATION.ERROR_HOTEL_DATES');
            this.isSaving = false;
            return;
        }

        if (this.reservationData.startDate && this.reservationData.endDate) {
            const start = new Date(this.reservationData.startDate);
            const end = new Date(this.reservationData.endDate);
            if (start > end) {
                this.errorMessage = this.translate.instant('DESTINATIONS.RESERVATION.ERROR_DATE_ORDER');
                this.isSaving = false;
                return;
            }
        }

        const reservation: any = {
            userId: Number(this.authService.currentUserValue!.id),
            itineraryId: Number(this.reservationData.itineraryId),
            offer: { id: Number(this.data.offer.id) },
            quantity: Number(this.reservationData.quantity),
            status: 'PENDING'
        };

        if (this.reservationData.startDate) {
            reservation.startDate = this.reservationData.startDate;
        }
        if (this.reservationData.endDate) {
            reservation.endDate = this.reservationData.endDate;
        }
        this.reservationService.createReservation(reservation).subscribe({
            next: () => {
                this.isSaving = false;
                this.dialogRef.close({ success: true });
                this.cdr.detectChanges();
            },
            error: (err) => {
                console.error('Reservation error:', err);
                if (err.error && err.error.message) {
                    this.errorMessage = err.error.message;
                    if (err.error.details) {
                        if (typeof err.error.details === 'string') {
                            this.errorMessage += `: ${err.error.details}`;
                        } else if (typeof err.error.details === 'object' && Object.keys(err.error.details).length > 0) {
                            const firstKey = Object.keys(err.error.details)[0];
                            this.errorMessage += `: ${err.error.details[firstKey]}`;
                        }
                    }
                } else if (err.error && typeof err.error === 'string') {
                    this.errorMessage = err.error;
                } else {
                    this.errorMessage = this.translate.instant('DESTINATIONS.RESERVATION.ERROR_GENERIC');
                }
                this.isSaving = false;
                this.cdr.detectChanges();
            }
        });
    }
}
