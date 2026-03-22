package ma.safar.morocco.reservation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.safar.morocco.reservation.dto.OfferReservationDTO;
import ma.safar.morocco.reservation.enums.ReservationStatus;
import ma.safar.morocco.reservation.service.OfferReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class OfferReservationController {

    private final OfferReservationService reservationService;

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    public ResponseEntity<OfferReservationDTO> createReservation(@RequestBody @Valid OfferReservationDTO dto) {
        log.info("Received request to create reservation: {}", dto);
        return new ResponseEntity<>(reservationService.createReservation(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OfferReservationDTO>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfferReservationDTO> getReservationById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @GetMapping("/itinerary/{itineraryId}")
    public ResponseEntity<List<OfferReservationDTO>> getReservationsByItinerary(@PathVariable("itineraryId") Long itineraryId) {
        return ResponseEntity.ok(reservationService.getReservationsByItinerary(itineraryId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OfferReservationDTO>> getReservationsByUser(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(reservationService.getReservationsByUser(userId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OfferReservationDTO> updateReservationStatus(
            @PathVariable("id") Long id, @RequestParam("status") ReservationStatus status) {
        return ResponseEntity.ok(reservationService.updateReservationStatus(id, status));
    }
}
