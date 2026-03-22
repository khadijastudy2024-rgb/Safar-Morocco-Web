package ma.safar.morocco.destination.service;

import lombok.RequiredArgsConstructor;
import ma.safar.morocco.destination.dto.DestinationResponseDTO;
import ma.safar.morocco.destination.entity.Destination;
import ma.safar.morocco.destination.repository.DestinationRepository;
import ma.safar.morocco.review.repository.AvisRepository;
import ma.safar.morocco.security.service.AuditService;
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
        existing.setNom(updated.getNom());
        existing.setDescription(updated.getDescription());
        existing.setLatitude(updated.getLatitude());
        existing.setLongitude(updated.getLongitude());
        existing.setCategorie(updated.getCategorie());
        existing.setBestTime(updated.getBestTime());
        existing.setLanguages(updated.getLanguages());
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

        return DestinationResponseDTO.builder()
                .id(d.getId())
                .nom(d.getNom())
                .description(d.getDescription())
                .histoire(d.getHistoire())
                .historicalDescription(d.getHistoricalDescription())
                .type(d.getType())
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
                .bestTime(d.getBestTime())
                .languages(d.getLanguages())
                .averageCost(d.getAverageCost())
                .videoUrl(d.getVideoUrl())
                .build();
    }
}
