import { Component } from '@angular/core';

@Component({
  selector: 'app-terms-and-conditions',
  standalone: false,
  templateUrl: './terms-and-conditions.component.html',
  styleUrls: ['./terms-and-conditions.component.css']
})
export class TermsAndConditionsComponent {
  currentDate = new Date();
}
