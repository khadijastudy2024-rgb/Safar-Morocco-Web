import { Offer } from './offer.model';

export enum ReservationStatus {
    PENDING = 'PENDING',
    APPROVED = 'APPROVED',
    REJECTED = 'REJECTED',
    CANCELLED = 'CANCELLED'
}

export interface OfferReservation {
    id?: number;
    userId: number;
    itineraryId: number;
    offerId?: number; // Backend could accept just id
    offer?: Offer;    // Backend returns this now
    startDate?: string;
    endDate?: string;
    quantity?: number;
    totalPrice?: number;
    status?: ReservationStatus;
}
