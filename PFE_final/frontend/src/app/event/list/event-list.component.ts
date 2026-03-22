import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { ApiService } from '../../core/services/api.service';

@Component({
    standalone: false,
    selector: 'app-event-list',
    templateUrl: './event-list.component.html',
    styleUrls: ['./event-list.component.css']
})
export class EventListComponent implements OnInit {
    events: any[] = [];
    loading = true;
    searchTerm: string = '';
    locationTerm: string = '';
    selectedCategory: string = 'All Events';
    categories = ['All Events', 'Festivals', 'Cultural', 'Music', 'Traditional'];

    filteredEvents: any[] = []; // Explicit array instead of getter

    constructor(
        private apiService: ApiService,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.loading = true;
        this.apiService.getEvents().subscribe({
            next: (data) => {
                // Parse date arrays from backend so the Angular date pipe doesn't crash
                this.events = data.map((evt: any) => ({
                    ...evt,
                    dateDebut: this.parseDate(evt.dateDebut),
                    dateFin: this.parseDate(evt.dateFin)
                }));
                this.applyFilters(); // Initial filter application
                this.loading = false;
                this.cdr.detectChanges(); // Force update
            },
            error: (e) => {
                console.error(e);
                this.loading = false;
                this.cdr.detectChanges();
            }
        });
    }

    private parseDate(dateStr: any): Date | null {
        if (!dateStr) return null;
        if (Array.isArray(dateStr)) {
            const [y, m, d, h = 0, min = 0, s = 0] = dateStr;
            return new Date(y, m - 1, d, h, min, s);
        }
        const parsed = new Date(dateStr);
        if (isNaN(parsed.getTime()) && typeof dateStr === 'string') {
            return new Date(dateStr.replace('T', ' ').replace(/\.\d+/, ''));
        }
        return parsed;
    }

    applyFilters() {
        const search = (this.searchTerm || '').trim().toLowerCase();
        const location = (this.locationTerm || '').trim().toLowerCase();
        const selectedCat = this.selectedCategory || 'All Events';

        // Map display label → backend eventType value
        const categoryMap: { [key: string]: string } = {
            'Festivals':    'festival',
            'Cultural':     'cultural',
            'Music':        'music',
            'Traditional':  'traditional',
        };
        const targetType = categoryMap[selectedCat]; // undefined when 'All Events'

        this.filteredEvents = this.events.filter(event => {
            const matchesSearch = !search || 
                (event.nom && event.nom.toLowerCase().includes(search)) ||
                (event.description && event.description.toLowerCase().includes(search));

            const matchesLocation = !location || 
                (event.lieu && event.lieu.toLowerCase().includes(location));

            let matchesCategory = true;
            if (targetType) {
                const eventType = (event.eventType || '').toLowerCase();
                matchesCategory = eventType === targetType;
            }

            return matchesSearch && matchesLocation && matchesCategory;
        });

        this.cdr.detectChanges();
    }

    filterCategory(category: string) {
        this.selectedCategory = category;
        this.applyFilters();
    }

    // Search trigger
    onSearch() {
        this.applyFilters();
    }

    resetFilters() {
        this.searchTerm = '';
        this.locationTerm = '';
        this.selectedCategory = 'All Events';
        this.applyFilters();
    }
}
