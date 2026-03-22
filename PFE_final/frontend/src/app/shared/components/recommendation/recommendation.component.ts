import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { RecommendationService, RecommendationDTO } from '../../../services/recommendation.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-recommendation',
  templateUrl: './recommendation.component.html',
  styleUrls: ['./recommendation.component.css'],
  standalone: false
})
export class RecommendationComponent implements OnInit {
  recommendations: RecommendationDTO[] = [];
  isLoading: boolean = true;
  hasError: boolean = false;

  constructor(
    private recommendationService: RecommendationService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    if (this.authService.isLoggedIn) {
      this.fetchRecommendations();
    } else {
      this.isLoading = false;
      this.hasError = false;
      this.recommendations = [];
    }
  }

  fetchRecommendations(): void {
    this.isLoading = true;
    this.hasError = false;
    
    this.recommendationService.getPersonalizedRecommendations().subscribe({
      next: (response) => {
        if (response && response.recommendations) {
          this.recommendations = response.recommendations;
        } else {
          this.recommendations = [];
        }
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.hasError = true;
        this.isLoading = false;
        this.recommendations = [];
        this.cdr.detectChanges();
      }
    });
  }
}
