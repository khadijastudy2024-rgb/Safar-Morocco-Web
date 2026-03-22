import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SettingsDTO {
  language: string;
  twoFactorEnabled: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class SettingsService {
  private apiUrl = '/api/settings';

  constructor(private http: HttpClient) { }

  getSettings(): Observable<SettingsDTO> {
    return this.http.get<SettingsDTO>(this.apiUrl);
  }

  updateSettings(settings: SettingsDTO): Observable<any> {
    return this.http.put(this.apiUrl, settings);
  }

  updatePassword(data: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/password`, data);
  }

}
