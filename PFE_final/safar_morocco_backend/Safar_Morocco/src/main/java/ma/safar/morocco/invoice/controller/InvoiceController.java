package ma.safar.morocco.invoice.controller;

import lombok.RequiredArgsConstructor;
import ma.safar.morocco.invoice.dto.InvoiceDTO;
import ma.safar.morocco.invoice.service.InvoiceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/generate/{itineraryId}/{userId}")
    public ResponseEntity<InvoiceDTO> generateInvoice(
            @PathVariable("itineraryId") Long itineraryId,
            @PathVariable("userId") Long userId,
            @RequestParam(defaultValue = "fr", required = false) String lang) {
        return new ResponseEntity<>(invoiceService.generateInvoice(itineraryId, userId, lang), HttpStatus.CREATED);
    }

    @PostMapping("/generate/reservation/{reservationId}")
    public ResponseEntity<InvoiceDTO> generateReservationInvoice(
            @PathVariable("reservationId") Long reservationId,
            @RequestParam(defaultValue = "fr", required = false) String lang) {
        return new ResponseEntity<>(invoiceService.generateInvoiceForReservation(reservationId, lang), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDTO> getInvoiceById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<InvoiceDTO>> getInvoicesByUser(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(invoiceService.getInvoicesByUser(userId));
    }

    @GetMapping("/itinerary/{itineraryId}")
    public ResponseEntity<List<InvoiceDTO>> getInvoicesByItinerary(@PathVariable("itineraryId") Long itineraryId) {
        return ResponseEntity.ok(invoiceService.getInvoicesByItinerary(itineraryId));
    }
}
