import { Component, OnInit, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { OfferService } from '../../../core/services/offer.service';
import { DestinationService } from '../../../core/services/destination.service';
import { Offer } from '../../../core/models/offer.model';
import { Destination } from '../../../core/models/destination.model';
import { OfferDialogComponent } from '../offer-dialog/offer-dialog';

@Component({
  selector: 'app-offer-list',
  standalone: false,
  templateUrl: './offer-list.html',
  styleUrl: './offer-list.css',
})
export class OfferList implements OnInit {
  displayedColumns: string[] = ['name', 'type', 'destination', 'price', 'status', 'actions'];
  dataSource!: MatTableDataSource<Offer>;
  destinations: Destination[] = [];

  // Filters
  selectedDestination: number | null = null;
  selectedType: string = '';
  selectedStatus: string = ''; // 'all', 'active', 'deleted'

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private offerService: OfferService,
    private destinationService: DestinationService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    this.loadDestinations();
    this.loadOffers();
  }

  loadDestinations() {
    this.destinationService.getAllDestinations().subscribe({
      next: (data: Destination[]) => {
        this.destinations = data;
        // Trigger a change detection/filter re-apply if needed
        if (this.dataSource) this.applyFilters();
      },
      error: (err: any) => console.error('Failed to load destinations', err)
    });
  }

  loadOffers() {
    this.offerService.getAllOffers().subscribe({
      next: (data: Offer[]) => {
        this.dataSource = new MatTableDataSource(data);
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;

        // Custom filter logic
        this.dataSource.filterPredicate = (data: Offer, filter: string): boolean => {
          const searchTerms = JSON.parse(filter);
          let matchesDest = searchTerms.destinationId === null || data.destinationId === searchTerms.destinationId;
          let matchesType = searchTerms.type === '' || data.type === searchTerms.type;
          let matchesStatus = searchTerms.status === '' || searchTerms.status === 'all' ||
            (searchTerms.status === 'active' && !data.deleted) ||
            (searchTerms.status === 'deleted' && data.deleted);
          return !!(matchesDest && matchesType && matchesStatus);
        };
      },
      error: (err: any) => {
        this.snackBar.open('Error loading offers', 'Close', { duration: 3000 });
      }
    });
  }

  applyFilters() {
    if (!this.dataSource) return;
    const filterValues = {
      destinationId: this.selectedDestination,
      type: this.selectedType,
      status: this.selectedStatus
    };
    this.dataSource.filter = JSON.stringify(filterValues);
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  resetFilters() {
    this.selectedDestination = null;
    this.selectedType = '';
    this.selectedStatus = '';
    this.applyFilters();
  }

  openDialog(offer?: Offer) {
    const dialogRef = this.dialog.open(OfferDialogComponent, {
      width: '600px',
      data: { offer, destinations: this.destinations }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        if (offer && offer.id) {
          this.offerService.updateOffer(offer.id, result).subscribe({
            next: () => {
              this.snackBar.open('Offer updated successfully', 'Close', { duration: 3000 });
              this.loadOffers();
            },
            error: (err: any) => {
              console.error('Update Offer Error:', err);
              this.snackBar.open('Error updating offer', 'Close', { duration: 3000 });
            }
          });
        } else {
          this.offerService.createOffer(result).subscribe({
            next: () => {
              this.snackBar.open('Offer created successfully', 'Close', { duration: 3000 });
              this.loadOffers();
            },
            error: (err: any) => {
              console.error('Create Offer Error:', err);
              this.snackBar.open('Error creating offer', 'Close', { duration: 3000 });
            }
          });
        }
      }
    });
  }

  deleteOffer(offer: Offer) {
    if (confirm(`Are you sure you want to delete ${offer.name}?`)) {
      this.offerService.deleteOffer(offer.id!).subscribe({
        next: () => {
          this.snackBar.open('Offer deleted successfully', 'Close', { duration: 3000 });
          this.loadOffers();
        },
        error: (err: any) => {
          this.snackBar.open('Error deleting offer', 'Close', { duration: 3000 });
        }
      });
    }
  }

  getDestinationName(id: number): string {
    const dest = this.destinations.find(d => d.id === id);
    return dest ? dest.nom : 'Unknown';
  }
}
