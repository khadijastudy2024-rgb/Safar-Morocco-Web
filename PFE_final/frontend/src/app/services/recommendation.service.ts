import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface RecommendationDTO {
  id: number;
  name: string;
  description: string;
  reason: string;
  imageUrl: string;
}

export interface RecommendationResponse {
  recommendations: RecommendationDTO[];
}

@Injectable({
  providedIn: 'root'
})
export class RecommendationService {
  private apiUrl = '/api/recommendations';

  constructor(private http: HttpClient) {}

  getPersonalizedRecommendations(): Observable<RecommendationResponse | null> {
    return this.http.get<RecommendationResponse>(this.apiUrl).pipe(
      catchError(error => {
        console.error('Error fetching recommendations:', error);
        // Fallback to null so the application does not crash
        return of(null);
      })
    );
  }
}
