export interface EvenementCulturel {
    id: number;
    nom: string;
    dateDebut: string; // ISO String
    dateFin: string; // ISO String
    lieu: string;
    eventType: string;
    description: string;
    historique?: string;
    destinationId?: number;
    // UI Helpers
    imageUrl?: string;
    status?: 'UPCOMING' | 'ONGOING' | 'PAST';
}
