import { Component, OnInit, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ReservationService } from '../../../core/services/reservation.service';
import { ApiService } from '../../../core/services/api.service';
import { OfferReservation, ReservationStatus } from '../../../core/models/reservation.model';
import { User } from '../../../core/models/user.model';

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
    private snackBar: MatSnackBar
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
        this.snackBar.open('Error loading reservations', 'Close', { duration: 3000 });
      }
    });
  }

  getUserName(userId: number): string {
    return this.users.get(userId) || 'Unknown User';
  }

  approveReservation(reservation: OfferReservation): void {
    if (confirm(`Approve reservation for ${this.getUserName(reservation.userId)}?`)) {
      this.reservationService.updateReservationStatus(reservation.id!, ReservationStatus.APPROVED).subscribe({
        next: () => {
          this.snackBar.open('Reservation approved successfully', 'Close', { duration: 3000 });
          this.loadReservations();
        },
        error: (err: any) => {
          this.snackBar.open('Error approving reservation', 'Close', { duration: 3000 });
        }
      });
    }
  }

  rejectReservation(reservation: OfferReservation): void {
    if (confirm(`Reject reservation for ${this.getUserName(reservation.userId)}?`)) {
      this.reservationService.updateReservationStatus(reservation.id!, ReservationStatus.REJECTED).subscribe({
        next: () => {
          this.snackBar.open('Reservation rejected successfully', 'Close', { duration: 3000 });
          this.loadReservations();
        },
        error: (err: any) => {
          this.snackBar.open('Error rejecting reservation', 'Close', { duration: 3000 });
        }
      });
    }
  }
}
