import { Component, OnInit, AfterViewInit, ChangeDetectorRef, HostListener, NgZone } from '@angular/core';
import * as L from 'leaflet';
import 'leaflet-routing-machine';
import { Destination } from '../../../core/models/destination.model';
import { DestinationService } from '../../../core/services/destination.service';
import { Router } from '@angular/router';

@Component({
    standalone: false,
    selector: 'app-global-map',
    templateUrl: './global-map.component.html',
    styleUrls: ['./global-map.component.css']
})
export class GlobalMapComponent implements OnInit, AfterViewInit {
    private map!: L.Map;
    private markersGroup = L.layerGroup();

    destinations: Destination[] = [];
    filteredDestinations: Destination[] = [];

    searchQuery: string = '';
    selectedCategory: string = 'ALL';
    categories = [
        { id: 'ALL', label: 'All', icon: 'bi-grid-fill' },
        { id: 'CULTURAL', label: 'Cultural', icon: 'bi-mask' },
        { id: 'NATURE', label: 'Nature', icon: 'bi-tree-fill' },
        { id: 'HISTORICAL', label: 'Historical', icon: 'bi-bank' },
        { id: 'RELIGIOUS', label: 'Religious', icon: 'bi-moon-stars' }
    ];

    selectedDestination: Destination | null = null;
    userLocation: L.LatLng | null = null;
    routingControl: any = null;

    // Loading states
    isLoading = false;
    isRouting = false;

    constructor(
        private destinationService: DestinationService,
        private router: Router,
        private cdr: ChangeDetectorRef,
        private ngZone: NgZone
    ) { }

    ngOnInit(): void {
        this.isLoading = true;
        this.destinationService.getAllDestinations().subscribe({
            next: (data) => {
                this.destinations = data;
                this.applyFilters();
                this.isLoading = false;
                this.cdr.detectChanges();
            },
            error: (err) => {
                console.error('Failed to fetch destinations', err);
                this.isLoading = false;
            }
        });
    }

    ngAfterViewInit(): void {
        this.initMap();
        this.locateUser();
    }

    private initMap(): void {
        this.map = L.map('global-map', {
            zoomControl: false // We'll add it in a better position
        }).setView([31.7917, -7.0926], 6);

        L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
            attribution: '&copy; OpenStreetMap &copy; CARTO'
        }).addTo(this.map);

        L.control.zoom({ position: 'bottomright' }).addTo(this.map);
        this.markersGroup.addTo(this.map);

        // Handle clicks on "View Details" button in popups
        this.map.on('popupopen', (e: any) => {
            const popup = e.popup;
            const container = popup.getElement();
            if (container) {
                const btn = container.querySelector('.btn-detail');
                if (btn) {
                    const destId = btn.getAttribute('data-id');
                    L.DomEvent.on(btn, 'click', (event: any) => {
                        L.DomEvent.stop(event);
                        this.ngZone.run(() => {
                            this.router.navigate(['/destinations', destId]);
                        });
                    });
                }
            }
        });
    }

    private addMarkers(): void {
        this.markersGroup.clearLayers();

        this.filteredDestinations.forEach(dest => {
            if (dest.latitude && dest.longitude) {
                const marker = L.marker([dest.latitude, dest.longitude], {
                    icon: this.createCustomIcon(dest.categorie)
                });

                // Get image URL for popup
                let imgUrl = dest.thumbnailUrl || (dest.medias && dest.medias.length > 0 ? dest.medias[0].url : 'assets/placeholder.jpg');
                if (imgUrl && !imgUrl.startsWith('http') && !imgUrl.startsWith('assets') && !imgUrl.startsWith('data:')) {
                    const clean = imgUrl.startsWith('/') ? imgUrl.substring(1) : imgUrl;
                    imgUrl = clean.startsWith('uploads/') ? '/' + clean : '/uploads/' + clean;
                }

                const popupContent = `
                    <div class="map-popup-card">
                        <img src="${imgUrl}" class="popup-img">
                        <div class="popup-info">
                            <h6>${dest.nom}</h6>
                            <p>${dest.type}</p>
                            <button class="btn-detail" data-id="${dest.id}">View Details</button>
                        </div>
                    </div>
                `;

                marker.bindPopup(popupContent, {
                    className: 'custom-popup',
                    maxWidth: 250
                });

                marker.on('click', () => {
                    this.ngZone.run(() => {
                        this.selectedDestination = dest;
                        this.cdr.detectChanges();
                    });
                });

                this.markersGroup.addLayer(marker);
            }
        });
    }

    private createCustomIcon(category: string): L.DivIcon {
        return L.divIcon({
            className: 'custom-marker',
            html: `<div class="marker-pin ${category?.toLowerCase()}"><i class="bi bi-geo-alt-fill"></i></div>`,
            iconSize: [30, 42],
            iconAnchor: [15, 42]
        });
    }

    applyFilters() {
        this.filteredDestinations = this.destinations.filter(dest => {
            const matchesSearch = !this.searchQuery ||
                dest.nom.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
                dest.type?.toLowerCase().includes(this.searchQuery.toLowerCase());

            const matchesCategory = this.selectedCategory === 'ALL' ||
                dest.categorie?.toUpperCase() === this.selectedCategory;

            return matchesSearch && matchesCategory;
        });

        this.addMarkers();
        if (this.filteredDestinations.length > 0 && this.map) {
            // Optional: Fit bounds if many results, or just update view
            // const group = new L.FeatureGroup(this.markersGroup.getLayers() as L.Marker[]);
            // this.map.fitBounds(group.getBounds().pad(0.1));
        }
    }

    setCategory(catId: string) {
        this.selectedCategory = catId;
        this.applyFilters();
    }

    locateUser() {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(pos => {
                this.userLocation = new L.LatLng(pos.coords.latitude, pos.coords.longitude);
                if (this.map) {
                    L.circleMarker(this.userLocation, {
                        color: 'var(--safar-majorelle)',
                        fillColor: '#3b82f6',
                        fillOpacity: 0.5,
                        radius: 8
                    }).addTo(this.map)
                        .bindPopup("You are here").openPopup();

                    this.map.flyTo(this.userLocation, 12);
                }
            }, err => {
                console.error('Geolocation denied or failed', err);
            });
        }
    }

    selectDestination(dest: Destination) {
        this.selectedDestination = dest;
        if (dest.latitude && dest.longitude) {
            this.map.flyTo([dest.latitude, dest.longitude], 14, {
                animate: true,
                duration: 1.5
            });

            // Find and open marker popup
            this.markersGroup.eachLayer((layer: any) => {
                if (layer instanceof L.Marker) {
                    const latLng = layer.getLatLng();
                    if (latLng.lat === dest.latitude && latLng.lng === dest.longitude) {
                        layer.openPopup();
                    }
                }
            });
        }
    }

    calculateRoute(destination: L.LatLng) {
        if (!this.userLocation) return;
        this.isRouting = true;

        if (this.routingControl) {
            this.map.removeControl(this.routingControl);
        }

        this.routingControl = L.Routing.control({
            waypoints: [
                this.userLocation,
                destination
            ],
            routeWhileDragging: false,
            showAlternatives: false,
            fitSelectedRoutes: true,
            lineOptions: {
                styles: [{ color: '#D35400', weight: 5 }],
                extendToWaypoints: true,
                missingRouteTolerance: 0
            }
        } as any).addTo(this.map);

        this.routingControl.on('routesfound', (e: any) => {
            this.isRouting = false;
            const routes = e.routes;
            const summary = routes[0].summary;
            // You can display summary.totalDistance (meters) and summary.totalTime (seconds)
        });
    }

    @HostListener('window:resize')
    onResize() {
        if (this.map) {
            this.map.invalidateSize();
        }
    }
}
