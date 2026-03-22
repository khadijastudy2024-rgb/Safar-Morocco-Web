import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { EventRoutingModule } from './event-routing.module';
import { EventListComponent } from './list/event-list.component';
import { EventDetailComponent } from './detail/event-detail.component';
import { SharedModule } from '../shared/shared.module';

@NgModule({
    declarations: [
        EventListComponent,
        EventDetailComponent
    ],
    imports: [
        CommonModule,
        EventRoutingModule,
        SharedModule
    ]
})
export class EventModule { }
