import { Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialog } from '@angular/material/dialog';
import { ApiService } from '../../core/services/api.service';
import { DestinationDialogComponent } from '../../destination/dialog/destination-dialog.component';

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

    constructor(private apiService: ApiService, private dialog: MatDialog) {
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
        if (confirm('Are you sure?')) {
            this.apiService.deleteDestination(id).subscribe(() => {
                this.loadDestinations();
            });
        }
    }
}
