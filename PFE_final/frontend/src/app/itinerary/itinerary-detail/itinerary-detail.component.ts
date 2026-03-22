import { Component, OnInit, AfterViewInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ItineraryService } from '../../core/services/itinerary.service';
import { ItineraireDetailDTO } from '../../core/models/itinerary.model';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TranslateService } from '@ngx-translate/core';
import { ReservationService } from '../../core/services/reservation.service';
import { InvoiceService } from '../../core/services/invoice.service';
import * as L from 'leaflet';

@Component({
    selector: 'app-itinerary-detail',
    templateUrl: './itinerary-detail.component.html',
    styleUrls: ['./itinerary-detail.component.css'],
    standalone: false
})
export class ItineraryDetailComponent implements OnInit, AfterViewInit, OnDestroy {
    itinerary: ItineraireDetailDTO | null = null;
    reservations: any[] = [];
    loading = true;
    map: L.Map | null = null;
    markers: L.Marker[] = [];
    routeControl: L.Polyline | null = null;
    isDropdownOpen = false;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private itineraryService: ItineraryService,
        private reservationService: ReservationService,
        private invoiceService: InvoiceService,
        private snackBar: MatSnackBar,
        private cdr: ChangeDetectorRef,
        private translate: TranslateService
    ) {}

    ngOnInit(): void {
        // Écouter les changements de langue
        this.translate.onLangChange.subscribe((event) => {
            console.log('🔄 Language changed in detail component:', event);
            this.forceUpdateTranslations();
        });
        
        // Forcer la mise à jour initiale
        setTimeout(() => {
            this.forceUpdateTranslations();
        }, 100);
        
        this.loadItinerary();
    }

    forceUpdateTranslations(): void {
        console.log('🔄 Forcing translation update in detail component');
        // Forcer la détection de changements
        setTimeout(() => {
            this.cdr.detectChanges();
        }, 50);
    }

    ngAfterViewInit(): void {
        // Initialize map after component view is initialized
        setTimeout(() => {
            this.initializeMap();
        }, 100);
    }

    ngOnDestroy(): void {
        if (this.map) {
            this.map.remove();
            this.map = null;
        }
    }

    loadItinerary(): void {
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.itineraryService.getItineraryDetails(+id).subscribe({
                next: (data) => {
                    this.itinerary = data;
                    this.loading = false;
                    this.cdr.detectChanges();
                    
                    this.loadReservations(+id);

                    // Initialize map if not already done
                    if (!this.map) {
                        setTimeout(() => this.initializeMap(), 100);
                    } else {
                        this.updateMap();
                    }
                },
                error: (err) => {
                    console.error('Error loading itinerary:', err);
                    this.loading = false;
                    this.snackBar.open('Erreur lors du chargement de l\'itinéraire', 'Fermer', {
                        duration: 3000
                    });
                }
            });
        }
    }

    loadReservations(itineraryId: number): void {
        this.reservationService.getReservationsByItinerary(itineraryId).subscribe({
            next: (data) => {
                this.reservations = data || [];
                console.log('Reservations loaded:', this.reservations);
                this.cdr.detectChanges();
            },
            error: (err) => {
                console.error('Error loading reservations:', err);
                this.cdr.detectChanges();
            }
        });
    }

    initializeMap(): void {
        if (!this.itinerary || this.map) return;

        // Initialize map centered on Morocco
        this.map = L.map('itinerary-map').setView([31.7917, -7.0926], 6);

        // Add OpenStreetMap tiles
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors'
        }).addTo(this.map);

        this.updateMap();
    }

    updateMap(): void {
        if (!this.map || !this.itinerary) return;

        // Clear existing markers and routes
        this.markers.forEach(marker => this.map!.removeLayer(marker));
        this.markers = [];
        if (this.routeControl) {
            this.map.removeLayer(this.routeControl);
        }

        // Add markers for each destination
        const latlngs: L.LatLng[] = [];
        
        this.itinerary.destinations.forEach((destination, index) => {
            if (destination.latitude && destination.longitude) {
                const latlng = L.latLng(destination.latitude, destination.longitude);
                latlngs.push(latlng);

                // Create custom icon with number
                const icon = L.divIcon({
                    className: 'custom-div-icon',
                    html: `<div style="background-color: #2196F3; color: white; border-radius: 50%; width: 30px; height: 30px; display: flex; align-items: center; justify-content: center; font-weight: bold; border: 2px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);">${index + 1}</div>`,
                    iconSize: [30, 30],
                    iconAnchor: [15, 15]
                });

                const marker = L.marker(latlng, { icon })
                    .bindPopup(`<b>${destination.nom}</b><br>${destination.categorie || destination.type}`)
                    .addTo(this.map!);
                
                this.markers.push(marker);
            }
        });

        // Draw route between destinations
        if (latlngs.length > 1) {
            this.routeControl = L.polyline(latlngs, {
                color: '#2196F3',
                weight: 3,
                opacity: 0.7,
                dashArray: '10, 10'
            }).addTo(this.map);

            // Fit map to show all markers
            const bounds = L.latLngBounds(latlngs);
            this.map.fitBounds(bounds, { padding: [50, 50] });
        } else if (latlngs.length === 1) {
            // Center on single destination
            this.map.setView(latlngs[0], 10);
        }
    }

    optimizeItinerary(): void {
        console.log('Optimize button clicked');
        console.log('Itinerary data:', this.itinerary);
        console.log('Is optimized:', this.itinerary?.estOptimise);
        
        if (!this.itinerary) {
            console.error('No itinerary data available');
            return;
        }

        if (this.itinerary.estOptimise) {
            console.log('ℹ Itinerary is already optimized, but forcing re-optimization as requested.');
            // We allow re-optimization to let users update their path if needed
        }

        console.log('Starting optimization...');
        this.snackBar.open('Optimisation en cours...', 'Fermer', {
            duration: 2000
        });

        this.itineraryService.optimiserItineraire(this.itinerary.id, this.itinerary.proprietaire.id).subscribe({
            next: (data) => {
                console.log('Optimization successful:', data);
                this.snackBar.open('Itinéraire optimisé avec succès!', 'Fermer', {
                    duration: 3000
                });
                // Reload itinerary to show updated data
                this.loadItinerary();
            },
            error: (err) => {
                console.error('Error optimizing itinerary:', err);
                this.snackBar.open('Erreur lors de l\'optimisation de l\'itinéraire', 'Fermer', {
                    duration: 3000
                });
            }
        });
    }

    startNavigation(): void {
        if (!this.itinerary || this.itinerary.destinations.length === 0) return;

        const destinations = this.itinerary.destinations;
        const validDestinations = destinations.filter(d => d.latitude && d.longitude);

        if (validDestinations.length === 0) return;

        if (validDestinations.length === 1) {
            const dest = validDestinations[0];
            const url = `https://www.google.com/maps/dir/?api=1&destination=${dest.latitude},${dest.longitude}`;
            window.open(url, '_blank');
        } else {
            // Use first as origin, last as destination, others as waypoints
            const origin = validDestinations[0];
            const destination = validDestinations[validDestinations.length - 1];
            const waypoints = validDestinations.slice(1, -1)
                .map(d => `${d.latitude},${d.longitude}`)
                .join('|');

            let url = `https://www.google.com/maps/dir/?api=1&origin=${origin.latitude},${origin.longitude}&destination=${destination.latitude},${destination.longitude}`;
            if (waypoints) {
                url += `&waypoints=${waypoints}`;
            }
            window.open(url, '_blank');
        }
    }

    refreshItinerary(): void {
        console.log('Refresh button clicked');
        this.snackBar.open('Actualisation en cours...', 'Fermer', {
            duration: 1000
        });
        this.loadItinerary();
    }

    toggleDropdown(): void {
        this.isDropdownOpen = !this.isDropdownOpen;
    }

    editItinerary(): void {
        this.isDropdownOpen = false;
        if (!this.itinerary) return;
        
        console.log('Edit itinerary clicked');
        // Navigate to create page with itinerary ID for editing
        this.router.navigate(['/itineraries/create'], { 
            queryParams: { editId: this.itinerary.id } 
        });
    }

    deleteItinerary(): void {
        this.isDropdownOpen = false;
        if (!this.itinerary) return;
        
        console.log('Delete itinerary clicked');
        
        if (confirm('Êtes-vous sûr de vouloir supprimer cet itinéraire ? Cette action est irreversible.')) {
            this.itineraryService.supprimerItineraire(this.itinerary.id, this.itinerary.proprietaire.id).subscribe({
                next: () => {
                    this.snackBar.open('Itinéraire supprimé avec succès', 'Fermer', {
                        duration: 3000
                    });
                    this.router.navigate(['/itineraries']);
                },
                error: (err) => {
                    console.error('Error deleting itinerary:', err);
                    this.snackBar.open('Erreur lors de la suppression de l\'itinéraire', 'Fermer', {
                        duration: 3000
                    });
                }
            });
        }
    }

    viewDestinationDetails(destination: any): void {
        console.log('View destination details clicked:', destination);
        
        // Naviguer vers la page de détails de la destination
        // Assurez-vous que la route '/destinations/:id' existe dans votre routing
        this.router.navigate(['/destinations', destination.id]);
    }

    goBack(): void {
        this.router.navigate(['/itineraries']);
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

    getDestinationTypeIcon(type: string): string {
        switch (type?.toLowerCase()) {
            case 'cultural':
                return 'bi-building';
            case 'historical':
                return 'bi-clock-history';
            case 'natural':
                return 'bi-tree';
            case 'adventure':
                return 'bi-bicycle';
            default:
                return 'bi-geo-alt';
        }
    }

    downloadReservationPDF(reservationId: number): void {
        this.snackBar.open('Génération du PDF...', 'Fermer', {
            duration: 2000
        });

        const lang = this.translate.currentLang || this.translate.getDefaultLang() || 'fr';

        this.invoiceService.generateReservationInvoice(reservationId, lang).subscribe({
            next: (invoice) => {
                if (invoice.pdfPath) {
                    // Logic to download the file
                    const fileName = invoice.pdfPath.split('/').pop();
                    const downloadUrl = `/uploads/invoices/${fileName}`;
                    window.open(downloadUrl, '_blank');
                    
                    this.snackBar.open('PDF généré avec succès!', 'Fermer', {
                        duration: 3000
                    });
                } else {
                    this.snackBar.open('Erreur: Chemin du PDF non trouvé', 'Fermer', {
                        duration: 3000
                    });
                }
            },
            error: (err) => {
                console.error('Error generating PDF:', err);
                const errorMessage = err.error?.message || 'Erreur lors de la génération du PDF';
                this.snackBar.open(errorMessage, 'Fermer', {
                    duration: 3000
                });
            }
        });
    }
}
