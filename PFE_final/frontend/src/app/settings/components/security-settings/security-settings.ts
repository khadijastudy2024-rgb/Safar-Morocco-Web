import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { SettingsService } from '../../../core/services/settings';
import { ApiService } from '../../../core/services/api.service';
import { TranslateService, TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-security-settings',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, TranslateModule],
  templateUrl: './security-settings.html',
  styleUrl: './security-settings.css',
})
export class SecuritySettings implements OnInit {
  passwordForm: FormGroup;
  message = '';
  error = '';

  // 2FA Logic
  is2faEnabled = false;
  qrCodeUri = '';
  secret = '';
  verificationCode = '';

  constructor(
    private fb: FormBuilder,
    private settingsService: SettingsService,
    private apiService: ApiService,
    private translate: TranslateService,
    private cdr: ChangeDetectorRef
  ) {
    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, { validator: this.passwordMatchValidator });
  }

  ngOnInit(): void {
    this.settingsService.getSettings().subscribe({
      next: (data) => {
        this.is2faEnabled = data.twoFactorEnabled;
      },
      error: (err: any) => console.error(err)
    });
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('newPassword')?.value === g.get('confirmPassword')?.value
      ? null : { mismatch: true };
  }

  changePassword() {
    if (this.passwordForm.invalid) return;

    const payload = {
      ancienMotDePasse: this.passwordForm.value.currentPassword,
      nouveauMotDePasse: this.passwordForm.value.newPassword
    };

    this.settingsService.updatePassword(payload).subscribe({
      next: () => {
        this.message = this.translate.instant('SETTINGS.PASS_CHANGED');
        this.error = '';
        this.passwordForm.reset();
      },
      error: (err: any) => {
        this.error = err.error?.error || this.translate.instant('SETTINGS.PASS_FAIL');
        this.message = '';
      }
    });
  }

  loading2FA = false;

  setup2FA() {
    if (this.loading2FA) return;
    this.loading2FA = true;
    this.error = '';
    this.message = '';
    this.verificationCode = '';

    this.apiService.setup2FA().subscribe({
      next: (res: any) => {
        this.qrCodeUri = res.qrCodeUri;
        this.secret = res.secret;
        this.loading2FA = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.loading2FA = false;
        this.error = 'Erreur lors de la configuration 2FA.';
        this.cdr.detectChanges();
      }
    });
  }

  verify2FA() {
    const payload = { secret: this.secret, code: this.verificationCode };
    this.apiService.verify2FA(payload).subscribe({
      next: (res: any) => {
        this.is2faEnabled = true;
        this.qrCodeUri = '';
        this.secret = '';
        this.verificationCode = '';
        this.message = this.translate.instant('SETTINGS.TWO_FACTOR.ENABLED_MSG');
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('2FA Verify Error:', err);
        this.error = err.error?.message || (typeof err.error === 'string' ? err.error : 'Code invalide ou erreur de connexion.');
      }
    });
  }

  disable2FA() {
    this.apiService.disable2FA().subscribe({
      next: (res) => {
        this.is2faEnabled = false;
        this.message = this.translate.instant('SETTINGS.TWO_FACTOR.DISABLED_MSG');
      }
    });
  }
}
