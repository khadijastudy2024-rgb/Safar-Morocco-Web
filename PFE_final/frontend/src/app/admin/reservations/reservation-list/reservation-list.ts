import { Component, OnInit, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ReservationService } from '../../../core/services/reservation.service';
import { ApiService } from '../../../core/services/api.service';
import { OfferReservation, ReservationStatus } from '../../../core/models/reservation.model';
import { User } from '../../../core/models/user.model';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-reservation-list',
  standalone: false,
  templateUrl: './reservation-list.html',
  styleUrl: './reservation-list.css'
})
export class ReservationList implements OnInit {
  displayedColumns: string[] = ['offerName', 'userName', 'dates', 'quantity', 'totalPrice', 'status', 'actions'];
  dataSource!: MatTableDataSource<OfferReservation>;
  users: Map<number, string> = new Map();

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private reservationService: ReservationService,
    private apiService: ApiService,
    private snackBar: MatSnackBar,
    private translate: TranslateService
  ) { }

  ngOnInit(): void {
    this.loadUsersAndReservations();
  }

  loadUsersAndReservations(): void {
    this.apiService.getAllUsers().subscribe({
      next: (users: User[]) => {
        users.forEach(u => this.users.set(u.id!, u.nom));
        this.loadReservations();
      },
      error: (err: any) => {
        console.error('Error loading users', err);
        this.loadReservations(); // Load anyway
      }
    });
  }

  loadReservations(): void {
    this.reservationService.getAllReservations().subscribe({
      next: (data: OfferReservation[]) => {
        this.dataSource = new MatTableDataSource(data);
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
      },
      error: (err: any) => {
        this.snackBar.open(
          this.translate.instant('ADMIN.RESERVATIONS.ERROR_LOADING'),
          this.translate.instant('COMMON.CLOSE'),
          { duration: 3000 }
        );
      }
    });
  }

  getUserName(userId: number): string {
    return this.users.get(userId) || this.translate.instant('ADMIN.RESERVATIONS.UNKNOWN_USER');
  }

  approveReservation(reservation: OfferReservation): void {
    const confirmMsg = this.translate.instant('ADMIN.RESERVATIONS.APPROVE_CONFIRM', {
      name: this.getUserName(reservation.userId)
    });
    if (confirm(confirmMsg)) {
      this.reservationService.updateReservationStatus(reservation.id!, ReservationStatus.APPROVED).subscribe({
        next: () => {
          this.snackBar.open(
            this.translate.instant('ADMIN.RESERVATIONS.APPROVE_SUCCESS'),
            this.translate.instant('COMMON.CLOSE'),
            { duration: 3000 }
          );
          this.loadReservations();
        },
        error: (err: any) => {
          this.snackBar.open(
            this.translate.instant('ADMIN.RESERVATIONS.APPROVE_ERROR'),
            this.translate.instant('COMMON.CLOSE'),
            { duration: 3000 }
          );
        }
      });
    }
  }

  rejectReservation(reservation: OfferReservation): void {
    const confirmMsg = this.translate.instant('ADMIN.RESERVATIONS.REJECT_CONFIRM', {
      name: this.getUserName(reservation.userId)
    });
    if (confirm(confirmMsg)) {
      this.reservationService.updateReservationStatus(reservation.id!, ReservationStatus.REJECTED).subscribe({
        next: () => {
          this.snackBar.open(
            this.translate.instant('ADMIN.RESERVATIONS.REJECT_SUCCESS'),
            this.translate.instant('COMMON.CLOSE'),
            { duration: 3000 }
          );
          this.loadReservations();
        },
        error: (err: any) => {
          this.snackBar.open(
            this.translate.instant('ADMIN.RESERVATIONS.REJECT_ERROR'),
            this.translate.instant('COMMON.CLOSE'),
            { duration: 3000 }
          );
        }
      });
    }
  }
}
