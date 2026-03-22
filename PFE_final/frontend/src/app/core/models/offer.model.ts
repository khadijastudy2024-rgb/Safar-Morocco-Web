export enum OfferType {
  HOTEL = 'HOTEL',
  RESTAURANT = 'RESTAURANT',
  ACTIVITY = 'ACTIVITY'
}

export interface Offer {
  id: number;
  name: string;
  description?: string;
  price?: number;
  type: OfferType;
  destinationId: number;
  available: boolean;
  deleted?: boolean;
  // HOTEL
  stars?: number;
  roomType?: string;
  pricePerNight?: number;
  // RESTAURANT
  cuisineType?: string;
  averagePrice?: number;
  // ACTIVITY
  duration?: string;
  activityType?: string;
  displayPrice?: number;
}
