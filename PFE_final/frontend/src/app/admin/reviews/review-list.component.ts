import { Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService } from '../../core/services/api.service';
import { TranslateService } from '@ngx-translate/core';

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

    constructor(
        private apiService: ApiService,
        private snackBar: MatSnackBar,
        private translate: TranslateService
    ) {
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
        if (confirm(this.translate.instant('ADMIN.REVIEWS.DELETE_CONFIRM'))) {
            this.apiService.deleteReview(id).subscribe({
                next: () => {
                    this.snackBar.open(
                        this.translate.instant('ADMIN.REVIEWS.DELETE_SUCCESS'),
                        this.translate.instant('COMMON.CLOSE'),
                        { duration: 3000 }
                    );
                    this.loadReviews();
                },
                error: () => this.snackBar.open(
                    this.translate.instant('ADMIN.REVIEWS.DELETE_ERROR'),
                    this.translate.instant('COMMON.CLOSE'),
                    { duration: 3000 }
                )
            });
        }
    }

    moderateReview(id: number, status: string) {
        this.apiService.moderateReview(id, status).subscribe({
            next: () => {
                const statusLabel = status === 'APPROVED' ? 'Approved' : 'Rejected'; // We could localize labels too
                this.snackBar.open(
                    this.translate.instant('ADMIN.REVIEWS.UPDATE_SUCCESS'),
                    this.translate.instant('COMMON.CLOSE'),
                    { duration: 3000 }
                );
                this.loadReviews();
            },
            error: () => this.snackBar.open(
                this.translate.instant('ADMIN.REVIEWS.UPDATE_ERROR'),
                this.translate.instant('COMMON.CLOSE'),
                { duration: 3000 }
            )
        });
    }
}
