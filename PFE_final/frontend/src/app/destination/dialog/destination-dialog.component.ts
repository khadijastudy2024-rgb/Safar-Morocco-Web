import { Component, Inject, AfterViewInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { DestinationService } from '../../core/services/destination.service';
import { ApiService } from '../../core/services/api.service';
import { Destination } from '../../core/models/destination.model';
import * as L from 'leaflet';

@Component({
    standalone: false,
    selector: 'app-destination-dialog',
    templateUrl: './destination-dialog.component.html',
    styleUrls: ['./destination-dialog.component.css']
})
export class DestinationDialogComponent implements AfterViewInit {
    form: FormGroup;
    loading = false;
    isUploading = false;
    // isUploading = false; // Duplicate removed
    existingImages: string[] = []; // URLs of existing images
    newFiles: { file: File, preview: string }[] = []; // Pending files to upload
    private map!: L.Map;
    private marker: L.Marker | null = null;

    constructor(
        private fb: FormBuilder,
        private destinationService: DestinationService,
        private apiService: ApiService,
        private dialogRef: MatDialogRef<DestinationDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: Destination
    ) {
        this.form = this.fb.group({
            nom: [data.nom || '', Validators.required],
            type: [data.type || '', Validators.required],
            categorie: [data.categorie || 'Cultural', Validators.required],
            latitude: [data.latitude || 31.7917, Validators.required],
            longitude: [data.longitude || -7.0926, Validators.required],
            videoUrl: [data.videoUrl || ''],
            description: [data.description || '', [Validators.required, Validators.minLength(20)]]
        });
        if (data.medias) {
            this.existingImages = data.medias.map(m => m.url);
        }
    }

    ngAfterViewInit(): void {
        this.initMap();

        // Listen to Latitude changes
        this.form.get('latitude')?.valueChanges.subscribe(val => {
            const lng = this.form.get('longitude')?.value;
            if (val && lng) this.updateMarker(val, lng, false);
        });

        // Listen to Longitude changes
        this.form.get('longitude')?.valueChanges.subscribe(val => {
            const lat = this.form.get('latitude')?.value;
            if (val && lat) this.updateMarker(lat, val, false);
        });
    }

    private initMap(): void {
        const lat = this.data.latitude || 31.7917;
        const lng = this.data.longitude || -7.0926;

        this.map = L.map('map').setView([lat, lng], 6);

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '© OpenStreetMap'
        }).addTo(this.map);

        this.updateMarker(lat, lng, false);

        this.map.on('click', (e: L.LeafletMouseEvent) => {
            this.updateMarker(e.latlng.lat, e.latlng.lng, true);
        });

        // Fix Leaflet sizing issue in dialog
        setTimeout(() => {
            this.map.invalidateSize();
        }, 300);
    }

    private updateMarker(lat: number, lng: number, updateForm: boolean) {
        if (this.marker) {
            this.marker.setLatLng([lat, lng]);
        } else {
            this.marker = L.marker([lat, lng]).addTo(this.map);
        }

        if (updateForm) {
            this.form.patchValue({
                latitude: parseFloat(lat.toFixed(6)),
                longitude: parseFloat(lng.toFixed(6))
            }, { emitEvent: false });
        }

        // Pan map if marker is out of view (optional)
        // this.map.panTo([lat, lng]);
    }

    isDragging = false;

    get descriptionLength(): number {
        const desc = this.form.get('description')?.value || '';
        return desc.length;
    }

    onDragOver(event: DragEvent) {
        event.preventDefault();
        event.stopPropagation();
        this.isDragging = true;
    }

    onDragLeave(event: DragEvent) {
        event.preventDefault();
        event.stopPropagation();
        this.isDragging = false;
    }

    onDrop(event: DragEvent) {
        event.preventDefault();
        event.stopPropagation();
        this.isDragging = false;

        const files = event.dataTransfer?.files;
        if (files && files.length > 0) {
            const file = files[0];
            this.uploadFile(file);
        }
    }

    uploadFile(file: File) {
        if (!file.type.startsWith('image/')) {
            console.error('Please select an image file');
            return;
        }

        if (file.size > 5 * 1024 * 1024) {
            console.error('File size must be less than 5MB');
            return;
        }

        // Create preview
        const reader = new FileReader();
        reader.onload = (e: any) => {
            this.newFiles.push({
                file: file,
                preview: e.target.result
            });
        };
        reader.readAsDataURL(file);
    }

    onFileSelected(event: any) {
        const file = event.target.files[0];
        if (file) {
            this.uploadFile(file);
        }
    }

    addImageUrl(url: string) {
        if (!url) return;
        try {
            new URL(url);
            const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.svg'];
            const hasImageExtension = imageExtensions.some(ext => url.toLowerCase().includes(ext));

            if (hasImageExtension || url.includes('unsplash') || url.includes('pexels')) {
                this.existingImages.push(url);
            } else {
                console.error('URL must point to an image');
            }
        } catch (e) {
            console.error('Invalid URL format');
        }
    }

    removeExistingImage(index: number) {
        this.existingImages.splice(index, 1);
    }

    removeNewFile(index: number) {
        this.newFiles.splice(index, 1);
    }

    onSave() {
        if (this.form.invalid) return;

        this.loading = true;
        const destinationData: Destination = { ...this.data, ...this.form.value };

        // Only include existing images in the payload (so backend knows which to keep)
        // New files will be uploaded separately
        if (this.existingImages.length > 0) {
            destinationData.medias = this.existingImages.map(url => ({
                url: url,
                type: 'IMAGE'
            }));
            destinationData.thumbnailUrl = this.existingImages[0];
        } else {
            destinationData.medias = [];
        }

        const request$ = this.data.id
            ? this.destinationService.updateDestination(this.data.id, destinationData)
            : this.destinationService.createDestination(destinationData);

        request$.subscribe({
            next: (result: any) => {
                const destinationId = result.id;

                // Process pending files
                if (this.newFiles.length > 0) {
                    let completed = 0;
                    this.newFiles.forEach(item => {
                        this.apiService.uploadFile(destinationId, item.file).subscribe({
                            next: () => {
                                completed++;
                                if (completed === this.newFiles.length) {
                                    this.loading = false;
                                    this.dialogRef.close(result);
                                }
                            },
                            error: (err: any) => {
                                console.error('Upload error', err);
                                completed++;
                                if (completed === this.newFiles.length) {
                                    this.loading = false;
                                    this.dialogRef.close(result);
                                }
                            }
                        });
                    });
                } else {
                    this.loading = false;
                    this.dialogRef.close(result);
                }
            },
            error: (err: any) => {
                this.loading = false;
                console.error(err);
            }
        });
    }

    searchLocation(query: string) {
        if (!query) return;

        // Use Nominatim (OSM) for free geocoding
        const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}`;

        fetch(url)
            .then(response => response.json())
            .then(data => {
                if (data && data.length > 0) {
                    const result = data[0];
                    const lat = parseFloat(result.lat);
                    const lon = parseFloat(result.lon);

                    this.updateMarker(lat, lon, true);
                    this.map.setView([lat, lon], 12);
                } else {
                    console.warn('Location not found');
                }
            })
            .catch(err => console.error('Geocoding error:', err));
    }

    onCancel() {
        this.dialogRef.close();
    }
}
