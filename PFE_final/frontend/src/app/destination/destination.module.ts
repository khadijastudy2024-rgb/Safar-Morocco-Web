import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DestinationListComponent } from './list/list.component';
import { DestinationDetailComponent } from './detail/detail.component';
import { ReservationDialogComponent } from './dialog/reservation-dialog.component';
import { DestinationMapComponent } from './map/map.component';
import { SharedModule } from '../shared/shared.module';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { MatTooltipModule } from '@angular/material/tooltip';

const routes: Routes = [
    { path: 'map', component: DestinationMapComponent },
    { path: '', component: DestinationListComponent },
    { path: ':id', component: DestinationDetailComponent }
];

@NgModule({
    declarations: [
        DestinationListComponent,
        DestinationDetailComponent,
        ReservationDialogComponent,
        DestinationMapComponent
    ],
    imports: [
        CommonModule,
        SharedModule,
        FormsModule,
        ReactiveFormsModule,
        MatChipsModule,
        MatDialogModule,
        MatSelectModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        MatProgressSpinnerModule,
        MatTooltipModule,
        RouterModule.forChild(routes)
    ]
})
export class DestinationModule { }
