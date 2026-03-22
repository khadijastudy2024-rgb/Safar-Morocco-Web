import { Component, Input } from '@angular/core';

@Component({
    standalone: false,
    selector: 'app-skeleton-loader',
    templateUrl: './skeleton-loader.component.html',
    styleUrls: ['./skeleton-loader.component.css']
})
export class SkeletonLoaderComponent {
    @Input() width = '100%';
    @Input() height = '20px';
    @Input() shape: 'rect' | 'circle' = 'rect';
    @Input() count = 1;

    get items() {
        return new Array(this.count);
    }
}
