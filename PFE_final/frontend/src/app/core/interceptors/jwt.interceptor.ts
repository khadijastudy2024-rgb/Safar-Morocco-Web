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

        // Only add token if it exists and we're NOT calling an auth endpoint 
        // (Login/Register don't need the Bearer token usually, though it doesn't hurt, 
        // but it can cause 401s if an expired token is sent during login)
        if (token && !isAuthUrl) {
            request = request.clone({
                setHeaders: {
                    Authorization: `Bearer ${token}`
                }
            });
        }

        return next.handle(request);
    }
}
