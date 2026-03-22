import { Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
    standalone: false,
    selector: 'app-user-list',
    templateUrl: './user-list.component.html',
    styleUrls: ['./user-list.component.css']
})
export class UserListComponent implements OnInit {
    displayedColumns: string[] = ['username', 'email', 'role', 'actions'];
    dataSource: MatTableDataSource<any>;

    @ViewChild(MatPaginator) paginator!: MatPaginator;
    @ViewChild(MatSort) sort!: MatSort;

    constructor(
        private apiService: ApiService,
        private snackBar: MatSnackBar,
        public authService: AuthService,
        private translate: TranslateService
    ) {
        this.dataSource = new MatTableDataSource();
    }

    isLoadingResults = true;

    ngOnInit(): void {
        this.loadUsers();
    }

    ngAfterViewInit() {
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
    }

    loadUsers() {
        this.isLoadingResults = true;
        this.apiService.getAllUsers().subscribe({
            next: (data) => {
                this.dataSource.data = data;
                this.isLoadingResults = false;
            },
            error: (err: any) => {
                console.error(err);
                this.isLoadingResults = false;
            }
        });
    }

    applyFilter(event: Event) {
        const filterValue = (event.target as HTMLInputElement).value;
        this.dataSource.filter = filterValue.trim().toLowerCase();
    }

    deleteUser(id: number) {
        if (confirm(this.translate.instant('ADMIN.USERS.DELETE_CONFIRM'))) {
            this.apiService.deleteUser(id).subscribe({
                next: () => {
                    this.snackBar.open(
                        this.translate.instant('ADMIN.USERS.DELETE_SUCCESS'),
                        this.translate.instant('COMMON.CLOSE'),
                        { duration: 3000 }
                    );
                    this.loadUsers();
                },
                error: () => this.snackBar.open(
                    this.translate.instant('ADMIN.USERS.DELETE_ERROR'),
                    this.translate.instant('COMMON.CLOSE'),
                    { duration: 3000 }
                )
            });
        }
    }

    toggleRole(user: any) {
        // ADMIN can switch any user between USER and ADMIN roles
        const newRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN';
        const confirmMsg = this.translate.instant('ADMIN.USERS.CHANGE_ROLE_CONFIRM', {
            name: user.nom || user.email,
            role: newRole
        });
        if (confirm(confirmMsg)) {
            this.apiService.updateUserRole(user.id, newRole).subscribe({
                next: () => {
                    this.snackBar.open(
                        this.translate.instant('ADMIN.USERS.ROLE_CHANGED_SUCCESS', { role: newRole }),
                        this.translate.instant('COMMON.CLOSE'),
                        { duration: 3000 }
                    );
                    this.loadUsers();
                },
                error: () => this.snackBar.open(
                    this.translate.instant('ADMIN.USERS.ROLE_CHANGED_ERROR'),
                    this.translate.instant('COMMON.CLOSE'),
                    { duration: 3000 }
                )
            });
        }
    }
}
