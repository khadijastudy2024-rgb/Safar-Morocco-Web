package ma.safar.morocco.reservation.repository;

import ma.safar.morocco.reservation.entity.OfferReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferReservationRepository extends JpaRepository<OfferReservation, Long> {
    List<OfferReservation> findByItineraryId(Long itineraryId);

    List<OfferReservation> findByUserId(Long userId);
}
