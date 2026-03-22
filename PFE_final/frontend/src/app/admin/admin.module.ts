import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../shared/shared.module';
import { BaseChartDirective } from 'ng2-charts';

import { AdminLayoutComponent } from './layout/layout.component';
import { AdminDashboardComponent } from './dashboard/dashboard.component';
import { AdminDestinationListComponent } from './destinations/destination-list.component';
import { UserListComponent } from './users/user-list.component';
import { AdminEventListComponent } from './events/event-list.component';
import { EventDialogComponent } from './events/event-dialog.component';
import { ActivityLogListComponent } from './logs/activity-log-list.component';
import { ReviewListComponent } from './reviews/review-list.component';
import { AdminSidebarComponent } from './components/sidebar/sidebar.component';
import { MatCardModule } from '@angular/material/card';
import { MatMenuModule } from '@angular/material/menu';
import { OfferList } from './offers/offer-list/offer-list';
import { ReservationList } from './reservations/reservation-list/reservation-list';
import { OfferDialogComponent } from './offers/offer-dialog/offer-dialog';


import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatToolbarModule } from '@angular/material/toolbar';

const routes: Routes = [
    {
        path: '',
        component: AdminLayoutComponent,
        children: [
            { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
            { path: 'dashboard', component: AdminDashboardComponent },
            { path: 'destinations', component: AdminDestinationListComponent },
            { path: 'offers', component: OfferList },
            { path: 'reservations', component: ReservationList },
            { path: 'users', component: UserListComponent },
            { path: 'events', component: AdminEventListComponent },
            { path: 'logs', component: ActivityLogListComponent },
            { path: 'reviews', component: ReviewListComponent }
        ]
    }
];

@NgModule({
    declarations: [
        AdminLayoutComponent,
        AdminDashboardComponent,
        AdminDestinationListComponent,
        UserListComponent,
        AdminEventListComponent,
        EventDialogComponent,
        ActivityLogListComponent,
        ReviewListComponent,
        AdminSidebarComponent,
        OfferList,
        ReservationList,
        OfferDialogComponent
    ],

    imports: [
        CommonModule,
        RouterModule.forChild(routes),
        FormsModule,
        ReactiveFormsModule,
        MatDatepickerModule,
        MatNativeDateModule,
        MatTableModule,
        MatPaginatorModule,
        MatSortModule,
        MatButtonModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatSnackBarModule,
        MatDialogModule,
        MatSidenavModule,
        MatListModule,
        MatToolbarModule,
        MatCardModule,
        MatMenuModule,
        SharedModule,
        BaseChartDirective
    ]
})
export class AdminModule { }
