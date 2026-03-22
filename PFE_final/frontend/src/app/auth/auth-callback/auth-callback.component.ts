import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
    standalone: false,
    selector: 'app-auth-callback',
    template: `
        <div class="auth-callback-container">
            <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
            <p class="mt-3">Completing login...</p>
        </div>
    `,
    styles: [`
        .auth-callback-container {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            height: 100vh;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
    `]
})
export class AuthCallbackComponent implements OnInit {
    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private authService: AuthService
    ) { }

    ngOnInit(): void {
        this.route.queryParams.subscribe(params => {
            const accessToken = params['accessToken'];
            const refreshToken = params['refreshToken'];
            const userId = params['userId'];
            const email = params['email'];
            const nom = params['nom'];
            const role = params['role'];


            if (accessToken) {
                // Store tokens
                localStorage.setItem('token', accessToken);
                if (refreshToken) localStorage.setItem('refreshToken', refreshToken);

                // Create user object
                const user = {
                    id: userId,
                    email: email,
                    nom: nom, // Backend sends 'nom' instead of 'username'
                    role: role
                };
                localStorage.setItem('user', JSON.stringify(user));

                // Update auth service
                this.authService.updateUser(user);

                // Redirect to home
                this.router.navigate(['/']);
            } else {
                // Redirect to login if no token
                this.router.navigate(['/auth/login']);
            }
        });
    }
}
