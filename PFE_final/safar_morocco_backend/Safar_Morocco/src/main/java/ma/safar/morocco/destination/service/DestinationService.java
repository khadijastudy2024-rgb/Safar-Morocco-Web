package ma.safar.morocco.destination.service;

import lombok.RequiredArgsConstructor;
import ma.safar.morocco.destination.dto.DestinationResponseDTO;
import ma.safar.morocco.destination.entity.Destination;
import ma.safar.morocco.destination.repository.DestinationRepository;
import ma.safar.morocco.review.repository.AvisRepository;
import ma.safar.morocco.security.service.AuditService;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DestinationService {
    private final DestinationRepository destinationRepository;
    private final AvisRepository avisRepository;
    private final AuditService auditService;

    private static final String DESTINATION_CONST = "Destination";

    public List<Destination> findAll() {
        return destinationRepository.findAll();
    }

    public List<DestinationResponseDTO> findAllDTO() {
        return destinationRepository.findAll().stream()
                .map(this::convertToDTO)
                .toList();
    }

    public Optional<Destination> findById(Long id) {
        return destinationRepository.findById(id);
    }

    public Optional<DestinationResponseDTO> findByIdDTO(Long id) {
        return destinationRepository.findById(id).map(this::convertToDTO);
    }

    @Transactional
    public Destination create(Destination d) {
        Destination created = destinationRepository.save(d);
        auditService.logAction(null, "DESTINATION_CREATED", DESTINATION_CONST, created.getId(),
                "Created destination: " + created.getNom());
        return created;
    }

    @Transactional
    public Destination update(Long id, Destination updated) {
        Destination existing = destinationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Destination non trouvée"));
        existing.setNomEn(updated.getNomEn());
        existing.setNomFr(updated.getNomFr());
        existing.setNomAr(updated.getNomAr());
        existing.setNomEs(updated.getNomEs());
        
        existing.setDescriptionEn(updated.getDescriptionEn());
        existing.setDescriptionFr(updated.getDescriptionFr());
        existing.setDescriptionAr(updated.getDescriptionAr());
        existing.setDescriptionEs(updated.getDescriptionEs());
        
        existing.setHistoireEn(updated.getHistoireEn());
        existing.setHistoireFr(updated.getHistoireFr());
        existing.setHistoireAr(updated.getHistoireAr());
        existing.setHistoireEs(updated.getHistoireEs());
        
        existing.setHistoricalDescriptionEn(updated.getHistoricalDescriptionEn());
        existing.setHistoricalDescriptionFr(updated.getHistoricalDescriptionFr());
        existing.setHistoricalDescriptionAr(updated.getHistoricalDescriptionAr());
        existing.setHistoricalDescriptionEs(updated.getHistoricalDescriptionEs());
        
        existing.setTypeEn(updated.getTypeEn());
        existing.setTypeFr(updated.getTypeFr());
        existing.setTypeAr(updated.getTypeAr());
        existing.setTypeEs(updated.getTypeEs());

        existing.setLatitude(updated.getLatitude());
        existing.setLongitude(updated.getLongitude());
        existing.setCategorie(updated.getCategorie());
        
        existing.setBestTimeEn(updated.getBestTimeEn());
        existing.setBestTimeFr(updated.getBestTimeFr());
        existing.setBestTimeAr(updated.getBestTimeAr());
        existing.setBestTimeEs(updated.getBestTimeEs());
        
        existing.setLanguagesEn(updated.getLanguagesEn());
        existing.setLanguagesFr(updated.getLanguagesFr());
        existing.setLanguagesAr(updated.getLanguagesAr());
        existing.setLanguagesEs(updated.getLanguagesEs());
        
        existing.setAverageCost(updated.getAverageCost());
        existing.setVideoUrl(updated.getVideoUrl());
        Destination result = destinationRepository.save(existing);
        auditService.logAction(null, "DESTINATION_UPDATED", DESTINATION_CONST, id,
                "Updated destination: " + result.getNom());
        return result;
    }

    public List<Destination> findByCategorie(String categorie) {
        return destinationRepository.findByCategorie(categorie);
    }

    public List<DestinationResponseDTO> findByCategorieDTO(String categorie) {
        return destinationRepository.findByCategorie(categorie).stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        if (!destinationRepository.existsById(id)) {
            throw new IllegalArgumentException("Destination non trouvée");
        }
        auditService.logAction(null, "DESTINATION_DELETED", DESTINATION_CONST, id, "Deleted destination ID: " + id);
        destinationRepository.deleteById(id);
    }

    @Transactional
    public void incrementViewCount(Long id) {
        Destination destination = destinationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Destination non trouvée"));
        destination.setViewCount(destination.getViewCount() == null ? 1 : destination.getViewCount() + 1);
        destinationRepository.save(destination);
    }

    public DestinationResponseDTO convertToDTO(Destination d) {
        if (d == null)
            return null;
        Double avgRating = avisRepository.getAverageRating(d.getId());
        Long reviewCount = avisRepository.getReviewCount(d.getId());

        String lang = LocaleContextHolder.getLocale().getLanguage();

        String nom = d.getNomEn();
        String desc = d.getDescriptionEn();
        String hist = d.getHistoireEn();
        String hDesc = d.getHistoricalDescriptionEn();
        String type = d.getTypeEn();
        String bestTime = d.getBestTimeEn();
        String languages = d.getLanguagesEn();

        if ("fr".equals(lang)) {
            if (d.getNomFr() != null) nom = d.getNomFr();
            if (d.getDescriptionFr() != null) desc = d.getDescriptionFr();
            if (d.getHistoireFr() != null) hist = d.getHistoireFr();
            if (d.getHistoricalDescriptionFr() != null) hDesc = d.getHistoricalDescriptionFr();
            if (d.getTypeFr() != null) type = d.getTypeFr();
            if (d.getBestTimeFr() != null) bestTime = d.getBestTimeFr();
            if (d.getLanguagesFr() != null) languages = d.getLanguagesFr();
        } else if ("ar".equals(lang)) {
            if (d.getNomAr() != null) nom = d.getNomAr();
            if (d.getDescriptionAr() != null) desc = d.getDescriptionAr();
            if (d.getHistoireAr() != null) hist = d.getHistoireAr();
            if (d.getHistoricalDescriptionAr() != null) hDesc = d.getHistoricalDescriptionAr();
            if (d.getTypeAr() != null) type = d.getTypeAr();
            if (d.getBestTimeAr() != null) bestTime = d.getBestTimeAr();
            if (d.getLanguagesAr() != null) languages = d.getLanguagesAr();
        } else if ("es".equals(lang)) {
            if (d.getNomEs() != null) nom = d.getNomEs();
            if (d.getDescriptionEs() != null) desc = d.getDescriptionEs();
            if (d.getHistoireEs() != null) hist = d.getHistoireEs();
            if (d.getHistoricalDescriptionEs() != null) hDesc = d.getHistoricalDescriptionEs();
            if (d.getTypeEs() != null) type = d.getTypeEs();
            if (d.getBestTimeEs() != null) bestTime = d.getBestTimeEs();
            if (d.getLanguagesEs() != null) languages = d.getLanguagesEs();
        }

        return DestinationResponseDTO.builder()
                .id(d.getId())
                .nom(nom)
                .nomEn(d.getNomEn())
                .nomFr(d.getNomFr())
                .nomAr(d.getNomAr())
                .nomEs(d.getNomEs())
                .description(desc)
                .descriptionEn(d.getDescriptionEn())
                .descriptionFr(d.getDescriptionFr())
                .descriptionAr(d.getDescriptionAr())
                .descriptionEs(d.getDescriptionEs())
                .histoire(hist)
                .histoireEn(d.getHistoireEn())
                .histoireFr(d.getHistoireFr())
                .histoireAr(d.getHistoireAr())
                .histoireEs(d.getHistoireEs())
                .historicalDescription(hDesc)
                .historicalDescriptionEn(d.getHistoricalDescriptionEn())
                .historicalDescriptionFr(d.getHistoricalDescriptionFr())
                .historicalDescriptionAr(d.getHistoricalDescriptionAr())
                .historicalDescriptionEs(d.getHistoricalDescriptionEs())
                .type(type)
                .typeEn(d.getTypeEn())
                .typeFr(d.getTypeFr())
                .typeAr(d.getTypeAr())
                .typeEs(d.getTypeEs())
                .latitude(d.getLatitude())
                .longitude(d.getLongitude())
                .categorie(d.getCategorie())
                .viewCount(d.getViewCount())
                .averageRating(avgRating != null ? avgRating : 0.0)
                .reviewCount(reviewCount != null ? reviewCount : 0L)
                .thumbnailUrl(d.getMedias() != null && !d.getMedias().isEmpty() ? d.getMedias().get(0).getUrl() : null)
                .medias(d.getMedias() != null ? d.getMedias().stream()
                        .map(m -> ma.safar.morocco.media.dto.MediaDTO.builder()
                                .id(m.getId())
                                .url(m.getUrl())
                                .type(m.getType())
                                .build())
                        .toList() : java.util.Collections.emptyList())
                .imageUrls(
                        d.getMedias() != null ? d.getMedias().stream().map(ma.safar.morocco.media.entity.Media::getUrl)
                                .toList()
                                : java.util.Collections.emptyList())
                .bestTime(bestTime)
                .bestTimeEn(d.getBestTimeEn())
                .bestTimeFr(d.getBestTimeFr())
                .bestTimeAr(d.getBestTimeAr())
                .bestTimeEs(d.getBestTimeEs())
                .languages(languages)
                .languagesEn(d.getLanguagesEn())
                .languagesFr(d.getLanguagesFr())
                .languagesAr(d.getLanguagesAr())
                .languagesEs(d.getLanguagesEs())
                .averageCost(d.getAverageCost())
                .videoUrl(d.getVideoUrl())
                .build();
    }
}
