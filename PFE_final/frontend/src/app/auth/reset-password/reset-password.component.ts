import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  template: `
    <div class="auth-container d-flex justify-content-center align-items-center min-vh-100 bg-light">
      <div class="card shadow-lg p-4" style="max-width: 400px; width: 100%;">
        <div class="text-center mb-4">
          <h2 class="fw-bold text-primary">Nouveau mot de passe</h2>
          <p class="text-muted">Créez un nouveau mot de passe pour votre compte.</p>
        </div>

        <div *ngIf="validatingToken" class="text-center mb-3">
          <span class="spinner-border text-primary"></span>
          <p class="mt-2 text-muted">Validation du lien en cours...</p>
        </div>

        <div *ngIf="!validatingToken && !tokenValid && error" class="alert alert-danger mb-3 text-center">
          {{ error }}
          <br><br>
          <a routerLink="/auth/forgot-password" class="btn btn-outline-danger btn-sm">Demander un nouveau lien</a>
        </div>

        <div *ngIf="!validatingToken && successMessage" class="alert alert-success mt-3 text-center">
            {{ successMessage }}
            <br><br>
            <a routerLink="/auth/login" class="btn btn-primary w-100">Se connecter</a>
        </div>

        <form *ngIf="!validatingToken && tokenValid && !successMessage" [formGroup]="resetForm" (ngSubmit)="onSubmit()">
          
          <div class="mb-3">
            <label class="form-label">Nouveau mot de passe</label>
            <input type="password" class="form-control" formControlName="newPassword" placeholder="******">
            <div *ngIf="resetForm.get('newPassword')?.touched && resetForm.get('newPassword')?.invalid" class="text-danger small mt-1">
              Mot de passe requis (au moins 6 caractères).
            </div>
          </div>

          <div class="mb-3">
            <label class="form-label">Confirmez le mot de passe</label>
            <input type="password" class="form-control" formControlName="confirmPassword" placeholder="******">
            <div *ngIf="resetForm.hasError('passwordsMismatch') && resetForm.get('confirmPassword')?.touched" class="text-danger small mt-1">
              Les mots de passe ne correspondent pas.
            </div>
          </div>

          <button type="submit" class="btn btn-primary w-100 mb-3" [disabled]="resetForm.invalid || loading">
            <span *ngIf="loading" class="spinner-border spinner-border-sm me-2"></span>
            Enregistrer
          </button>
        </form>
      </div>
    </div>
  `
})
export class ResetPasswordComponent implements OnInit {
  resetForm: FormGroup;
  token: string | null = null;
  validatingToken = true;
  tokenValid = false;
  loading = false;
  error = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private apiService: ApiService,
    private cdr: ChangeDetectorRef
  ) {
    this.resetForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.token = params['token'];
      if (!this.token) {
        this.error = "Lien de réinitialisation invalide ou manquant.";
        this.validatingToken = false;
        return;
      }

      this.apiService.validateResetToken(this.token).subscribe({
        next: (isValid: boolean) => {
          this.validatingToken = false;
          if (isValid) {
            this.tokenValid = true;
          } else {
            this.error = "Le lien a expiré ou est invalide.";
          }
          this.cdr.detectChanges();
        },
        error: (err: any) => {
          this.validatingToken = false;
          this.error = "Une erreur est survenue lors de la validation du lien.";
          this.cdr.detectChanges();
        }
      });
    });
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('newPassword')?.value === g.get('confirmPassword')?.value
      ? null : { 'passwordsMismatch': true };
  }

  onSubmit(): void {
    if (this.resetForm.invalid || !this.token) return;

    this.loading = true;
    this.error = '';

    const data = {
      token: this.token,
      newPassword: this.resetForm.value.newPassword
    };

    this.apiService.resetPassword(data).subscribe({
      next: (res: any) => {
        this.successMessage = res.message || "Mot de passe modifié avec succès.";
        this.loading = false;
        this.cdr.detectChanges();
        
        // Redirect to login after 2 seconds
        setTimeout(() => {
          this.router.navigate(['/auth/login']);
        }, 2000);
      },
      error: (err: any) => {
        this.error = "Erreur lors de la réinitialisation du mot de passe.";
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }
}
