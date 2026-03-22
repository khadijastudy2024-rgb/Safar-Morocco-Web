package ma.safar.morocco.offer.service;

import lombok.RequiredArgsConstructor;
import ma.safar.morocco.destination.entity.Destination;
import ma.safar.morocco.destination.repository.DestinationRepository;
import ma.safar.morocco.offer.dto.OfferDTO;
import ma.safar.morocco.offer.entity.Offer;
import ma.safar.morocco.offer.repository.OfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OfferServiceImpl implements OfferService {

    private static final String OFFER_NOT_FOUND_MSG = "Offer not found with id: ";

    private final OfferRepository offerRepository;
    private final DestinationRepository destinationRepository;

    @Override
    @Transactional
    public OfferDTO createOffer(OfferDTO offerDTO) {
        Destination destination = destinationRepository.findById(offerDTO.getDestinationId())
                .orElseThrow(
                        () -> new RuntimeException("Destination not found with id: " + offerDTO.getDestinationId()));

        Offer offer = mapToEntity(offerDTO);
        offer.setDestination(destination);

        Offer savedOffer = offerRepository.save(offer);
        return mapToDTO(savedOffer);
    }

    @Override
    public OfferDTO getOfferById(Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(OFFER_NOT_FOUND_MSG + id));
        return mapToDTO(offer);
    }

    @Override
    public List<OfferDTO> getOffersByDestination(Long destinationId) {
        return offerRepository.findByDestinationIdAndDeletedFalse(destinationId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<OfferDTO> getAllOffers() {
        return offerRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional
    public OfferDTO updateOffer(Long id, OfferDTO offerDTO) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(OFFER_NOT_FOUND_MSG + id));

        offer.setName(offerDTO.getName());
        offer.setDescription(offerDTO.getDescription());
        offer.setPrice(offerDTO.getPrice());
        offer.setType(offerDTO.getType());
        offer.setAvailable(offerDTO.getAvailable());
        offer.setStars(offerDTO.getStars());
        offer.setRoomType(offerDTO.getRoomType());
        offer.setPricePerNight(offerDTO.getPricePerNight());
        offer.setCuisineType(offerDTO.getCuisineType());
        offer.setAveragePrice(offerDTO.getAveragePrice());
        offer.setDuration(offerDTO.getDuration());
        offer.setActivityType(offerDTO.getActivityType());
        offer.setDeleted(offerDTO.getDeleted());

        if (offerDTO.getDestinationId() != null
                && !offerDTO.getDestinationId().equals(offer.getDestination().getId())) {
            Destination newDestination = destinationRepository.findById(offerDTO.getDestinationId())
                    .orElseThrow(() -> new RuntimeException(
                            "Destination not found with id: " + offerDTO.getDestinationId()));
            offer.setDestination(newDestination);
        }

        Offer updatedOffer = offerRepository.save(offer);
        return mapToDTO(updatedOffer);
    }

    @Override
    @Transactional
    public void deleteOffer(Long id) {
        Offer offer = offerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(OFFER_NOT_FOUND_MSG + id));
        offer.setDeleted(true);
        offerRepository.save(offer);
    }

    private Offer mapToEntity(OfferDTO dto) {
        return Offer.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .type(dto.getType())
                .available(dto.getAvailable())
                .stars(dto.getStars())
                .roomType(dto.getRoomType())
                .pricePerNight(dto.getPricePerNight())
                .cuisineType(dto.getCuisineType())
                .averagePrice(dto.getAveragePrice())
                .duration(dto.getDuration())
                .activityType(dto.getActivityType())
                .deleted(dto.getDeleted())
                .build();
    }

    private OfferDTO mapToDTO(Offer offer) {
        Double displayPrice = null;
        if (offer.getType() != null) {
            switch (offer.getType()) {
                case HOTEL -> displayPrice = offer.getPricePerNight();
                case RESTAURANT -> displayPrice = offer.getAveragePrice();
                case ACTIVITY -> displayPrice = offer.getPrice();
            }
        }

        return OfferDTO.builder()
                .id(offer.getId())
                .name(offer.getName())
                .description(offer.getDescription())
                .price(offer.getPrice())
                .type(offer.getType())
                .destinationId(offer.getDestination() != null ? offer.getDestination().getId() : null)
                .available(offer.isAvailable())
                .stars(offer.getStars())
                .roomType(offer.getRoomType())
                .pricePerNight(offer.getPricePerNight())
                .cuisineType(offer.getCuisineType())
                .averagePrice(offer.getAveragePrice())
                .displayPrice(displayPrice)
                .duration(offer.getDuration())
                .activityType(offer.getActivityType())
                .deleted(offer.isDeleted())
                .build();
    }
}
