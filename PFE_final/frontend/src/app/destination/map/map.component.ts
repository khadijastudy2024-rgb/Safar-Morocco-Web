import { Component, OnInit, AfterViewInit, OnDestroy, NgZone } from '@angular/core';
import * as L from 'leaflet';
// import 'leaflet.markercluster'; // Loaded via angular.json scripts
// import 'leaflet-routing-machine'; // Loaded via angular.json scripts
import { DestinationService } from '../../core/services/destination.service';
import { Destination } from '../../core/models/destination.model';

@Component({
    selector: 'app-global-map',
    standalone: false,
    templateUrl: './map.component.html',
    styleUrls: ['./map.component.css'],
})
export class DestinationMapComponent implements OnInit, AfterViewInit, OnDestroy {
    private map!: L.Map;
    private markerClusterGroup!: any;
    private userMarker: L.Marker | null = null;
    public routingControl: any = null;
    private markers: Map<number, L.Marker> = new Map();

    destinations: Destination[] = [];
    filteredDestinations: Destination[] = [];

    // Categories with metadata
    categories = [
        { id: 'City', icon: 'bi-building', label: 'City', color: '#3b82f6' },
        { id: 'Nature', icon: 'bi-tree', label: 'Nature', color: '#10b981' },
        { id: 'Culture', icon: 'bi-bank', label: 'Culture', color: '#8b5cf6' },
        { id: 'Beach', icon: 'bi-water', label: 'Beach', color: '#eab308' },
        { id: 'Desert', icon: 'bi-sun', label: 'Desert', color: '#f97316' },
        { id: 'Mountain', icon: 'bi-cloud-fog', label: 'Mountain', color: '#64748b' }
    ];

    selectedCategory: string | null = null;
    searchQuery: string = '';
    selectedDestination: Destination | null = null;
    isLoading: boolean = true;
    isLocating: boolean = false;

    // Route Info
    routeInfo: { distance: string, time: string } | null = null;

    constructor(
        private destinationService: DestinationService,
        private ngZone: NgZone
    ) { }

    ngOnInit(): void {
        this.loadDestinations();
    }

    ngAfterViewInit(): void {
        this.initMap();
    }

    ngOnDestroy(): void {
        if (this.map) {
            this.map.remove();
        }
    }

    loadDestinations(): void {
        this.isLoading = true;
        this.destinationService.getAllDestinations().subscribe({
            next: (data) => {
                this.destinations = data;
                this.applyFilters();
                this.isLoading = false;
            },
            error: (err: any) => {
                console.error('Failed to load destinations', err);
                this.isLoading = false;
            }
        });
    }

    private initMap(): void {
        // Center of Morocco
        this.map = L.map('global-map', {
            zoomControl: false, // We'll add custom controls if needed or use default in different position
            attributionControl: false
        }).setView([31.7917, -7.0926], 6);

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; OpenStreetMap contributors'
        }).addTo(this.map);

        // Add zoom control to top-right to avoid conflict with our UI
        L.control.zoom({ position: 'topright' }).addTo(this.map);

        this.map.on('locationfound', (e: L.LocationEvent) => {
            this.isLocating = false;
            if (this.userMarker) {
                this.userMarker.setLatLng(e.latlng);
            } else {
                this.createUserMarker(e.latlng);
            }
            this.map.flyTo(e.latlng, 12);
        });

        this.map.on('locationerror', (e) => {
            this.isLocating = false;
            console.error('Geolocation error:', e.message);
            alert('Could not access your location.');
        });

        // Clicking map background clears selection
        this.map.on('click', () => {
            this.selectedDestination = null;
            if (this.routingControl) {
                this.map.removeControl(this.routingControl);
                this.routingControl = null;
                this.routeInfo = null;
            }
        });

        this.updateMarkers();
    }

    private createUserMarker(latlng: L.LatLng): void {
        this.userMarker = L.marker(latlng, {
            icon: L.divIcon({
                className: 'user-location-marker',
                html: '<div class="pulse"></div>',
                iconSize: [24, 24],
                iconAnchor: [12, 12]
            })
        }).addTo(this.map);
    }

    toggleCategory(category: string): void {
        // Toggle selection (radio behavior, or clear if clicking same)
        if (this.selectedCategory === category) {
            this.selectedCategory = null;
        } else {
            this.selectedCategory = category;
        }
        this.applyFilters();
    }

    onSearch(query: string): void {
        this.searchQuery = query;
        this.applyFilters();
    }

    applyFilters(): void {
        let temp = this.destinations;

        // 1. Filter by Category
        if (this.selectedCategory) {
            temp = temp.filter(d => d.categorie === this.selectedCategory);
        }

        // 2. Filter by Search Query (Name or City)
        if (this.searchQuery && this.searchQuery.trim() !== '') {
            const lowerQuery = this.searchQuery.toLowerCase();
            temp = temp.filter(d =>
                (d.nom && d.nom.toLowerCase().includes(lowerQuery)) ||
                (d.type && d.type.toLowerCase().includes(lowerQuery))
            );
        }

        this.filteredDestinations = temp;
        this.updateMarkers();
    }

    locateMe(): void {
        this.isLocating = true;
        this.map.locate({ setView: false, maxZoom: 16 });
    }

    private updateMarkers(): void {
        if (!this.map) return;

        // Clear existing clusters
        if (this.markerClusterGroup) {
            this.map.removeLayer(this.markerClusterGroup);
        }

        // Re-init cluster group
        this.markerClusterGroup = (L as any).markerClusterGroup({
            showCoverageOnHover: false,
            spiderfyOnMaxZoom: true,
            zoomToBoundsOnClick: true,
            animate: true,
            maxClusterRadius: 50
        });

        this.markers.clear();

        this.filteredDestinations.forEach(dest => {
            if (dest.latitude && dest.longitude && dest.id) {
                const marker = L.marker([dest.latitude, dest.longitude], {
                    icon: this.createCustomIcon(dest.categorie)
                });

                // On hover -> Show Preview Card (Temporary)
                marker.on('mouseover', (e) => {
                    if (!this.selectedDestination) {
                        this.selectDestination(dest);
                    }
                });

                // On click -> Lock Selection
                marker.on('click', (e) => {
                    L.DomEvent.stopPropagation(e as any);
                    this.ngZone.run(() => {
                        this.selectDestination(dest);
                    });
                    this.map.flyTo([dest.latitude!, dest.longitude!], 14, { duration: 1.5 });
                });

                this.markerClusterGroup.addLayer(marker);
                this.markers.set(dest.id, marker);
            }
        });

        this.map.addLayer(this.markerClusterGroup);

        // Automatically fit bounds if we have filtered results
        if (this.filteredDestinations.length > 0 && this.map) {
            const bounds = L.latLngBounds(this.filteredDestinations.map(d => [d.latitude!, d.longitude!]));
            this.map.fitBounds(bounds, { padding: [50, 50], maxZoom: 12 });
        }
    }

    private createCustomIcon(category: string): L.DivIcon {
        const catObj = this.categories.find(c => c.id === category) || this.categories.find(c => c.id === 'City')!;
        const color = catObj ? catObj.color : '#3b82f6';
        const iconClass = catObj ? catObj.icon : 'bi-geo-alt-fill';

        const html = `
            <div class="custom-map-marker" style="background-color: ${color}">
                <i class="bi ${iconClass}"></i>
            </div>
            <div class="marker-shadow"></div>
        `;

        return L.divIcon({
            className: 'custom-div-icon',
            html: html,
            iconSize: [40, 48],
            iconAnchor: [20, 48],
            popupAnchor: [0, -48]
        });
    }

    selectDestination(dest: Destination): void {
        this.selectedDestination = dest;
        // Optionally clear routing when selecting new destination
        if (this.routingControl) {
            this.map.removeControl(this.routingControl);
            this.routingControl = null;
            this.routeInfo = null;
        }
    }

    closePreview(): void {
        this.selectedDestination = null;
    }

    getDirections(): void {
        if (!this.selectedDestination || !this.selectedDestination.latitude || !this.selectedDestination.longitude) return;

        // Reset
        if (this.routingControl) {
            this.map.removeControl(this.routingControl);
        }

        // We need user location for routing
        if (!this.userMarker) {
            this.locateMe();
            // Listen once for location found then route
            this.map.once('locationfound', () => this.getDirections());
            return;
        }

        const userLatLng = this.userMarker.getLatLng();
        const destLatLng = L.latLng(this.selectedDestination.latitude!, this.selectedDestination.longitude!);

        this.routingControl = (L as any).Routing.control({
            waypoints: [
                userLatLng,
                destLatLng
            ],
            router: (L as any).Routing.osrmv1({
                serviceUrl: 'https://router.project-osrm.org/route/v1'
            }),
            lineOptions: {
                styles: [{ color: '#f59e0b', weight: 6, opacity: 0.8 }]
            },
            fitSelectedRoutes: true,
            showAlternatives: false,
            // Custom summary container
            createMarker: () => null // Hide default markers
        })
            .on('routesfound', (e: any) => {
                const routes = e.routes;
                if (routes && routes.length > 0) {
                    const summary = routes[0].summary;
                    // Convert time (seconds) to human readable
                    const minutes = Math.round(summary.totalTime / 60);
                    const hours = Math.floor(minutes / 60);
                    const mins = minutes % 60;
                    const timeStr = hours > 0 ? `${hours}h ${mins}min` : `${mins} min`;

                    // Convert distance (meters) to km
                    const distKm = (summary.totalDistance / 1000).toFixed(1);

                    this.routeInfo = {
                        distance: `${distKm} km`,
                        time: timeStr
                    };
                }
            })
            .addTo(this.map);

        // Hide default container
        const container = document.querySelector('.leaflet-routing-container');
        if (container) {
            container.classList.add('d-none');
        }
    }
}

