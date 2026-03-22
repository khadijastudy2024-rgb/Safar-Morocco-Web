import { Component, OnInit } from '@angular/core';
import { SettingsService, SettingsDTO } from '../../../core/services/settings';

@Component({
    selector: 'app-privacy-settings',
    standalone: false,
    templateUrl: './privacy-settings.html',
    styleUrl: './privacy-settings.css',
})
export class PrivacySettings implements OnInit {
    settings: SettingsDTO = {
        language: 'EN',
        twoFactorEnabled: false
    };

    constructor(private settingsService: SettingsService) { }

    ngOnInit(): void {
        this.settingsService.getSettings().subscribe({
            next: (data) => this.settings = data,
            error: (err: any) => console.error('Failed to load privacy settings', err)
        });
    }

    togglePrivacy(type: 'public' | 'email') {
        // Feature reverted
    }
}
