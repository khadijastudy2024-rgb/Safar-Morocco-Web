import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ItineraryService } from '../../core/services/itinerary.service';
import { DestinationService } from '../../core/services/destination.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
    selector: 'app-itinerary-create',
    templateUrl: './itinerary-create.component.html',
    styleUrls: ['./itinerary-create.component.css'],
    standalone: false
})
export class ItineraryCreateComponent implements OnInit {
    itineraryForm: FormGroup;
    availableDestinations: any[] = [];
    selectedDestinations: any[] = [];
    loading = false;
    preselectedDestinationId: number | null = null;
    editMode = false;
    editId: number | null = null;
    preloadDestinations: any[] = [];

    constructor(
        private fb: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private itineraryService: ItineraryService,
        private destinationService: DestinationService,
        private snackBar: MatSnackBar,
        private authService: AuthService,
        private translate: TranslateService,
        private cdr: ChangeDetectorRef
    ) {
        this.itineraryForm = this.fb.group({
            nom: ['', [Validators.required, Validators.minLength(3)]]
        });
    }

    ngOnInit(): void {
        console.log('🔍 ItineraryCreateComponent initialized');
        console.log('🔍 Route snapshot:', this.route.snapshot);
        console.log('🔍 Query params:', this.route.snapshot.queryParamMap);
        
        // Écouter les changements de langue
        this.translate.onLangChange.subscribe((event) => {
            console.log('🔄 Language changed in create component:', event);
            this.forceUpdateTranslations();
        });
        
        // Forcer la mise à jour initiale
        setTimeout(() => {
            this.forceUpdateTranslations();
        }, 100);
        
        this.checkEditMode();
        this.loadDestinations();
        this.checkPreselectedDestination();
    }

    forceUpdateTranslations(): void {
        console.log('🔄 Forcing translation update');
        // Forcer la détection de changements
        setTimeout(() => {
            this.cdr.detectChanges();
        }, 50);
    }

    checkEditMode(): void {
        const editIdParam = this.route.snapshot.queryParamMap.get('editId');
        if (editIdParam) {
            this.editMode = true;
            this.editId = +editIdParam;
            console.log('🔍 Edit mode detected for itinerary ID:', this.editId);
            this.loadItineraryForEdit();
        }
    }

    loadItineraryForEdit(): void {
        if (!this.editId) return;
        
        console.log('🔍 Loading itinerary for edit, ID:', this.editId);
        this.itineraryService.getItineraryDetails(this.editId).subscribe({
            next: (itinerary) => {
                console.log('🔍 Itinerary loaded for edit:', itinerary);
                
                // Pré-remplir le nom de l'itinéraire
                this.itineraryForm.patchValue({
                    nom: itinerary.nom
                });
                
                // Stocker les destinations pour le pré-chargement
                this.preloadDestinations = itinerary.destinations || [];
                console.log('🔍 Preload destinations set:', this.preloadDestinations);
                
                // Si les destinations disponibles sont déjà chargées, ajouter les destinations pré-chargées
                if (this.availableDestinations.length > 0) {
                    this.addPreloadedDestinations();
                }
            },
            error: (err) => {
                console.error('❌ Error loading itinerary for edit:', err);
                this.snackBar.open('Erreur lors du chargement de l\'itinéraire', 'Fermer', {
                    duration: 3000
                });
            }
        });
    }

    addPreloadedDestinations(): void {
        if (!this.editMode || !this.preloadDestinations || this.preloadDestinations.length === 0) {
            return;
        }
        
        console.log('🔍 Adding preloaded destinations:', this.preloadDestinations);
        
        // Vider d'abord les destinations sélectionnées
        this.selectedDestinations = [];
        
        this.preloadDestinations.forEach(preloadedDest => {
            const matchingDestination = this.availableDestinations.find(d => d.id === preloadedDest.id);
            if (matchingDestination) {
                this.selectedDestinations.push(matchingDestination);
                console.log('🔍 Added destination:', matchingDestination.nom);
            } else {
                console.warn('⚠️ Destination not found in available destinations:', preloadedDest);
            }
        });
        console.log('🔍 Final selected destinations:', this.selectedDestinations);
    }

    checkPreselectedDestination(): void {
        const destinationId = this.route.snapshot.queryParamMap.get('destinationId');
        console.log('🔍 Checking preselected destination ID from URL:', destinationId);
        
        if (destinationId) {
            this.preselectedDestinationId = +destinationId;
            console.log('🔍 Preselected destination ID set:', this.preselectedDestinationId);
            
            // Si les destinations sont déjà chargées, ajouter la destination pré-sélectionnée
            if (this.availableDestinations.length > 0) {
                this.addPreselectedDestination();
            }
        }
    }

    addPreselectedDestination(): void {
        if (!this.preselectedDestinationId) return;
        
        const preselected = this.availableDestinations.find(d => d.id === this.preselectedDestinationId);
        console.log('🔍 Looking for preselected destination:', this.preselectedDestinationId);
        console.log('🔍 Preselected destination found:', preselected);
        
        if (preselected && !this.selectedDestinations.find(d => d.id === preselected.id)) {
            this.selectedDestinations.push(preselected);
            console.log('🔍 Preselected destination added to selection:', preselected.nom);
            console.log('🔍 Total selected destinations:', this.selectedDestinations.length);
        } else if (preselected) {
            console.log('🔍 Preselected destination already in selection');
        } else {
            console.warn('⚠️ Preselected destination not found in available destinations');
        }
    }

    loadDestinations(): void {
        this.destinationService.getAllDestinations().subscribe({
            next: (destinations: any[]) => {
                this.availableDestinations = destinations;
                console.log('🔍 Destinations loaded:', destinations);
                console.log('🔍 Edit mode:', this.editMode);
                
                // En mode édition, ajouter les destinations de l'itinéraire existant
                if (this.editMode) {
                    this.addPreloadedDestinations();
                }
                
                // Ajouter la destination pré-sélectionnée (mode création)
                if (this.preselectedDestinationId) {
                    this.addPreselectedDestination();
                }
            },
            error: (err: any) => {
                console.error('Error loading destinations:', err);
                this.snackBar.open('Erreur lors du chargement des destinations', 'Fermer', {
                    duration: 3000
                });
            }
        });
    }

    addDestination(destination: any): void {
        if (!this.selectedDestinations.find(d => d.id === destination.id)) {
            this.selectedDestinations.push(destination);
        }
    }

    removeDestination(destination: any): void {
        const index = this.selectedDestinations.findIndex(d => d.id === destination.id);
        if (index > -1) {
            this.selectedDestinations.splice(index, 1);
        }
    }

    isDestinationSelected(destination: any): boolean {
        return this.selectedDestinations.some(d => d.id === destination.id);
    }

    onCreateItinerary(): void {
        console.log(' onCreateItinerary called');
        console.log(' Edit mode:', this.editMode);
        console.log(' Form valid:', this.itineraryForm.valid);
        console.log(' Form invalid:', this.itineraryForm.invalid);
        console.log(' Form value:', this.itineraryForm.value);
        console.log(' Selected destinations count:', this.selectedDestinations.length);
        console.log(' Loading state:', this.loading);

        if (this.itineraryForm.invalid || this.selectedDestinations.length === 0) {
            if (this.selectedDestinations.length === 0) {
                this.snackBar.open(this.translate.instant('ITINERARY.SELECT_AT_LEAST_ONE'), 'Fermer', {
                    duration: 3000
                });
            }
            if (this.itineraryForm.invalid) {
                this.snackBar.open(this.translate.instant('ITINERARY.NAME_ERROR'), 'Fermer', {
                    duration: 3000
                });
            }
            return;
        }

        this.loading = true;

        const currentUser = this.authService.currentUserValue;
        if (!currentUser) {
            this.snackBar.open('Utilisateur non connecté', 'Fermer', { duration: 3000 });
            this.loading = false;
            return;
        }

        const itineraryRequest = {
            nom: this.itineraryForm.value.nom,
            destinationIds: this.selectedDestinations.map(d => d.id)
        };

        console.log(' Itinerary request:', itineraryRequest);

        if (this.editMode && this.editId) {
            // Mode édition : mettre à jour l'itinéraire existant
            console.log(' Updating existing itinerary:', this.editId);
            this.itineraryService.updateItineraire(this.editId, itineraryRequest, currentUser.id).subscribe({
                next: (response) => {
                    console.log(' Itinerary updated successfully:', response);
                    this.snackBar.open('Itinéraire mis à jour avec succès!', 'Fermer', {
                        duration: 3000
                    });
                    this.router.navigate(['/itineraires/detail', response.id]);
                },
                error: (err) => {
                    console.error(' Error updating itinerary:', err);
                    this.snackBar.open('Erreur lors de la mise à jour de l\'itinéraire', 'Fermer', {
                        duration: 3000
                    });
                    this.loading = false;
                }
            });
        } else {
            // Mode création : créer un nouvel itinéraire
            console.log(' Creating new itinerary');
            this.itineraryService.creerItineraire(itineraryRequest, currentUser.id).subscribe({
                next: (response) => {
                    console.log(' Itinerary created successfully:', response);
                    this.snackBar.open('Itinéraire créé avec succès!', 'Fermer', {
                        duration: 3000
                    });
                    this.router.navigate(['/itineraires/detail', response.id]);
                },
                error: (err) => {
                    console.error(' Error creating itinerary:', err);
                    this.snackBar.open('Erreur lors de la création de l\'itinéraire', 'Fermer', {
                        duration: 3000
                    });
                    this.loading = false;
                }
            });
        }
    }

    goBack(): void {
        if (this.editMode && this.editId) {
            this.router.navigate(['/itineraires/detail', this.editId]);
        } else {
            this.router.navigate(['/itineraires']);
        }
    }
}
