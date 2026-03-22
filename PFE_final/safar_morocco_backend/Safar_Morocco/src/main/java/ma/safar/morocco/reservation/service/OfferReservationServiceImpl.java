package ma.safar.morocco.reservation.service;

import lombok.RequiredArgsConstructor;
import ma.safar.morocco.itinerary.entity.Itineraire;
import ma.safar.morocco.itinerary.repository.ItineraireRepository;
import ma.safar.morocco.offer.entity.Offer;
import ma.safar.morocco.offer.repository.OfferRepository;
import ma.safar.morocco.reservation.dto.OfferReservationDTO;
import ma.safar.morocco.reservation.entity.OfferReservation;
import ma.safar.morocco.reservation.enums.ReservationStatus;
import ma.safar.morocco.reservation.repository.OfferReservationRepository;
import ma.safar.morocco.user.entity.Utilisateur;
import ma.safar.morocco.user.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OfferReservationServiceImpl implements OfferReservationService {

        private final OfferReservationRepository reservationRepository;
        private final UtilisateurRepository userRepository;
        private final ItineraireRepository itineraryRepository;
        private final OfferRepository offerRepository;

        @Override
        @Transactional
        @SuppressWarnings("java:S3776")
        public OfferReservationDTO createReservation(OfferReservationDTO dto) {
                Utilisateur user = userRepository.findById(dto.getUserId())
                                .orElseThrow(() -> new RuntimeException("User not found"));
                Itineraire itinerary = itineraryRepository.findById(dto.getItineraryId())
                                .orElseThrow(() -> new RuntimeException("Itinerary not found"));
                Offer offer = offerRepository.findById(dto.getOffer().getId())
                                .orElseThrow(() -> new RuntimeException("Offer not found"));

                // Date Validations
                if (dto.getStartDate() != null && dto.getEndDate() != null) {
                        if (dto.getStartDate().isAfter(dto.getEndDate())) {
                                throw new IllegalArgumentException("La date de début doit être avant la date de fin.");
                        }
                } else if (offer.getType() == ma.safar.morocco.offer.enums.OfferType.HOTEL
                                && (dto.getStartDate() == null || dto.getEndDate() == null)) {
                        throw new IllegalArgumentException(
                                        "Les dates de début et de fin sont obligatoires pour les réservations d'hôtel.");
                }

                // Calculate total price based on type
                Double calculatedTotalPrice = 0.0;
                int qty = dto.getQuantity() != null ? dto.getQuantity() : 1;

                switch (offer.getType()) {
                        case HOTEL:
                                if (dto.getStartDate() != null && dto.getEndDate() != null
                                                && dto.getStartDate().isBefore(dto.getEndDate())) {
                                        long nights = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate());
                                        if (offer.getPricePerNight() != null) {
                                                calculatedTotalPrice = offer.getPricePerNight() * nights;
                                        }
                                }
                                break;
                        case ACTIVITY, RESTAURANT: // price is base price or average price
                                Double basePrice = offer.getPrice();
                                if (basePrice == null) {
                                        basePrice = offer.getAveragePrice() != null ? offer.getAveragePrice() : 0.0;
                                }
                                calculatedTotalPrice = basePrice * qty;
                                break;
                }

                OfferReservation reservation = OfferReservation.builder()
                                .user(user)
                                .itinerary(itinerary)
                                .offer(offer)
                                .startDate(dto.getStartDate())
                                .endDate(dto.getEndDate())
                                .quantity(qty)
                                .totalPrice(calculatedTotalPrice)
                                .status(ReservationStatus.PENDING)
                                .build();

                OfferReservation saved = reservationRepository.save(reservation);
                return mapToDTO(saved);
        }

        @Override
        public OfferReservationDTO getReservationById(Long id) {
                OfferReservation reservation = reservationRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Reservation not found"));
                return mapToDTO(reservation);
        }

        @Override
        public List<OfferReservationDTO> getReservationsByItinerary(Long itineraryId) {
                return reservationRepository.findByItineraryId(itineraryId).stream()
                                .map(this::mapToDTO)
                                .toList();
        }

        @Override
        public List<OfferReservationDTO> getAllReservations() {
                return reservationRepository.findAll().stream()
                                .map(this::mapToDTO)
                                .toList();
        }

        @Override
        public List<OfferReservationDTO> getReservationsByUser(Long userId) {
                return reservationRepository.findByUserId(userId).stream()
                                .map(this::mapToDTO)
                                .toList();
        }

        @Override
        @Transactional
        public OfferReservationDTO updateReservationStatus(Long id, ReservationStatus status) {
                OfferReservation reservation = reservationRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Reservation not found"));
                reservation.setStatus(status);
                return mapToDTO(reservationRepository.save(reservation));
        }

        private OfferReservationDTO mapToDTO(OfferReservation entity) {
                return OfferReservationDTO.builder()
                                .id(entity.getId())
                                .userId(entity.getUser().getId())
                                .itineraryId(entity.getItinerary().getId())
                                .offer(ma.safar.morocco.offer.dto.OfferDTO.builder()
                                                .id(entity.getOffer().getId())
                                                .name(entity.getOffer().getName())
                                                .type(entity.getOffer().getType())
                                                .price(entity.getOffer().getPrice())
                                                .averagePrice(entity.getOffer().getAveragePrice())
                                                .pricePerNight(entity.getOffer().getPricePerNight())
                                                .build())
                                .startDate(entity.getStartDate())
                                .endDate(entity.getEndDate())
                                .quantity(entity.getQuantity())
                                .totalPrice(entity.getTotalPrice())
                                .status(entity.getStatus())
                                .build();
        }
}
