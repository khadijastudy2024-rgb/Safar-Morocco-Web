import { Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { ApiService } from '../../core/services/api.service';
import { DestinationDialogComponent } from '../../destination/dialog/destination-dialog.component';
import { TranslateService } from '@ngx-translate/core';

@Component({
    standalone: false,
    selector: 'app-admin-destination-list',
    templateUrl: './destination-list.component.html',
    styleUrls: ['./destination-list.component.css']
})
export class AdminDestinationListComponent implements OnInit {
    displayedColumns: string[] = ['id', 'image', 'name', 'city', 'category', 'status', 'actions'];
    dataSource: MatTableDataSource<any>;

    @ViewChild(MatPaginator) paginator!: MatPaginator;
    @ViewChild(MatSort) sort!: MatSort;

    constructor(
        private readonly apiService: ApiService,
        private readonly dialog: MatDialog,
        private readonly translate: TranslateService
    ) {
        this.dataSource = new MatTableDataSource();
    }

    ngOnInit(): void {
        this.loadDestinations();
    }

    loadDestinations() {
        this.apiService.getDestinations().subscribe((data: any[]) => {
            this.dataSource.data = data;
            this.dataSource.paginator = this.paginator;
            this.dataSource.sort = this.sort;
        });
    }

    applyFilter(event: Event) {
        const filterValue = (event.target as HTMLInputElement).value;
        this.dataSource.filter = filterValue.trim().toLowerCase();
    }

    openDialog(destination?: any) {
        const dialogRef = this.dialog.open(DestinationDialogComponent, {
            width: '600px',
            data: destination || {}
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.loadDestinations();
            }
        });
    }

    deleteDestination(id: number) {
        if (confirm(this.translate.instant('DESTINATIONS.DETAIL.NOTIFICATIONS.DELETE_CONFIRM'))) {
            this.apiService.deleteDestination(id).subscribe(() => {
                this.loadDestinations();
            });
        }
    }

    exportCSV() {
        const rows = this.dataSource.data;
        if (!rows || rows.length === 0) return;

        const headers = [
            this.translate.instant('ADMIN.DESTINATIONS.COLS.ID'),
            this.translate.instant('ADMIN.DESTINATIONS.COLS.NAME'),
            this.translate.instant('ADMIN.DESTINATIONS.COLS.REGION'),
            this.translate.instant('ADMIN.DESTINATIONS.COLS.CATEGORY'),
            this.translate.instant('ADMIN.DESTINATIONS.COLS.STATUS')
        ];

        const csvRows = rows.map(dest => [
            dest.id,
            `"${(dest.nom || '').replaceAll('"', '""')}"`,
            `"${(dest.type || '').replaceAll('"', '""')}"`,
            `"${(dest.categorie || '').replaceAll('"', '""')}"`,
            this.translate.instant('ADMIN.DESTINATIONS.STATUS_ACTIVE')
        ].join(','));

        const csvContent = [headers.join(','), ...csvRows].join('\n');
        const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
        const url = URL.createObjectURL(blob);

        const link = document.createElement('a');
        link.href = url;
        link.download = `destinations_${new Date().toISOString().slice(0, 10)}.csv`;
        link.click();
        URL.revokeObjectURL(url);
    }
}
