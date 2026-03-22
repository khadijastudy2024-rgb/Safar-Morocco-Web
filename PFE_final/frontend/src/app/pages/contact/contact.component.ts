import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TranslateService } from '@ngx-translate/core';

@Component({
    standalone: false,
    selector: 'app-contact',
    templateUrl: './contact.component.html',
    styleUrls: ['./contact.component.css']
})
export class ContactComponent implements OnInit {
    contactForm!: FormGroup;
    isSubmitting = false;

    constructor(
        private fb: FormBuilder,
        private snackBar: MatSnackBar,
        private translate: TranslateService
    ) { }

    ngOnInit(): void {
        this.initForm();
    }

    private initForm(): void {
        this.contactForm = this.fb.group({
            name: ['', [Validators.required, Validators.minLength(3)]],
            email: ['', [Validators.required, Validators.email]],
            subject: ['', [Validators.required]],
            message: ['', [Validators.required, Validators.minLength(10)]]
        });
    }

    onSubmit(): void {
        if (this.contactForm.valid) {
            this.isSubmitting = true;
            
            // Simuler un appel API
            setTimeout(() => {
                const msg = this.translate.instant('CONTACT_PAGE.SUCCESS_MESSAGE');
                const close = this.translate.instant('COMMON.CLOSE');
                this.snackBar.open(msg, close, {
                    duration: 5000,
                    horizontalPosition: 'center',
                    verticalPosition: 'bottom',
                    panelClass: ['success-snackbar']
                });
                this.contactForm.reset();
                this.isSubmitting = false;
            }, 1500);
        } else {
            this.contactForm.markAllAsTouched();
        }
    }
}
