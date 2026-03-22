import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-auth-required',
  standalone: false,
  templateUrl: './auth-required.html',
  styleUrls: ['./auth-required.css'],
})
export class AuthRequired implements OnInit {
  returnUrl: string = '/';

  constructor(private route: ActivatedRoute) { }

  ngOnInit(): void {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }
}

