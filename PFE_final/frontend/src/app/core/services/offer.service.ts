import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Offer } from '../models/offer.model';

@Injectable({
    providedIn: 'root'
})
export class OfferService {
    private apiUrl = '/api/offers';

    constructor(private http: HttpClient) { }

    getOffersByDestination(destinationId: number): Observable<Offer[]> {
        return this.http.get<Offer[]>(`${this.apiUrl}/destination/${destinationId}`);
    }

    getAllOffers(): Observable<Offer[]> {
        return this.http.get<Offer[]>(this.apiUrl);
    }

    createOffer(offer: Offer): Observable<Offer> {
        return this.http.post<Offer>(this.apiUrl, offer);
    }

    updateOffer(id: number, offer: Offer): Observable<Offer> {
        return this.http.put<Offer>(`${this.apiUrl}/${id}`, offer);
    }

    deleteOffer(id: number): Observable<any> {
        return this.http.delete(`${this.apiUrl}/${id}`);
    }
}
