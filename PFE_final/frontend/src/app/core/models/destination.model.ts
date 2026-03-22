export interface Media {
    id?: number;
    url: string;
    type: 'IMAGE' | 'VIDEO';
}

export interface Review {
    id?: number;
    note: number;
    commentaire: string;
    auteur?: any;
    datePublication?: string;
}

import { Offer } from './offer.model';

export interface Destination {
    id: number;
    nom: string;
    description?: string;
    histoire?: string;
    historicalDescription?: string;
    type: string; // City/Location type
    latitude?: number;
    longitude?: number;
    categorie: string;
    videoUrl?: string;
    medias?: Media[];
    avis?: Review[];
    evenements?: any[]; // Will be typed later
    offers?: Offer[];
    meteo?: any;
    // UI Helpers
    thumbnailUrl?: string; // Derived or mapped
    averageRating?: number;
    reviewCount?: number;
    bestTime?: string;
    languages?: string;
    viewCount?: number; // Backend uses viewCount
    averageCost?: number;
}
