import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { SettingsService, SettingsDTO } from '../../../core/services/settings';
import { LanguageService } from '../../../core/services/language.service';

@Component({
  selector: 'app-preferences-settings',
  standalone: false,
  templateUrl: './preferences-settings.html',
  styleUrl: './preferences-settings.css',
})
export class PreferencesSettings implements OnInit {
  settings: SettingsDTO = {
    language: 'EN',
    twoFactorEnabled: false
  };
  message = '';

  constructor(
    private settingsService: SettingsService,
    private translate: TranslateService,
    private languageService: LanguageService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.settingsService.getSettings().subscribe({
      next: (data: SettingsDTO) => {
        this.settings = data;
        this.cdr.detectChanges();
      },
      error: (err: any) => console.error('Failed to load settings', err)
    });
  }

  savePreferences() {
    this.settingsService.updateSettings(this.settings).subscribe({
      next: () => {
        this.message = this.translate.instant('SETTINGS.PREFERENCES_SAVED') || 'Preferences saved successfully';
        this.languageService.setLanguage(this.settings.language.toLowerCase()); // Apply language change
        this.cdr.detectChanges();
        setTimeout(() => {
          this.message = '';
          this.cdr.detectChanges();
        }, 3000);
      },
      error: (err: any) => console.error('Failed to save preferences', err)
    });
  }
}
