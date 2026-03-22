import { Component, OnInit } from '@angular/core';
import { DestinationService } from '../../core/services/destination.service';
import { Destination } from '../../core/models/destination.model';
import { AuthService } from '../../core/services/auth.service';
import { Router, ActivatedRoute } from '@angular/router';
import { ChangeDetectorRef } from '@angular/core';

@Component({
    standalone: false,
    selector: 'app-list',
    templateUrl: './list.component.html',
    styleUrls: ['./list.component.css']
})
export class DestinationListComponent implements OnInit {
    destinations: Destination[] = [];
    filteredDestinations: Destination[] = [];
    loading = true;
    isAdmin = false;
    searchText = '';
    currentFilter = 'All';
    selectedType: string | null = null;

    constructor(
        private destinationService: DestinationService,
        private authService: AuthService,
        private router: Router,
        private route: ActivatedRoute,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.isAdmin = this.authService.isAdmin;
        this.loadDestinations();

        // Listen for query params from Home Page
        this.route.queryParams.subscribe(params => {
            // Reset state
            this.searchText = '';
            this.currentFilter = 'All';

            if (params['search']) {
                this.searchText = params['search'];
            } else if (params['q']) {
                this.searchText = params['q'];
            }
            this.selectedType = params['type'];
            
            // If we have type parameter, resolve the currentFilter
            if (this.selectedType) {
                const lowerType = this.selectedType.toLowerCase();
                if (lowerType.includes('cultur')) this.currentFilter = 'Cultural';
                else if (lowerType.includes('natur')) this.currentFilter = 'Nature';
                else if (lowerType.includes('histor')) this.currentFilter = 'Historical';
                else if (lowerType.includes('religi')) this.currentFilter = 'Religious';
            }

            // Apply filters if data is already loaded
            if (this.destinations.length > 0) {
                this.applyFilters();
                this.cdr.detectChanges();
            }
        });
    }

    loadDestinations() {
        this.loading = true;
        this.destinationService.getAllDestinations().subscribe({
            next: (data) => {
                this.destinations = data || [];
                this.loading = false;
                this.applyFilters();
                this.cdr.detectChanges();
            },
            error: (err: any) => {
                console.error('❌ LIST: Error loading destinations', err);
                console.error('❌ LIST: Status:', err.status);
                this.loading = false;
            }
        });
    }

    applyFilters() {
        let result = this.destinations;

        // Apply search filter (Name)
        if (this.searchText) {
            const lower = this.searchText.toLowerCase();
            result = result.filter(d =>
                (d.nom && d.nom.toLowerCase().includes(lower))
            );
        }

        // Apply type filter (second input)
        if (this.selectedType) {
            const lowerType = this.selectedType.toLowerCase();
            result = result.filter(d =>
                (d.type && d.type.toLowerCase().includes(lowerType))
            );
        }

        // Apply category filter
        if (this.currentFilter !== 'All') {
            result = result.filter(d => d.categorie === this.currentFilter);
        }

        this.filteredDestinations = result;
    }

    onSearch() {
        this.applyFilters();
    }

    onFilter(category: string) {
        this.currentFilter = category;
        this.applyFilters();
    }

    filterDestinationsByType(type: string) {
        if (!type || this.destinations.length === 0) return;

        let category = 'All';
        const lowerType = type.toLowerCase();

        if (lowerType.includes('cultur')) category = 'Cultural';
        else if (lowerType.includes('natur')) category = 'Nature';
        else if (lowerType.includes('histor')) category = 'Historical';
        else if (lowerType.includes('religi')) category = 'Religious';

        this.currentFilter = category;
        this.applyFilters();
    }

    viewDetails(id: number) {
        this.router.navigate(['/destinations', id]);
    }

    createDestination() {
        // To be implemented with Dialog
    }
}
