import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private apiUrl = '/api/auth';
    private TOKEN_KEY = 'token';
    private REFRESH_TOKEN_KEY = 'refreshToken';
    private userSubject = new BehaviorSubject<any>(this.getUserFromStorage());
    public user$ = this.userSubject.asObservable();
    public currentUserSubject = this.userSubject; // Alias for compatibility if needed

    constructor(private http: HttpClient, private router: Router) { }

    private getUserFromStorage(): any {
        const user = localStorage.getItem('user');
        return user ? JSON.parse(user) : null;
    }

    public get currentUserValue(): any {
        return this.userSubject.value;
    }

    public get isLoggedIn(): boolean {
        return !!this.userSubject.value;
    }

    public get isAdmin(): boolean {
        return this.userSubject.value?.role === 'ADMIN';
    }

    register(user: any): Observable<any> {
        return this.http.post(`${this.apiUrl}/register`, user);
    }

    verifyToken(token: string): Observable<any> {
        return this.http.get(`${this.apiUrl}/verify?token=${token}`);
    }

    login(credentials: any): Observable<any> {
        return this.http.post(`${this.apiUrl}/login`, credentials).pipe(
            tap((response: any) => {
                // Only handle success if no 2FA is required, otherwise the component handles it
                if (!response.requiresTwoFactor) {
                    this.handleAuthSuccess(response);
                }
            }),
            catchError(err => {
                console.error('❌ AuthService: Login FAILED', err);
                return this.handleError(err);
            })
        );
    }

    handleAuthSuccess(response: any) {
        if (response.accessToken) {
            localStorage.setItem(this.TOKEN_KEY, response.accessToken);
            if (response.refreshToken) {
                localStorage.setItem(this.REFRESH_TOKEN_KEY, response.refreshToken);
            }

            const user: any = {
                id: response.userId,
                nom: response.nom || response.name,
                email: response.email,
                role: response.role
            };
            localStorage.setItem('user', JSON.stringify(user));
            this.userSubject.next(user);
        }
    }

    logout() {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem('user');
        localStorage.removeItem(this.REFRESH_TOKEN_KEY);
        localStorage.removeItem('chat_session_id');
        this.userSubject.next(null);
        this.router.navigate(['/auth/login']);
    }

    updateUser(user: any) {
        localStorage.setItem('user', JSON.stringify(user));
        this.userSubject.next(user);
    }

    getToken(): string | null {
        return localStorage.getItem(this.TOKEN_KEY);
    }

    private handleError(error: HttpErrorResponse) {
        let errorMessage = 'An error occurred';
        if (error.error instanceof ErrorEvent) {
            errorMessage = error.error.message;
        } else {
            errorMessage = error.error?.message || `Error Code: ${error.status}`;
        }
        return throwError(() => new Error(errorMessage));
    }
}
