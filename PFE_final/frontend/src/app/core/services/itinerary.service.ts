import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { 
    ItineraireDetailDTO, 
    ItineraireResponseDTO, 
    ItineraireRequestDTO, 
    UpdateItineraireDTO, 
    RechercheItineraireDTO 
} from '../models/itinerary.model';
import { AuthService } from './auth.service';

@Injectable({
    providedIn: 'root'
})
export class ItineraryService {
    private readonly apiUrl = '/api/itineraires';

    constructor(
        private http: HttpClient,
        private authService: AuthService
    ) {}

    // CRUD Operations
    creerItineraire(request: ItineraireRequestDTO, utilisateurId: number): Observable<ItineraireResponseDTO> {
        console.log('🔍 Creating itinerary with request:', request);
        console.log('🔍 User ID:', utilisateurId);
        console.log('🔍 API URL:', `${this.apiUrl}/utilisateur/${utilisateurId}`);
        
        return this.http.post<ItineraireResponseDTO>(`${this.apiUrl}/utilisateur/${utilisateurId}`, request);
    }

    getItinerairesUtilisateur(utilisateurId: number): Observable<ItineraireResponseDTO[]> {
        console.log('🔍 Getting itineraries for user:', utilisateurId);
        console.log('🔍 API URL:', `${this.apiUrl}/utilisateur/${utilisateurId}`);
        
        return this.http.get<ItineraireResponseDTO[]>(`${this.apiUrl}/utilisateur/${utilisateurId}`);
    }

    getItineraireById(id: number, utilisateurId: number): Observable<ItineraireDetailDTO> {
        console.log('🔍 Getting itinerary details for ID:', id, 'user:', utilisateurId);
        console.log('🔍 API URL:', `${this.apiUrl}/${id}/utilisateur/${utilisateurId}`);
        
        return this.http.get<ItineraireDetailDTO>(`${this.apiUrl}/${id}/utilisateur/${utilisateurId}`);
    }

    updateItineraire(id: number, request: UpdateItineraireDTO, utilisateurId: number): Observable<ItineraireResponseDTO> {
        console.log('🔍 Updating itinerary:', id, 'with request:', request);
        return this.http.put<ItineraireResponseDTO>(`${this.apiUrl}/${id}/utilisateur/${utilisateurId}`, request);
    }

    supprimerItineraire(id: number, utilisateurId: number): Observable<void> {
        console.log('🔍 Deleting itinerary:', id);
        return this.http.delete<void>(`${this.apiUrl}/${id}/utilisateur/${utilisateurId}`);
    }

    // Advanced Features
    optimiserItineraire(id: number, utilisateurId: number): Observable<ItineraireResponseDTO> {
        console.log('🔍 Optimizing itinerary:', id);
        return this.http.post<ItineraireResponseDTO>(`${this.apiUrl}/${id}/optimiser/utilisateur/${utilisateurId}`, {});
    }

    ajouterDestination(id: number, destinationId: number, utilisateurId: number): Observable<ItineraireResponseDTO> {
        console.log('🔍 Adding destination:', destinationId, 'to itinerary:', id);
        return this.http.post<ItineraireResponseDTO>(`${this.apiUrl}/${id}/destinations/${destinationId}/utilisateur/${utilisateurId}`, {});
    }

    supprimerDestination(id: number, destinationId: number, utilisateurId: number): Observable<ItineraireResponseDTO> {
        console.log('🔍 Removing destination:', destinationId, 'from itinerary:', id);
        return this.http.delete<ItineraireResponseDTO>(`${this.apiUrl}/${id}/destinations/${destinationId}/utilisateur/${utilisateurId}`);
    }

    rechercherItineraires(request: RechercheItineraireDTO, utilisateurId: number): Observable<ItineraireResponseDTO[]> {
        console.log('🔍 Searching itineraries with request:', request);
        return this.http.post<ItineraireResponseDTO[]>(`${this.apiUrl}/rechercher/utilisateur/${utilisateurId}`, request);
    }

    // Helper method to get current user itineraries
    getMyItineraries(): Observable<ItineraireResponseDTO[]> {
        const utilisateurId = this.getCurrentUserId();
        if (utilisateurId) {
            return this.getItinerairesUtilisateur(utilisateurId);
        }
        throw new Error('User not logged in');
    }

    // Helper method to get itinerary details
    getItineraryDetails(id: number): Observable<ItineraireDetailDTO> {
        const utilisateurId = this.getCurrentUserId();
        if (utilisateurId) {
            return this.getItineraireById(id, utilisateurId);
        }
        throw new Error('User not logged in');
    }

    private getCurrentUserId(): number | null {
        const currentUser = this.authService.currentUserValue;
        console.log('🔍 Current user from AuthService:', currentUser);
        return currentUser ? currentUser.id : null;
    }
}
