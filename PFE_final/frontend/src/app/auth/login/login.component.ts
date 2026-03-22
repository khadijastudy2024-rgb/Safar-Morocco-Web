import { Component, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { ApiService } from '../../core/services/api.service';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
    standalone: false,
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})
export class LoginComponent {
    loginForm: FormGroup;
    loading = false;
    hidePassword = true;

    // 2FA state
    requiresTwoFactor = false;
    twoFactorCode = '';
    tempEmail = '';

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private apiService: ApiService,
        private router: Router,
        private snackBar: MatSnackBar,
        private cdr: ChangeDetectorRef
    ) {
        this.loginForm = this.fb.group({
            email: ['', [Validators.required, Validators.email]],
            motDePasse: ['', Validators.required]
        });
    }

    onSubmit() {
        if (this.loginForm.invalid) return;

        this.loading = true;
        const { email, motDePasse } = this.loginForm.value;

        this.authService.login({ email, motDePasse }).subscribe({
            next: (response: any) => {
                if (response.requiresTwoFactor) {
                    this.requiresTwoFactor = true;
                    this.tempEmail = email;
                    this.loading = false;
                    this.cdr.detectChanges(); // Trigger immediate UI refresh
                    this.snackBar.open('Code 2FA requis', 'OK', { duration: 3000 });
                } else {
                    this.loading = false;
                    this.snackBar.open('Login Successful', 'Close', { duration: 3000 });
                    this.router.navigate(['/']);
                }
            },
            error: (err: any) => {
                this.loading = false;
                console.error('Login error:', err);
                let errorMessage = 'Login failed. Please check your connection.';
                if (err && err.message) {
                    errorMessage = err.message.includes('Error Code: 0') ? errorMessage : err.message;
                }
                this.snackBar.open(errorMessage, 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
            }
        });
    }

    verify2FA() {
        if (!this.twoFactorCode || this.twoFactorCode.length < 6) return;
        this.loading = true;

        this.apiService.validateLogin2FA({ email: this.tempEmail, code: this.twoFactorCode }).subscribe({
            next: (res) => {
                this.authService.handleAuthSuccess(res);
                this.loading = false;
                this.cdr.detectChanges();
                this.router.navigate(['/']);
            },
            error: (err) => {
                this.loading = false;
                const msg = err.error?.message || (typeof err.error === 'string' ? err.error : 'Code 2FA invalide');
                this.snackBar.open(msg, 'Close', { duration: 5000 });
                this.cdr.detectChanges();
            }
        });
    }

    loginWithGoogle() {
        window.location.href = 'http://localhost:8088/oauth2/authorization/google';
    }
}
