import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { AuthGuard } from './core/guards/auth.guard';
import { ProfileComponent } from './user/profile/profile.component';
import { HelpCenterComponent } from './pages/help-center/help-center.component';
import { ContactComponent } from './pages/contact/contact.component';
import { TermsComponent } from './pages/terms/terms.component';
import { PrivacyPolicyComponent } from './pages/privacy-policy/privacy-policy.component';
import { TermsAndConditionsComponent } from './pages/terms-and-conditions/terms-and-conditions.component';


const routes: Routes = [
    { path: '', component: HomeComponent, data: { animation: 'HomePage' } },
    { path: 'auth', loadChildren: () => import('./auth/auth.module').then(m => m.AuthModule), data: { animation: 'AuthPage' } },
    { path: 'destinations', loadChildren: () => import('./destination/destination.module').then(m => m.DestinationModule), canActivate: [AuthGuard], data: { animation: 'DestinationsPage' } },
    { path: 'itineraries', loadChildren: () => import('./itinerary/itinerary.module').then(m => m.ItineraryModule), canActivate: [AuthGuard], data: { animation: 'ItinerariesPages' } },
    { path: 'itineraires', loadChildren: () => import('./itinerary/itinerary.module').then(m => m.ItineraryModule), canActivate: [AuthGuard], data: { animation: 'ItinerairesPages' } },
    { path: 'admin', loadChildren: () => import('./admin/admin.module').then(m => m.AdminModule), canActivate: [AuthGuard], data: { animation: 'AdminPage' } },
    { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard], data: { animation: 'ProfilePage' } },
    { path: 'help-center', component: HelpCenterComponent },
    { path: 'contact', component: ContactComponent },
    { path: 'terms', component: TermsComponent },
    { path: 'privacy-policy', component: PrivacyPolicyComponent },
    { path: 'terms-and-conditions', component: TermsAndConditionsComponent },

    { path: 'events', loadChildren: () => import('./event/event.module').then(m => m.EventModule), canActivate: [AuthGuard], data: { animation: 'EventsPage' } },
    { path: 'map', loadChildren: () => import('./map/map.module').then(m => m.MapModule), canActivate: [AuthGuard], data: { animation: 'MapPage' } },
    { path: 'settings', loadChildren: () => import('./settings/settings-module').then(m => m.SettingsModule) },
    { path: 'oauth2/redirect', loadComponent: () => import('./oauth2-redirect/oauth2-redirect.component').then(m => m.OAuth2RedirectComponent) },
    { path: '**', redirectTo: '' }
];

@NgModule({
    imports: [RouterModule.forRoot(routes, { scrollPositionRestoration: 'enabled' })],
    exports: [RouterModule]
})
export class AppRoutingModule { }
