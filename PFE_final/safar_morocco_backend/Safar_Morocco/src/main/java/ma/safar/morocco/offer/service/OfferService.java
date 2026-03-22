package ma.safar.morocco.offer.service;

import ma.safar.morocco.offer.dto.OfferDTO;
import java.util.List;

public interface OfferService {
    OfferDTO createOffer(OfferDTO offerDTO);

    OfferDTO getOfferById(Long id);

    List<OfferDTO> getOffersByDestination(Long destinationId);

    List<OfferDTO> getAllOffers();

    OfferDTO updateOffer(Long id, OfferDTO offerDTO);

    void deleteOffer(Long id);
}
