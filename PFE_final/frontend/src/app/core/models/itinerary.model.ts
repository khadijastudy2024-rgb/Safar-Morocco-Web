export interface ItineraireDetailDTO {
    id: number;
    nom: string;
    dureeEstimee: string;
    dateCreation: string;
    dateModification: string;
    distanceTotale: number;
    nombreDestinations: number;
    estOptimise: boolean;
    proprietaire: ProprietaireDTO;
    destinations: DestinationDTO[];
}

export interface ProprietaireDTO {
    id: number;
    nom: string;
    email: string;
}

export interface DestinationDTO {
    id: number;
    nom: string;
    type: string;
    categorie: string;
    latitude: number;
    longitude: number;
    ordre: number;
}

export interface ItineraireResponseDTO {
    id: number;
    nom: string;
    dureeEstimee: string;
    dateCreation: string;
    dateModification: string;
    distanceTotale: number;
    nombreDestinations: number;
    estOptimise: boolean;
    proprietaire: ProprietaireDTO;
}

export interface ItineraireRequestDTO {
    nom: string;
    destinationIds: number[];
}

export interface UpdateItineraireDTO {
    nom: string;
    destinationIds: number[];
}

export interface RechercheItineraireDTO {
    nom?: string;
    estOptimise?: boolean;
    distanceMin?: number;
    distanceMax?: number;
}
