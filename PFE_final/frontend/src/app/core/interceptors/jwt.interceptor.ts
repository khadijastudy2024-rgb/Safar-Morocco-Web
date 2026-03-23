import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
    constructor(private authService: AuthService) { }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const token = this.authService.getToken();
        const isAuthUrl = request.url.includes('/api/auth');

        // Read selected language from localStorage (set by LanguageService)
        const lang = localStorage.getItem('selected_language') || 'en';

        const headers: { [name: string]: string } = {
            'Accept-Language': lang
        };

        // Only add token if it exists and we're NOT calling an auth endpoint
        if (token && !isAuthUrl) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        request = request.clone({ setHeaders: headers });
        return next.handle(request);
    }
}
