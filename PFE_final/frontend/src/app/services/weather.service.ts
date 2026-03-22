import { Injectable }  from '@angular/core';
import { HttpClient }  from '@angular/common/http';
import { Observable }  from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WeatherService {
  private apiUrl = '/api/meteo';

  constructor(private http: HttpClient) { }

  getCurrentWeather(destinationId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/destination/${destinationId}`);
  }

  getForecast(destinationId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/destination/${destinationId}/all`);
  }
}
