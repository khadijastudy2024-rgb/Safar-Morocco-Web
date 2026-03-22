import { Component }  from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule }  from '@angular/forms';
import { RouterModule }  from '@angular/router';
import { ApiService }  from '../../core/services/api.service';
import { CommonModule }  from '@angular/common';
import { TranslateModule }  from '@ngx-translate/core';

@Component({
    selector: 'app-forgot-password',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule, TranslateModule],
    template: `
    <div class="auth-container d-flex justify-content-center align-items-center min-vh-100 bg-light">
      <div class="card shadow-lg p-4" style="max-width: 400px; width: 100%;">
        <div class="text-center mb-4">
          <h2 class="fw-bold text-primary">Mot de passe oublié</h2>
          <p class="text-muted">Entrez votre email pour recevoir un lien de réinitialisation.</p>
        </div>

        <form [formGroup]="forgotForm" (ngSubmit)="onSubmit()">
          <div class="mb-3">
            <label class="form-label">Email</label>
            <input type="email" class="form-control" formControlName="email" placeholder="exemple@email.com">
            <div *ngIf="forgotForm.get('email')?.touched && forgotForm.get('email')?.invalid" class="text-danger small">
              Email valide requis.
            </div>
          </div>

          <div *ngIf="message" class="alert alert-success small mb-3">
            {{ message }}
          </div>
          <div *ngIf="error" class="alert alert-danger small mb-3">
            {{ error }}
          </div>

          <button type="submit" class="btn btn-primary w-100 mb-3" [disabled]="forgotForm.invalid || loading">
            <span *ngIf="loading" class="spinner-border spinner-border-sm me-2"></span>
            Envoyer le lien
          </button>

          <div class="text-center">
            <a routerLink="/login" class="text-decoration-none">Retour à la connexion</a>
          </div>
        </form>
      </div>
    </div>
  `
})
export class ForgotPasswordComponent {
    forgotForm: FormGroup;
    loading = false;
    message = '';
    error = '';

    constructor(private fb: FormBuilder, private apiService: ApiService) {
        this.forgotForm = this.fb.group({
            email: ['', [Validators.required, Validators.email]]
        });
    }

    onSubmit() {
        if (this.forgotForm.invalid) return;

        this.loading = true;
        this.message = '';
        this.error = '';

        this.apiService.forgotPassword(this.forgotForm.value.email).subscribe({
            next: (res: any) => {
                this.message = (res && res.message) ? res.message : 'Un email de réinitialisation a été envoyé.';
                this.loading = false;
                this.forgotForm.reset();
            },
            error: (err: any) => {
                this.error = (err.error && err.error.message) ? err.error.message : 'Erreur lors de l\'envoi. Vérifiez que cet email existe.';
                this.loading = false;
            }
        });
    }
}
