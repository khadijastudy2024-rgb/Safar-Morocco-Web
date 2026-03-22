import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MapRoutingModule } from './map-routing.module';
import { GlobalMapComponent } from './pages/global-map/global-map.component';
import { SharedModule } from '../shared/shared.module'; // Adjust path if needed

@NgModule({
    declarations: [
        GlobalMapComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        MapRoutingModule,
        SharedModule
    ]
})
export class MapModule { }
