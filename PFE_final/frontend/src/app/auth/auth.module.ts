import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { AuthCallbackComponent } from './auth-callback/auth-callback.component';
import { SharedModule } from '../shared/shared.module';
import { RouterModule, Routes } from '@angular/router';
import { AuthRequired } from './auth-required/auth-required';
import { Verified } from './verified/verified';

const routes: Routes = [
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'auth-callback', component: AuthCallbackComponent },
    { path: 'auth-required', component: AuthRequired },
    { path: 'verified', component: Verified },
    {
        path: 'forgot-password',
        loadComponent: () => import('./forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent)
    },
    {
        path: 'reset-password',
        loadComponent: () => import('./reset-password/reset-password.component').then(m => m.ResetPasswordComponent)
    }
];

@NgModule({
    declarations: [
        LoginComponent,
        RegisterComponent,
        AuthCallbackComponent,
        AuthRequired,
        Verified
    ],
    imports: [
        CommonModule,
        SharedModule,
        RouterModule.forChild(routes)
    ]
})
export class AuthModule { }
