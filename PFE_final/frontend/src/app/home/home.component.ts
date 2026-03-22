import { Component, OnInit, AfterViewInit, NgZone, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DestinationService } from '../core/services/destination.service';
import { ApiService } from '../core/services/api.service';
import { AuthService } from '../core/services/auth.service';
import { Destination } from '../core/models/destination.model';
import * as L from 'leaflet';

@Component({
    standalone: false,
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit, AfterViewInit {
    featuredDestinations: Destination[] = [];
    upcomingEvents: any[] = [];
    loading = true;
    searchQuery: string = '';
    private map!: L.Map;

    public get isLoggedIn(): boolean {
        return this.authService.isLoggedIn;
    }

    constructor(
        private destinationService: DestinationService,
        private apiService: ApiService,
        private authService: AuthService,
        private router: Router,
        private snackBar: MatSnackBar,
        private ngZone: NgZone,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.loading = true;

        // Fetch Destinations
        this.destinationService.getAllDestinations().subscribe({
            next: (data) => {
                this.ngZone.run(() => {
                    this.featuredDestinations = (data || []).slice(0, 3);
                    this.checkLoading();
                    this.updateMapMarkers();
                    this.cdr.detectChanges();
                });
            },
            error: (e) => {
                this.ngZone.run(() => {
                    console.error('❌ HOME: Error fetching destinations', e);
                    this.checkLoading();
                    this.cdr.detectChanges();
                });
            }
        });

        // Fetch Events
        this.apiService.getEvents().subscribe({
            next: (data) => {
                this.ngZone.run(() => {
                    this.upcomingEvents = (data || []).slice(0, 3);
                    this.checkLoading();
                    this.cdr.detectChanges();
                });
            },
            error: (e) => {
                this.ngZone.run(() => {
                    console.error('❌ HOME: Error fetching events', e);
                    this.checkLoading();
                    this.cdr.detectChanges();
                });
            }
        });
    }

    ngAfterViewInit(): void {
        this.initMap();
    }

    private initMap(): void {
        const mapContainer = document.getElementById('mini-map');
        if (!mapContainer) return;

        this.map = L.map('mini-map', {
            zoomControl: false,
            scrollWheelZoom: false,
            dragging: false // Keep it static-like but interactive on click? Or allow dragging? User said "interactive". Let's allow dragging but disable scroll.
        }).setView([31.7917, -7.0926], 5);

        // Re-enable dragging
        this.map.dragging.enable();

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; OpenStreetMap contributors'
        }).addTo(this.map);

        // Fix Leaflet Icon
        const defaultIcon = L.icon({
            iconUrl: 'assets/marker-icon.png',
            iconRetinaUrl: 'assets/marker-icon-2x.png',
            shadowUrl: 'assets/marker-shadow.png',
            iconSize: [25, 41],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34],
            shadowSize: [41, 41]
        });
        L.Marker.prototype.options.icon = defaultIcon;

        this.updateMapMarkers();
    }

    private updateMapMarkers(): void {
        if (!this.map || !this.featuredDestinations.length) return;

        this.featuredDestinations.forEach(dest => {
            if (dest.latitude && dest.longitude) {
                L.marker([dest.latitude, dest.longitude])
                    .addTo(this.map)
                    .bindPopup(`<b>${dest.nom}</b><br>${dest.type}`);
            }
        });
    }

    checkLoading() {
        if (this.featuredDestinations && this.upcomingEvents) {
            this.loading = false;
        }
    }

    onSearch() {
        if (this.searchQuery.trim()) {
            this.router.navigate(['/destinations'], { queryParams: { search: this.searchQuery } });
        }
    }

    openChat() {
        const chatTrigger = document.querySelector('.chatbot-trigger') as HTMLElement;
        if (chatTrigger) {
            chatTrigger.click();
        }
    }

    /**
     * Rediriger vers la création d'itinéraire
     * Appelé depuis le bouton "Planifier un voyage" sur la homepage
     */
    startPlanning() {
        if (!this.authService.isLoggedIn) {
            this.snackBar.open('Veuillez vous connecter pour planifier un voyage', 'Fermer', { duration: 5000 });
            this.router.navigate(['/auth/login']);
            return;
        }
        this.router.navigate(['/itineraires/create']);
    }

    handleImageError(event: any) {
        event.target.src = 'assets/placeholder.jpg';
    }
}
