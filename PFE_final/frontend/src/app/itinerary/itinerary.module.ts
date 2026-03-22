import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { ItineraryRoutingModule } from './itinerary-routing.module';
import { ItineraryDetailComponent } from './itinerary-detail/itinerary-detail.component';
import { ItineraryCreateComponent } from './itinerary-create/itinerary-create.component';
import { ItineraryListComponent } from './itinerary-list/itinerary-list.component';
import { SharedModule } from '../shared/shared.module';

@NgModule({
    declarations: [
        ItineraryDetailComponent,
        ItineraryCreateComponent,
        ItineraryListComponent
    ],
    imports: [
        CommonModule,
        ReactiveFormsModule,
        ItineraryRoutingModule,
        SharedModule
    ]
})
export class ItineraryModule { }
