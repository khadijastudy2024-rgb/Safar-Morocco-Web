export interface User {
    id: number;
    nom: string;
    email: string;
    role: 'USER' | 'ADMIN';
    telephone?: string;
    langue?: string;
    description?: string;
    photoUrl?: string; // If 'media' relation is used or a 'photo' field added later
    actif?: boolean;
    compteBloquer?: boolean;
    provider?: string;
}
