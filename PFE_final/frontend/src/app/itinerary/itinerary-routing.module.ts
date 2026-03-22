import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ItineraryListComponent } from './itinerary-list/itinerary-list.component';
import { ItineraryDetailComponent } from './itinerary-detail/itinerary-detail.component';
import { ItineraryCreateComponent } from './itinerary-create/itinerary-create.component';
import { AuthGuard } from '../core/guards/auth.guard';

const routes: Routes = [
    {
        path: '',
        component: ItineraryListComponent,
        canActivate: [AuthGuard],
        data: { animation: 'ItineraryList' }
    },
    {
        path: 'create',
        component: ItineraryCreateComponent,
        canActivate: [AuthGuard],
        data: { animation: 'ItineraryCreate' }
    },
    {
        path: 'detail/:id',
        component: ItineraryDetailComponent,
        canActivate: [AuthGuard],
        data: { animation: 'ItineraryDetail' }
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class ItineraryRoutingModule { }
