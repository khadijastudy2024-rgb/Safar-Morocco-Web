package ma.safar.morocco.offer.service;

import lombok.RequiredArgsConstructor;
import ma.safar.morocco.destination.entity.Destination;
import ma.safar.morocco.destination.repository.DestinationRepository;
import ma.safar.morocco.offer.dto.OfferDTO;
import ma.safar.morocco.offer.entity.Offer;
import ma.safar.morocco.offer.repository.OfferRepository;
import org.springframework.context.i18n.LocaleContextHolder;
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

        offer.setNameEn(offerDTO.getNameEn());
        offer.setNameFr(offerDTO.getNameFr());
        offer.setNameAr(offerDTO.getNameAr());
        offer.setNameEs(offerDTO.getNameEs());

        offer.setDescriptionEn(offerDTO.getDescriptionEn());
        offer.setDescriptionFr(offerDTO.getDescriptionFr());
        offer.setDescriptionAr(offerDTO.getDescriptionAr());
        offer.setDescriptionEs(offerDTO.getDescriptionEs());

        offer.setPrice(offerDTO.getPrice());
        offer.setType(offerDTO.getType());
        offer.setAvailable(offerDTO.getAvailable());
        offer.setStars(offerDTO.getStars());

        offer.setRoomTypeEn(offerDTO.getRoomTypeEn());
        offer.setRoomTypeFr(offerDTO.getRoomTypeFr());
        offer.setRoomTypeAr(offerDTO.getRoomTypeAr());
        offer.setRoomTypeEs(offerDTO.getRoomTypeEs());

        offer.setPricePerNight(offerDTO.getPricePerNight());

        offer.setCuisineTypeEn(offerDTO.getCuisineTypeEn());
        offer.setCuisineTypeFr(offerDTO.getCuisineTypeFr());
        offer.setCuisineTypeAr(offerDTO.getCuisineTypeAr());
        offer.setCuisineTypeEs(offerDTO.getCuisineTypeEs());

        offer.setAveragePrice(offerDTO.getAveragePrice());

        offer.setDurationEn(offerDTO.getDurationEn());
        offer.setDurationFr(offerDTO.getDurationFr());
        offer.setDurationAr(offerDTO.getDurationAr());
        offer.setDurationEs(offerDTO.getDurationEs());

        offer.setActivityTypeEn(offerDTO.getActivityTypeEn());
        offer.setActivityTypeFr(offerDTO.getActivityTypeFr());
        offer.setActivityTypeAr(offerDTO.getActivityTypeAr());
        offer.setActivityTypeEs(offerDTO.getActivityTypeEs());

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
                .nameEn(dto.getNameEn())
                .nameFr(dto.getNameFr())
                .nameAr(dto.getNameAr())
                .nameEs(dto.getNameEs())
                .descriptionEn(dto.getDescriptionEn())
                .descriptionFr(dto.getDescriptionFr())
                .descriptionAr(dto.getDescriptionAr())
                .descriptionEs(dto.getDescriptionEs())
                .price(dto.getPrice())
                .type(dto.getType())
                .available(dto.getAvailable())
                .stars(dto.getStars())
                .roomTypeEn(dto.getRoomTypeEn())
                .roomTypeFr(dto.getRoomTypeFr())
                .roomTypeAr(dto.getRoomTypeAr())
                .roomTypeEs(dto.getRoomTypeEs())
                .pricePerNight(dto.getPricePerNight())
                .cuisineTypeEn(dto.getCuisineTypeEn())
                .cuisineTypeFr(dto.getCuisineTypeFr())
                .cuisineTypeAr(dto.getCuisineTypeAr())
                .cuisineTypeEs(dto.getCuisineTypeEs())
                .averagePrice(dto.getAveragePrice())
                .durationEn(dto.getDurationEn())
                .durationFr(dto.getDurationFr())
                .durationAr(dto.getDurationAr())
                .durationEs(dto.getDurationEs())
                .activityTypeEn(dto.getActivityTypeEn())
                .activityTypeFr(dto.getActivityTypeFr())
                .activityTypeAr(dto.getActivityTypeAr())
                .activityTypeEs(dto.getActivityTypeEs())
                .deleted(dto.getDeleted())
                .build();
    }

    private OfferDTO mapToDTO(Offer offer) {
        String lang = LocaleContextHolder.getLocale().getLanguage();
        
        String name = offer.getNameEn();
        String desc = offer.getDescriptionEn();
        String roomType = offer.getRoomTypeEn();
        String cuisineType = offer.getCuisineTypeEn();
        String duration = offer.getDurationEn();
        String activityType = offer.getActivityTypeEn();
        
        if ("fr".equals(lang)) {
            if (offer.getNameFr() != null) name = offer.getNameFr();
            if (offer.getDescriptionFr() != null) desc = offer.getDescriptionFr();
            if (offer.getRoomTypeFr() != null) roomType = offer.getRoomTypeFr();
            if (offer.getCuisineTypeFr() != null) cuisineType = offer.getCuisineTypeFr();
            if (offer.getDurationFr() != null) duration = offer.getDurationFr();
            if (offer.getActivityTypeFr() != null) activityType = offer.getActivityTypeFr();
        } else if ("ar".equals(lang)) {
            if (offer.getNameAr() != null) name = offer.getNameAr();
            if (offer.getDescriptionAr() != null) desc = offer.getDescriptionAr();
            if (offer.getRoomTypeAr() != null) roomType = offer.getRoomTypeAr();
            if (offer.getCuisineTypeAr() != null) cuisineType = offer.getCuisineTypeAr();
            if (offer.getDurationAr() != null) duration = offer.getDurationAr();
            if (offer.getActivityTypeAr() != null) activityType = offer.getActivityTypeAr();
        } else if ("es".equals(lang)) {
            if (offer.getNameEs() != null) name = offer.getNameEs();
            if (offer.getDescriptionEs() != null) desc = offer.getDescriptionEs();
            if (offer.getRoomTypeEs() != null) roomType = offer.getRoomTypeEs();
            if (offer.getCuisineTypeEs() != null) cuisineType = offer.getCuisineTypeEs();
            if (offer.getDurationEs() != null) duration = offer.getDurationEs();
            if (offer.getActivityTypeEs() != null) activityType = offer.getActivityTypeEs();
        }

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
                .name(name)
                .nameEn(offer.getNameEn())
                .nameFr(offer.getNameFr())
                .nameAr(offer.getNameAr())
                .nameEs(offer.getNameEs())
                .description(desc)
                .descriptionEn(offer.getDescriptionEn())
                .descriptionFr(offer.getDescriptionFr())
                .descriptionAr(offer.getDescriptionAr())
                .descriptionEs(offer.getDescriptionEs())
                .price(offer.getPrice())
                .type(offer.getType())
                .destinationId(offer.getDestination() != null ? offer.getDestination().getId() : null)
                .available(offer.isAvailable())
                .stars(offer.getStars())
                .roomType(roomType)
                .roomTypeEn(offer.getRoomTypeEn())
                .roomTypeFr(offer.getRoomTypeFr())
                .roomTypeAr(offer.getRoomTypeAr())
                .roomTypeEs(offer.getRoomTypeEs())
                .pricePerNight(offer.getPricePerNight())
                .cuisineType(cuisineType)
                .cuisineTypeEn(offer.getCuisineTypeEn())
                .cuisineTypeFr(offer.getCuisineTypeFr())
                .cuisineTypeAr(offer.getCuisineTypeAr())
                .cuisineTypeEs(offer.getCuisineTypeEs())
                .averagePrice(offer.getAveragePrice())
                .displayPrice(displayPrice)
                .duration(duration)
                .durationEn(offer.getDurationEn())
                .durationFr(offer.getDurationFr())
                .durationAr(offer.getDurationAr())
                .durationEs(offer.getDurationEs())
                .activityType(activityType)
                .activityTypeEn(offer.getActivityTypeEn())
                .activityTypeFr(offer.getActivityTypeFr())
                .activityTypeAr(offer.getActivityTypeAr())
                .activityTypeEs(offer.getActivityTypeEs())
                .deleted(offer.isDeleted())
                .build();
    }
}
