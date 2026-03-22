import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-verified',
  standalone: false,
  templateUrl: './verified.html',
  styleUrl: './verified.css',
})
export class Verified implements OnInit {

  status: 'pending' | 'success' | 'error' = 'pending';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) { }

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.status = 'error';
      return;
    }

    this.authService.verifyToken(token).subscribe({
      next: (response) => {
        this.status = 'success';
        this.authService.handleAuthSuccess(response);

        setTimeout(() => {
          this.router.navigate(['/']);
        }, 3000);
      },
      error: (err) => {
        console.error('Verification error:', err);
        this.status = 'error';
      }
    });
  }
}
