import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Offer } from '../../../core/models/offer.model';
import { Destination } from '../../../core/models/destination.model';

@Component({
  selector: 'app-offer-dialog',
  standalone: false,
  templateUrl: './offer-dialog.html',
  styleUrls: ['./offer-dialog.css']
})
export class OfferDialogComponent implements OnInit {
  offerForm!: FormGroup;
  isEditMode: boolean = false;
  destinations: Destination[] = [];

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<OfferDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { offer?: Offer, destinations: Destination[] }
  ) {
    this.isEditMode = !!data.offer;
    this.destinations = data.destinations || [];
  }

  ngOnInit(): void {
    this.offerForm = this.fb.group({
      name: [this.data.offer?.name || '', [Validators.required, Validators.maxLength(100)]],
      type: [this.data.offer?.type || 'HOTEL', Validators.required],
      destinationId: [this.data.offer?.destinationId || null, Validators.required],
      price: [this.data.offer?.price || 0, [Validators.required, Validators.min(0)]],
      // HOTEL
      stars: [this.data.offer?.stars || 5, [Validators.min(1), Validators.max(5)]],
      roomType: [this.data.offer?.roomType || ''],
      pricePerNight: [this.data.offer?.pricePerNight || 0, [Validators.min(0)]],
      // RESTAURANT
      cuisineType: [this.data.offer?.cuisineType || ''],
      averagePrice: [this.data.offer?.averagePrice || 0, [Validators.min(0)]],
      // ACTIVITY
      duration: [this.data.offer?.duration || ''],
      activityType: [this.data.offer?.activityType || '']
    });
  }

  get selectedType(): string {
    return this.offerForm.get('type')?.value;
  }

  onSubmit(): void {
    if (this.offerForm.valid) {
      const formValue = this.offerForm.value;
      const offerData: Offer = {
        ...this.data.offer,
        ...formValue
      };
      this.dialogRef.close(offerData);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
