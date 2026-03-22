import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GlobalMapComponent } from './pages/global-map/global-map.component';

const routes: Routes = [
    { path: '', component: GlobalMapComponent }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class MapRoutingModule { }
