import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
    standalone: false,
    selector: 'app-event-dialog',
    templateUrl: './event-dialog.component.html',
    styleUrls: ['./event-dialog.component.css']
})
export class EventDialogComponent {
    form: FormGroup;
    isUploading = false;
    eventTypes = ['FESTIVAL', 'CULTURAL', 'MUSIC', 'TRADITIONAL'];

    constructor(
        public dialogRef: MatDialogRef<EventDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: any,
        private apiService: ApiService,
        private snackBar: MatSnackBar,
        private fb: FormBuilder
    ) {
        this.form = this.fb.group({
            id: [data.id || null],
            nom: [data.nom || '', Validators.required],
            lieu: [data.lieu || '', Validators.required],
            dateDebut: [data.dateDebut || '', Validators.required],
            dateFin: [data.dateFin || '', Validators.required],
            description: [data.description || '', Validators.required],
            historique: [data.historique || ''],
            eventType: [data.eventType || 'CULTURAL', Validators.required],
            imageUrl: [data.imageUrl || ''],
            destinationId: [data.destinationId || data.destination?.id] // Ensure destinationId is captured
        });
    }

    onFileSelected(event: any) {
        const file = event.target.files[0];
        if (file) {
            this.isUploading = true;
            // We need a destinationId to upload. Use form value or data.
            const destId = this.form.get('destinationId')?.value || this.data.destinationId;
            if (destId) {
                this.apiService.uploadFile(destId, file).subscribe({
                    next: (response: any) => {
                        this.form.patchValue({ imageUrl: response.url });
                        this.isUploading = false;
                    },
                    error: (err: any) => {
                        console.error('Upload failed', err);
                        this.snackBar.open('Upload failed', 'Close', { duration: 3000 });
                        this.isUploading = false;
                    }
                });
            } else {
                console.error('No destination ID found for upload');
                this.snackBar.open('Cannot upload image without a destination', 'Close', { duration: 3000 });
                this.isUploading = false;
            }
        }
    }

    save() {
        if (this.form.invalid) return;

        const eventData = this.form.value;
        const request$ = eventData.id
            ? this.apiService.updateEvent(eventData.id, eventData)
            : this.apiService.createEvent(eventData);

        request$.subscribe({
            next: () => {
                this.snackBar.open(eventData.id ? 'Event updated' : 'Event created', 'Close', { duration: 3000 });
                this.dialogRef.close(true);
            },
            error: (err: any) => {
                console.error(err);
                this.snackBar.open('Failed to save event', 'Close', { duration: 3000 });
            }
        });
    }
}
