import { Directive, ElementRef, Input, OnChanges, SimpleChanges } from '@angular/core';

@Directive({
    selector: '[appCountUp]',
    standalone: false
})
export class CountUpDirective implements OnChanges {
    @Input('appCountUp') countTo: number = 0;
    @Input() duration: number = 1000;

    constructor(private el: ElementRef) { }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['countTo']) {
            this.animate();
        }
    }

    animate() {
        const start = 0;
        const end = this.countTo;
        const duration = this.duration;
        const startTime = performance.now();

        const step = (currentTime: number) => {
            const elapsed = currentTime - startTime;
            const progress = Math.min(elapsed / duration, 1);

            // Ease out quart
            const ease = 1 - Math.pow(1 - progress, 4);

            const current = Math.floor(start + (end - start) * ease);
            this.el.nativeElement.textContent = current.toLocaleString();

            if (progress < 1) {
                requestAnimationFrame(step);
            } else {
                this.el.nativeElement.textContent = end.toLocaleString();
            }
        };

        requestAnimationFrame(step);
    }
}
