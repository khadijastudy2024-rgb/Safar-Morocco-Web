import { Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { ApiService } from '../../core/services/api.service';

@Component({
    standalone: false,
    selector: 'app-activity-log-list',
    templateUrl: './activity-log-list.component.html',
    styleUrls: ['./activity-log-list.component.css']
})
export class ActivityLogListComponent implements OnInit {
    displayedColumns: string[] = ['timestamp', 'action', 'entityType', 'performedBy', 'status', 'description'];
    dataSource: MatTableDataSource<any>;

    @ViewChild(MatPaginator) paginator!: MatPaginator;
    @ViewChild(MatSort) sort!: MatSort;

    constructor(private apiService: ApiService) {
        this.dataSource = new MatTableDataSource();
    }

    ngOnInit(): void {
        this.loadLogs();
    }

    loadLogs() {
        this.apiService.getAuditLogs().subscribe({
            next: (data: any[]) => {
                this.dataSource.data = data;
                this.dataSource.paginator = this.paginator;
                this.dataSource.sort = this.sort;
            },
            error: (err: any) => console.error(err)
        });
    }

    applyFilter(event: Event) {
        const filterValue = (event.target as HTMLInputElement).value;
        this.dataSource.filter = filterValue.trim().toLowerCase();
    }
}
