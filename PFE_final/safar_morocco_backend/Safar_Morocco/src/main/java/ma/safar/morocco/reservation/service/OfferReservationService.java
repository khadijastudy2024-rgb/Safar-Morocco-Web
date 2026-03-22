package ma.safar.morocco.reservation.service;

import ma.safar.morocco.reservation.dto.OfferReservationDTO;
import java.util.List;

public interface OfferReservationService {
    OfferReservationDTO createReservation(OfferReservationDTO dto);

    OfferReservationDTO getReservationById(Long id);

    List<OfferReservationDTO> getAllReservations();

    List<OfferReservationDTO> getReservationsByItinerary(Long itineraryId);

    List<OfferReservationDTO> getReservationsByUser(Long userId);

    OfferReservationDTO updateReservationStatus(Long id, ma.safar.morocco.reservation.enums.ReservationStatus status);
}
