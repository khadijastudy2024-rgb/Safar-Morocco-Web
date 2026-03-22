package ma.safar.morocco.invoice.service;

import ma.safar.morocco.invoice.dto.InvoiceDTO;
import java.util.List;

public interface InvoiceService {
    InvoiceDTO generateInvoice(Long itineraryId, Long userId, String lang);
    InvoiceDTO generateInvoiceForReservation(Long reservationId, String lang);

    InvoiceDTO getInvoiceById(Long id);

    List<InvoiceDTO> getInvoicesByUser(Long userId);

    List<InvoiceDTO> getInvoicesByItinerary(Long itineraryId);
}
