import { Component } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    standalone: false,
    selector: 'app-admin-sidebar',
    templateUrl: './sidebar.component.html',
    styleUrls: ['./sidebar.component.css']
})
export class AdminSidebarComponent {
    constructor(private authService: AuthService) { }

    logout() {
        this.authService.logout();
    }
}
