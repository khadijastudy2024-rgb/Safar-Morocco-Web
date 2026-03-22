export enum InvoiceStatus {
    PAID = 'PAID',
    UNPAID = 'UNPAID'
}

export interface Invoice {
    id: number;
    userId: number;
    itineraryId: number;
    totalAmount: number;
    generatedDate: string;
    status: InvoiceStatus;
    pdfPath?: string;
}
