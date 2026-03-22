import { Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService } from '../../core/services/api.service';
import { EventDialogComponent } from './event-dialog.component';
import { TranslateService } from '@ngx-translate/core';

@Component({
    standalone: false,
    selector: 'app-admin-event-list',
    templateUrl: './event-list.component.html',
    styleUrls: ['./event-list.component.css']
})
export class AdminEventListComponent implements OnInit {
    displayedColumns: string[] = ['image', 'title', 'date', 'location', 'price', 'actions'];
    dataSource: MatTableDataSource<any>;

    @ViewChild(MatPaginator) paginator!: MatPaginator;
    @ViewChild(MatSort) sort!: MatSort;

    constructor(
        private apiService: ApiService,
        private dialog: MatDialog,
        private snackBar: MatSnackBar,
        private translate: TranslateService
    ) {
        this.dataSource = new MatTableDataSource();
    }

    ngOnInit(): void {
        this.loadEvents();
    }

    loadEvents() {
        this.apiService.getEvents().subscribe((data: any[]) => {
            this.dataSource.data = data;
            this.dataSource.paginator = this.paginator;
            this.dataSource.sort = this.sort;
        });
    }

    applyFilter(event: Event) {
        const filterValue = (event.target as HTMLInputElement).value;
        this.dataSource.filter = filterValue.trim().toLowerCase();

        if (this.dataSource.paginator) {
            this.dataSource.paginator.firstPage();
        }
    }

    openDialog(event?: any) {
        const dialogRef = this.dialog.open(EventDialogComponent, {
            width: '600px',
            data: event || {}
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.loadEvents();
            }
        });
    }

    deleteEvent(id: number) {
        if (confirm(this.translate.instant('EVENTS.DETAIL.DELETE_CONFIRM'))) {
            this.apiService.deleteEvent(id).subscribe({
                next: () => {
                    this.snackBar.open(
                        this.translate.instant('EVENTS.DETAIL.DELETE_SUCCESS'),
                        this.translate.instant('COMMON.CLOSE'),
                        { duration: 3000 }
                    );
                    this.loadEvents();
                },
                error: (err) => {
                    this.snackBar.open(
                        this.translate.instant('EVENTS.DETAIL.DELETE_ERROR'),
                        this.translate.instant('COMMON.CLOSE'),
                        { duration: 3000 }
                    );
                }
            });
        }
    }
}
