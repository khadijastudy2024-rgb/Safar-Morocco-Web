import { Injectable }  from '@angular/core';
import { HttpClient }  from '@angular/common/http';
import { Observable, forkJoin, of, throwError }  from 'rxjs';
import { map, catchError }  from 'rxjs/operators';

@Injectable({
    providedIn: 'root'
})
export class ApiService {

    constructor(private http: HttpClient) { }

    // Destinations
    getDestinations(): Observable<any[]> {
        return this.http.get<any[]>('/api/destinations').pipe(
            catchError(err => {
                console.error('âŒ ApiService: GET /api/destinations FAILED', err);
                return throwError(() => err);
            })
        );
    }

    getDestination(id: number): Observable<any> {
        return this.http.get<any>(`/api/destinations/${id}`).pipe(
            catchError(err => {
                console.error(`âŒ ApiService: GET /api/destinations/${id} FAILED`, err);
                return throwError(() => err);
            })
        );
    }

    searchDestinations(keyword: string): Observable<any[]> {
        return this.getDestinations().pipe(
            map(destinations => destinations.filter(d =>
                d.nom?.toLowerCase().includes(keyword.toLowerCase()) ||
                d.type?.toLowerCase().includes(keyword.toLowerCase())
            ))
        );
    }

    createDestination(data: any): Observable<any> {
        return this.http.post('/api/destinations', data);
    }

    deleteDestination(id: number): Observable<any> {
        return this.http.delete(`/api/destinations/${id}`);
    }

    addMedia(id: number, media: any): Observable<any> {
        return this.http.post(`/api/destinations/${id}/media`, media);
    }

    // Reviews (Avis)
    getReviews(destinationId: number): Observable<any[]> {
        return this.http.get<any[]>(`/api/avis/destination/${destinationId}`).pipe(
            catchError(() => of([]))
        );
    }

    getAllReviews(): Observable<any[]> {
        return this.http.get<any[]>('/api/avis').pipe(
            catchError(() => of([]))
        );
    }

    addReview(review: any): Observable<any> {
        return this.http.post('/api/avis', review);
    }

    moderateReview(id: number, status: string): Observable<any> {
        return this.http.put(`/api/avis/${id}`, { status });
    }

    deleteReview(id: number): Observable<any> {
        return this.http.delete(`/api/avis/${id}`);
    }

    // Events
    getEvents(): Observable<any[]> {
        return this.http.get<any[]>('/api/evenements');
    }

    getEventById(id: number): Observable<any> {
        return this.http.get<any>(`/api/evenements/${id}`);
    }

    createEvent(event: any): Observable<any> {
        return this.http.post<any>(`/api/evenements/destination/${event.destinationId}`, event);
    }

    updateEvent(id: number, event: any): Observable<any> {
        return this.http.put<any>(`/api/evenements/${id}`, event);
    }

    deleteEvent(id: number): Observable<any> {
        return this.http.delete<any>(`/api/evenements/${id}`);
    }

    // Event Reservations
    bookEvent(eventId: number): Observable<any> {
        return this.http.post<any>(`/api/reservations/${eventId}`, {});
    }

    getMyEventReservations(): Observable<any[]> {
        return this.http.get<any[]>('/api/reservations/my');
    }

    cancelEventReservation(id: number): Observable<any> {
        return this.http.put<any>(`/api/reservations/${id}/cancel`, {});
    }

    // Users & Admin
    getAllUsers(): Observable<any[]> {
        return this.http.get<any[]>('/api/utilisateurs/all/list');
    }

    deleteUser(id: number): Observable<any> {
        return this.http.delete<any>(`/api/utilisateurs/${id}`);
    }

    updateUserRole(id: number, role: string): Observable<any> {
        return this.http.put<any>(`/api/utilisateurs/${id}/role`, { role });
    }

    getCurrentUser(): Observable<any> {
        return this.http.get<any>('/api/utilisateurs/profile');
    }

    updateProfile(data: any): Observable<any> {
        return this.http.put<any>('/api/utilisateurs/profile', data);
    }

    uploadProfilePhoto(file: File): Observable<any> {
        const formData = new FormData();
        formData.append('file', file);
        return this.http.post<any>('/api/utilisateurs/profile/photo', formData);
    }

    forgotPassword(email: string): Observable<any> {
        return this.http.post('/api/auth/forgot-password', { email });
    }

    validateResetToken(token: string): Observable<boolean> {
        return this.http.get<boolean>(`/api/auth/validate-reset-token?token=${token}`);
    }

    resetPassword(data: any): Observable<any> {
        return this.http.post('/api/auth/reset-password', data);
    }

    // 2FA
    setup2FA(): Observable<any> {
        return this.http.post('/api/2fa/setup', {});
    }

    verify2FA(data: any): Observable<any> {
        return this.http.post('/api/2fa/verify', data);
    }

    disable2FA(): Observable<any> {
        return this.http.post('/api/2fa/disable', {});
    }

    validateLogin2FA(data: any): Observable<any> {
        return this.http.post('/api/2fa/validate-login', data);
    }

    // Preferences (Mapped to Profile)
    getPreferences(): Observable<any> {
        return this.getCurrentUser().pipe(
            map(user => ({
                languagePreference: user.langue
            }))
        );
    }

    updatePreferences(prefs: any): Observable<any> {
        return this.updateProfile({
            langue: prefs.languagePreference
        });
    }

    // Stats (Admin) - Using Audit endpoints or counting endpoints
    // Stats (Admin)
    getAdminStats(): Observable<any> {
        return forkJoin({
            users: this.getAllUsers(),
            destinations: this.getDestinations(),
            events: this.getEvents(),
            activeUsersObj: this.http.get<any>('/api/utilisateurs/stats/active').pipe(catchError((err: any) => of({ activeUsers: 0 })))
        }).pipe(
            map(data => {
                // 1. Category Stats
                const categoryStats: { [key: string]: number } = {};
                data.destinations.forEach((d: any) => {
                    const cat = d.categorie || d.category || 'Uncategorized';
                    categoryStats[cat] = (categoryStats[cat] || 0) + 1;
                });

                // 2. User Growth (Last 6 Months)
                const userGrowth: { [key: string]: number } = {};
                const now = new Date();
                const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

                // Initialize last 6 months with 0
                for (let i = 5; i >= 0; i--) {
                    const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
                    const monthName = months[d.getMonth()];
                    userGrowth[monthName] = 0;
                }

                data.users.forEach((u: any) => {
                    if (u.dateInscription) {
                        const date = new Date(u.dateInscription);
                        const monthName = months[date.getMonth()];
                        if (userGrowth.hasOwnProperty(monthName)) {
                            userGrowth[monthName]++;
                        }
                    }
                });

                // Cumulative growth? Or new users per month? Dashboard looks like "New Travelers" (Line chart usually cumulative or trend).
                // Let's make it cumulative for "Total Travelers" feel, or just monthly for "New".
                // Dashboard says "New Travelers". So monthly count is fine.

                // 3. Activity Stats (Events count per month as proxy)
                const activityStats: { [key: string]: number } = {};
                for (let i = 5; i >= 0; i--) {
                    const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
                    const monthName = months[d.getMonth()];
                    activityStats[monthName] = 0;
                }
                data.events.forEach((e: any) => {
                    // Assuming event has date property. dateDebut?
                    const dateStr = e.dateDebut || e.date;
                    if (dateStr) {
                        const date = new Date(dateStr);
                        const monthName = months[date.getMonth()];
                        if (activityStats.hasOwnProperty(monthName)) {
                            activityStats[monthName]++;
                        }
                    }
                });

                return {
                    totalUsers: data.users.length,
                    activeUsers: data.activeUsersObj.activeUsers || 0,
                    totalDestinations: data.destinations.length,
                    upcomingEvents: data.events.length,
                    pendingReviews: 0,
                    categoryStats: categoryStats,
                    userGrowth: userGrowth,
                    activityStats: activityStats
                };
            })
        );
    }

    getActivityLogs(): Observable<any[]> {
        return this.http.get<any[]>('/api/activities').pipe(
            catchError(err => {
                console.error('âŒ ApiService: GET /api/activities FAILED', err);
                return of([]);
            })
        );
    }

    getAuditLogs(): Observable<any[]> {
        return this.http.get<any[]>('/api/admin/audit-logs').pipe(
            catchError(err => {
                console.error('âŒ ApiService: GET /api/admin/audit-logs FAILED', err);
                return of([]);
            })
        );
    }

    deleteAccount(): Observable<any> {
        return this.http.delete('/api/utilisateurs/me');
    }

    // Chatbot (Spring Boot Backend) - REPLACED BY ChatService

    // File Upload (Admin only for destinations/media)
    uploadFile(destinationId: number, file: File, description: string = ''): Observable<any> {
        const formData = new FormData();
        formData.append('file', file);
        if (description) formData.append('description', description);
        return this.http.post<any>(`/api/media/upload/destination/${destinationId}`, formData);
    }

    // Itineraries
    getUserItineraries(userId: number): Observable<any[]> {
        return this.http.get<any[]>(`/api/itineraires/utilisateur/${userId}`).pipe(
            catchError(err => {
                console.error('âŒ ApiService: GET /api/itineraires/utilisateur FAILED', err);
                return of([]);
            })
        );
    }

    // My Itineraries / Reservation History
    getMyItineraries(): Observable<any[]> {
        return this.http.get<any[]>('/api/utilisateurs/my-itineraries').pipe(
            catchError(err => {
                console.error('âŒ ApiService: GET /api/utilisateurs/my-itineraries FAILED', err);
                return of([]);
            })
        );
    }
}
