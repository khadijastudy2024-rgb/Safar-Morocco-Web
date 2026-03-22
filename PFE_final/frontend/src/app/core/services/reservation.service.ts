import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OfferReservation } from '../models/reservation.model';

@Injectable({
    providedIn: 'root'
})
export class ReservationService {
    private apiUrl = '/api/reservations';

    constructor(private http: HttpClient) { }

    createReservation(reservation: OfferReservation): Observable<OfferReservation> {
        return this.http.post<OfferReservation>(this.apiUrl, reservation);
    }

    getReservationsByItinerary(itineraryId: number): Observable<OfferReservation[]> {
        return this.http.get<OfferReservation[]>(`${this.apiUrl}/itinerary/${itineraryId}`);
    }

    getAllReservations(): Observable<OfferReservation[]> {
        return this.http.get<OfferReservation[]>(this.apiUrl);
    }

    updateReservationStatus(id: number, status: string): Observable<OfferReservation> {
        return this.http.put<OfferReservation>(`${this.apiUrl}/${id}/status?status=${status}`, {});
    }

    getUserReservations(userId: number): Observable<OfferReservation[]> {
        return this.http.get<OfferReservation[]>(`${this.apiUrl}/user/${userId}`);
    }
}
