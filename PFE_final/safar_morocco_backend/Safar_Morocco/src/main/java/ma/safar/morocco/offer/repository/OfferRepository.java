package ma.safar.morocco.offer.repository;

import ma.safar.morocco.offer.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
    List<Offer> findByDestinationId(Long destinationId);

    List<Offer> findByDestinationIdAndDeletedFalse(Long destinationId);
}
