import { Component, OnInit } from '@angular/core';
import { ChildrenOutletContexts, Router, NavigationEnd } from '@angular/router';
import { trigger, transition, style, query, animate, group } from '@angular/animations';
import { LanguageService } from './core/services/language.service';
import { filter } from 'rxjs/operators';

export const fadeAnimation = trigger('routeAnimations', [
    transition('* <=> *', [
        style({ position: 'relative' }),
        query(':enter, :leave', [
            style({
                position: 'absolute',
                top: 0,
                left: 0,
                width: '100%'
            })
        ], { optional: true }),
        query(':enter', [
            style({ opacity: 0, transform: 'translateY(10px)' })
        ], { optional: true }),
        group([
            query(':leave', [
                animate('0.3s ease-out', style({ opacity: 0, transform: 'translateY(-10px)' }))
            ], { optional: true }),
            query(':enter', [
                animate('0.4s ease-out', style({ opacity: 1, transform: 'translateY(0)' }))
            ], { optional: true })
        ])
    ])
]);

@Component({
    standalone: false,
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css'],
    animations: [fadeAnimation]
})
export class AppComponent implements OnInit {
    title = 'Safar Morocco';

    constructor(
        private contexts: ChildrenOutletContexts,
        private languageService: LanguageService,
        private router: Router
    ) { }

    ngOnInit() {
        this.router.events.pipe(
            filter(event => event instanceof NavigationEnd)
        ).subscribe(() => {
            window.scrollTo({ top: 0, behavior: 'instant' });
        });
    }

    getRouteAnimationData() {
        return this.contexts.getContext('primary')?.route?.snapshot?.data?.['animation'];
    }
}
