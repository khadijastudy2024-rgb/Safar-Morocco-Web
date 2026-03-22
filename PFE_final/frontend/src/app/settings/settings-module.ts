import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { SettingsRoutingModule } from './settings-routing-module';
import { Settings } from './settings';
import { SecuritySettings } from './components/security-settings/security-settings';
import { PreferencesSettings } from './components/preferences-settings/preferences-settings';
import { NotificationSettings } from './components/notification-settings/notification-settings';
import { PrivacySettings } from './components/privacy-settings/privacy-settings';


import { TranslateModule } from '@ngx-translate/core';

@NgModule({
  declarations: [
    Settings,
    PreferencesSettings,
    NotificationSettings,
    PrivacySettings
  ],
  imports: [
    CommonModule,
    SettingsRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    TranslateModule.forChild(),
    SecuritySettings
  ]
})
export class SettingsModule { }
