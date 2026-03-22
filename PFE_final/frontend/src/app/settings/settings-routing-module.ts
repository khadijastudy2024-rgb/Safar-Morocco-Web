import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Settings } from './settings';
import { SecuritySettings } from './components/security-settings/security-settings';
import { PreferencesSettings } from './components/preferences-settings/preferences-settings';
const routes: Routes = [
  {
    path: '',
    component: Settings,
    children: [
      { path: '', redirectTo: 'preferences', pathMatch: 'full' },
      { path: 'security', component: SecuritySettings },
      { path: 'preferences', component: PreferencesSettings }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SettingsRoutingModule { }
