import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { ItineraryService } from '../../core/services/itinerary.service';
import { AuthService } from '../../core/services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TranslateService } from '@ngx-translate/core';

@Component({
    selector: 'app-itinerary-list',
    templateUrl: './itinerary-list.component.html',
    styleUrls: ['./itinerary-list.component.css'],
    standalone: false
})
export class ItineraryListComponent implements OnInit {
    itineraries: any[] = [];
    loading = true;
    currentUser: any;

    constructor(
        private router: Router,
        private itineraryService: ItineraryService,
        private authService: AuthService,
        private snackBar: MatSnackBar,
        private cdr: ChangeDetectorRef,
        public translateService: TranslateService
    ) { }

    ngOnInit(): void {
        console.log('🔍 ItineraryListComponent initialized');
        this.currentUser = this.authService.currentUserValue;
        console.log('🔍 Current user:', this.currentUser);
        
        // Debug translation service
        console.log('🔍 Current language:', this.translateService.currentLang);
        this.translateService.get('ITINERARY.HERO_TITLE').subscribe(translation => {
            console.log('🔍 Translation test:', translation);
        });
        
        this.loadItineraries();
    }

    loadItineraries(): void {
        if (!this.currentUser) {
            console.log('🔍 No user found');
            this.loading = false;
            return;
        }

        console.log('🔍 Loading itineraries for user:', this.currentUser.id);

        this.itineraryService.getItinerairesUtilisateur(this.currentUser.id).subscribe({
            next: (itineraries) => {
                this.itineraries = itineraries || [];
                this.loading = false;
                console.log('🔍 Itineraries loaded:', itineraries);
                this.cdr.detectChanges();
            },
            error: (err) => {
                console.error('🔍 Error loading itineraries:', err);
                this.snackBar.open('Erreur lors du chargement des itinéraires', 'Fermer', {
                    duration: 3000
                });
                this.loading = false;
                this.cdr.detectChanges();
            }
        });
    }

    viewItineraryDetail(id: number): void {
        this.router.navigate(['/itineraries/detail', id]);
    }

    createNewItinerary(): void {
        this.router.navigate(['/itineraries/create']);
    }

    goBack(): void {
        this.router.navigate(['/destinations']);
    }

    getDurationValue(duration: string): string {
        if (!duration) return '';
        const match = duration.match(/[\d.]+/);
        return match ? match[0] : '';
    }

    getDurationUnitKey(duration: string): string {
        if (!duration) return 'ITINERARY.NOT_SPECIFIED';
        duration = duration.toLowerCase();
        if (duration.includes('heures') || duration.includes('hours')) return 'ITINERARY.HOURS';
        if (duration.includes('heure') || duration.includes('hour')) return 'ITINERARY.HOUR';
        if (duration.includes('jours') || duration.includes('days')) return 'ITINERARY.DAYS';
        if (duration.includes('jour') || duration.includes('day')) return 'ITINERARY.DAY';
        return 'ITINERARY.NOT_SPECIFIED';
    }

    formatDate(dateValue: any): Date | string {
        if (!dateValue) return '';
        // Handle array format [year, month, day, hour, min, sec] usually returned by Spring
        if (Array.isArray(dateValue) && dateValue.length >= 3) {
            const [year, month, day, hour = 0, min = 0, sec = 0] = dateValue;
            return new Date(year, month - 1, day, hour, min, sec);
        }
        return dateValue;
    }

    getDestinationCount(destinations: any[]): number {
        return destinations ? destinations.length : 0;
    }

    getOptimizedCount(): number {
        return this.itineraries ? this.itineraries.filter(it => it.estOptimise).length : 0;
    }

    getTotalDistance(): number {
        if (!this.itineraries) return 0;
        return Math.round(this.itineraries.reduce((total, it) => total + (it.distanceTotale || 0), 0) * 100) / 100;
    }
}
