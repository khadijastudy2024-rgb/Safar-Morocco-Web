import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { Router } from '@angular/router';

@Component({
    standalone: false,
    selector: 'app-admin-layout',
    templateUrl: './layout.component.html',
    styleUrls: ['./layout.component.css']
})
export class AdminLayoutComponent implements OnInit {
    isSidebarOpen = true;
    adminUser: any;

    constructor(
        private authService: AuthService,
        private router: Router
    ) { }

    ngOnInit(): void {
        this.adminUser = this.authService.currentUserValue;
    }

    toggleSidebar() {
        this.isSidebarOpen = !this.isSidebarOpen;
    }

    logout() {
        this.authService.logout();
        this.router.navigate(['/auth/login']);
    }

    viewProfile() {
        this.router.navigate(['/profile']);
    }

    openSettings() {
        this.router.navigate(['/settings']);
    }
}

