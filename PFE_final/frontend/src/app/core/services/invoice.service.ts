import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Invoice } from '../models/invoice.model';
import { AuthService } from './auth.service';

@Injectable({
    providedIn: 'root'
})
export class InvoiceService {
    private apiUrl = '/api/invoices';

    constructor(
        private http: HttpClient,
        private authService: AuthService
    ) { }

    generateInvoice(itineraryId: number, lang: string = 'fr'): Observable<Invoice> {
        const userId = this.authService.currentUserValue?.id;
        if (!userId) {
            throw new Error('User not authenticated');
        }
        return this.http.post<Invoice>(`${this.apiUrl}/generate/${itineraryId}/${userId}?lang=${lang}`, {});
    }

    getInvoicesByItinerary(itineraryId: number): Observable<Invoice[]> {
        return this.http.get<Invoice[]>(`${this.apiUrl}/itinerary/${itineraryId}`);
    }

    generateReservationInvoice(reservationId: number, lang: string = 'fr'): Observable<Invoice> {
        return this.http.post<Invoice>(`${this.apiUrl}/generate/reservation/${reservationId}?lang=${lang}`, {});
    }
}
