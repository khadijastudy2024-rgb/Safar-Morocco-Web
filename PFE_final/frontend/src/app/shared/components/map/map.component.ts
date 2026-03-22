import { Component, Input, OnChanges, OnInit, SimpleChanges, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import * as L from 'leaflet';

@Component({
    standalone: false,
    selector: 'app-map',
    templateUrl: './map.component.html',
    styleUrls: ['./map.component.css']
})
export class MapComponent implements OnInit, AfterViewInit, OnChanges {
    @Input() latitude!: number;
    @Input() longitude!: number;
    @Input() popupText: string = '';

    @ViewChild('map') mapContainer!: ElementRef;
    private map!: L.Map;

    constructor() { }

    ngOnInit(): void {
    }

    ngAfterViewInit(): void {
        this.initMap();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if ((changes['latitude'] || changes['longitude']) && this.map) {
            this.updateMap();
        }
    }

    private initMap(): void {
        if (!this.latitude || !this.longitude) return;

        this.map = L.map(this.mapContainer.nativeElement).setView([this.latitude, this.longitude], 13);

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors'
        }).addTo(this.map);

        this.addMarker();
    }

    private updateMap(): void {
        if (!this.map) return;
        this.map.setView([this.latitude, this.longitude], 13);
        // Clear existing layers (markers)
        this.map.eachLayer((layer) => {
            if (layer instanceof L.Marker) {
                this.map.removeLayer(layer);
            }
        });
        this.addMarker();
    }

    private addMarker(): void {
        const marker = L.marker([this.latitude, this.longitude]).addTo(this.map);
        if (this.popupText) {
            marker.bindPopup(this.popupText).openPopup();
        }
    }
}
