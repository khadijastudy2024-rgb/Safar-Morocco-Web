import { Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService } from '../../core/services/api.service';

@Component({
    standalone: false,
    selector: 'app-review-list',
    templateUrl: './review-list.component.html',
    styleUrls: ['./review-list.component.css']
})
export class ReviewListComponent implements OnInit {
    displayedColumns: string[] = ['id', 'user', 'destination', 'note', 'commentaire', 'status', 'actions'];
    dataSource: MatTableDataSource<any>;

    @ViewChild(MatPaginator) paginator!: MatPaginator;
    @ViewChild(MatSort) sort!: MatSort;

    constructor(private apiService: ApiService, private snackBar: MatSnackBar) {
        this.dataSource = new MatTableDataSource();
    }

    ngOnInit(): void {
        this.loadReviews();
    }

    loadReviews() {
        this.apiService.getAllReviews().subscribe({
            next: (data: any[]) => {
                this.dataSource.data = data;
                this.dataSource.paginator = this.paginator;
                this.dataSource.sort = this.sort;
            },
            error: (err) => console.error(err)
        });
    }

    applyFilter(event: Event) {
        const filterValue = (event.target as HTMLInputElement).value;
        this.dataSource.filter = filterValue.trim().toLowerCase();
    }

    deleteReview(id: number) {
        if (confirm('Are you sure you want to delete this review?')) {
            this.apiService.deleteReview(id).subscribe({
                next: () => {
                    this.snackBar.open('Review deleted', 'Close', { duration: 3000 });
                    this.loadReviews();
                },
                error: () => this.snackBar.open('Error deleting review', 'Close', { duration: 3000 })
            });
        }
    }

    moderateReview(id: number, status: string) {
        this.apiService.moderateReview(id, status).subscribe({
            next: () => {
                this.snackBar.open(`Review ${status.toLowerCase()}`, 'Close', { duration: 3000 });
                this.loadReviews();
            },
            error: () => this.snackBar.open('Error updating review status', 'Close', { duration: 3000 })
        });
    }
}
