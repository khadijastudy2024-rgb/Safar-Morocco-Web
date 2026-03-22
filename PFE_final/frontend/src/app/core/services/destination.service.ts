import { Injectable }  from '@angular/core';
import { HttpClient }  from '@angular/common/http';
import { Observable, of, throwError }  from 'rxjs';
import { shareReplay, map, switchMap, catchError }  from 'rxjs/operators';
import { Destination }  from '../models/destination.model';

@Injectable({
    providedIn: 'root'
})
export class DestinationService {
    private apiUrl = '/api/destinations';
    private destinationsCache$: Observable<Destination[]> | null = null;

    constructor(private http: HttpClient) { }

    getAllDestinations(forceRefresh = false): Observable<Destination[]> {
        console.log('🔍 getAllDestinations called, forceRefresh:', forceRefresh);
        console.log('🔍 Cache exists:', !!this.destinationsCache$);
        
        if (!this.destinationsCache$ || forceRefresh) {
            console.log('🔍 Making HTTP request to:', this.apiUrl);
            this.destinationsCache$ = this.http.get<Destination[]>(this.apiUrl).pipe(
                shareReplay(1),
                map(destinations => {
                    console.log('🔍 Destinations received:', destinations);
                    return destinations;
                }),
                catchError(err => {
                    console.error('🔍 Error loading destinations:', err);
                    this.destinationsCache$ = null;
                    return throwError(() => err);
                })
            );
        } else {
            console.log('🔍 Using cached destinations');
        }
        return this.destinationsCache$;
    }

    getDestinationById(id: number): Observable<Destination> {
        // Optimization: Check cache first to see if we have this destination
        // This makes navigation instant if data is already loaded
        if (this.destinationsCache$) {
            return this.destinationsCache$.pipe(
                map(dests => dests.find(d => d.id === id)),
                // If found, return it. If not (unlikely if list loaded), fetch from API
                switchMap(dest => {
                    if (dest) return of(dest);
                    return this.http.get<Destination>(`${this.apiUrl}/${id}`);
                })
            );
        }
        return this.http.get<Destination>(`${this.apiUrl}/${id}`);
    }

    searchDestinations(keyword: string): Observable<Destination[]> {
        // Backend doesn't support search, so we fetch all and filter client-side
        return this.getAllDestinations().pipe(
            map(destinations => destinations.filter(d =>
                d.nom.toLowerCase().includes(keyword.toLowerCase()) ||
                d.type.toLowerCase().includes(keyword.toLowerCase())
            ))
        );
    }

    createDestination(destination: Destination): Observable<Destination> {
        this.destinationsCache$ = null; // Invalidate cache
        return this.http.post<Destination>(this.apiUrl, destination);
    }

    updateDestination(id: number, destination: Destination): Observable<Destination> {
        this.destinationsCache$ = null; // Invalidate cache
        return this.http.put<Destination>(`${this.apiUrl}/${id}`, destination);
    }

    deleteDestination(id: number): Observable<any> {
        this.destinationsCache$ = null; // Invalidate cache
        return this.http.delete(`${this.apiUrl}/${id}`);
    }
}
