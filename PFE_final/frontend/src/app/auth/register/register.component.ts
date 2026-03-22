import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TranslateService } from '@ngx-translate/core';

@Component({
    standalone: false,
    selector: 'app-register',
    templateUrl: './register.component.html',
    styleUrls: ['./register.component.css']
})
export class RegisterComponent {
    registerForm: FormGroup;
    loading = false;
    hidePassword = true;

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router,
        private snackBar: MatSnackBar,
        private translate: TranslateService
    ) {
        this.registerForm = this.fb.group({
            firstName: ['', Validators.required],
            lastName: ['', Validators.required],
            email: ['', [Validators.required, Validators.email]],
            motDePasse: ['', [Validators.required, Validators.minLength(8)]]
        });
    }

    onSubmit() {
        if (this.registerForm.invalid) return;

        this.loading = true;
        const formValue = this.registerForm.value;
        const userToRegister = {
            ...formValue,
            nom: `${formValue.firstName} ${formValue.lastName}`.trim()
        };

        this.authService.register(userToRegister).subscribe({
            next: () => {
                this.snackBar.open(
                    this.translate.instant('AUTH.REGISTER.SUCCESS'),
                    this.translate.instant('COMMON.CLOSE'),
                    { duration: 6000 }
                );
                this.router.navigate(['/auth/login']);
            },
            error: (err: any) => {
                this.loading = false;
                console.error('Registration error:', err);
                const defaultError = this.translate.instant('AUTH.REGISTER.ERROR');
                const errorMessage = err.error && typeof err.error === 'string'
                    ? err.error
                    : err.error && err.error.message
                        ? err.error.message
                        : defaultError;
                this.snackBar.open(
                    errorMessage,
                    this.translate.instant('COMMON.CLOSE'),
                    { duration: 5000 }
                );
            }
        });
    }

    loginWithGoogle() {
        window.location.href = 'http://localhost:8088/oauth2/authorization/google';
    }
}
