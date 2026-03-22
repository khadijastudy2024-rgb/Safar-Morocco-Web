import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class FileService {
    private apiUrl = 'http://localhost:8080/api/files';

    constructor(private http: HttpClient) { }

    uploadFile(file: File): Observable<{ url: string }> {
        const formData = new FormData();
        formData.append('file', file);
        return this.http.post<{ url: string }>(`${this.apiUrl}/upload`, formData);
    }

    uploadMultipleFiles(files: File[]): Observable<{ url: string }[]> {
        const formData = new FormData();
        for (let i = 0; i < files.length; i++) {
            formData.append('files', files[i]);
        }
        return this.http.post<{ url: string }[]>(`${this.apiUrl}/upload-multiple`, formData);
    }
}
