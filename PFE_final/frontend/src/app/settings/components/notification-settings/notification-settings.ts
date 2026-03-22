import { Component, OnInit } from '@angular/core';
import { SettingsService, SettingsDTO } from '../../../core/services/settings';

@Component({
    selector: 'app-notification-settings',
    standalone: false,
    templateUrl: './notification-settings.html',
    styleUrl: './notification-settings.css',
})
export class NotificationSettings implements OnInit {
    settings: SettingsDTO = {
        language: 'EN',
        twoFactorEnabled: false
    };

    constructor(private settingsService: SettingsService) { }

    ngOnInit(): void {
        this.settingsService.getSettings().subscribe({
            next: (data) => this.settings = data,
            error: (err: any) => console.error('Failed to load notification settings', err)
        });
    }

    toggleNotification(type: 'email' | 'sms') {
        // Feature reverted
    }
}
