package ma.safar.morocco.review.service;

import lombok.RequiredArgsConstructor;
import ma.safar.morocco.review.dto.ReviewResponseDTO;
import ma.safar.morocco.review.entity.Avis;
import ma.safar.morocco.review.repository.AvisRepository;
import ma.safar.morocco.security.service.AuditService;
import ma.safar.morocco.user.service.UtilisateurService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AvisService {
    private final AvisRepository avisRepository;
    private final AuditService auditService;
    private final UtilisateurService utilisateurService;

    public List<Avis> findByDestination(Long destinationId) {
        return avisRepository.findByDestinationId(destinationId);
    }

    public List<ReviewResponseDTO> findByDestinationDTO(Long destinationId) {
        return findByDestination(destinationId).stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<ReviewResponseDTO> findAllDTO() {
        return avisRepository.findAll().stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional
    public Avis addAvis(Avis a) {
        return avisRepository.save(a);
    }

    @Transactional
    public void deleteAvis(Long id) {
        Long currentUserId = (utilisateurService.getCurrentUser() != null) ? utilisateurService.getCurrentUser().getId()
                : null;
        auditService.logAction(currentUserId, "REVIEW_DELETED", "Avis", id, "Review deleted");
        avisRepository.deleteById(id);
    }

    @Transactional
    public Avis update(Long id, Avis updated) {
        Avis existing = avisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Avis non trouvé"));
        if (updated.getCommentaire() != null) {
            existing.setCommentaire(updated.getCommentaire());
        }
        if (updated.getNote() != null) {
            existing.setNote(updated.getNote());
        }
        if (updated.getStatus() != null) {
            existing.setStatus(updated.getStatus());
        }

        // Log action
        Long currentUserId = (utilisateurService.getCurrentUser() != null) ? utilisateurService.getCurrentUser().getId()
                : null;
        auditService.logAction(currentUserId, "REVIEW_UPDATED", "Avis", id,
                "Review updated (status: " + existing.getStatus() + ")");

        return avisRepository.save(existing);
    }

    public Optional<Avis> findById(Long id) {
        return avisRepository.findById(id);
    }

    public double calculateAverageRating(Long destinationId) {
        List<Avis> avisList = findByDestination(destinationId);
        if (avisList.isEmpty())
            return 0.0;
        return avisList.stream()
                .mapToInt(Avis::getNote)
                .average()
                .orElse(0.0);
    }

    public long countByDestination(Long destinationId) {
        return findByDestination(destinationId).size();
    }

    public ReviewResponseDTO convertToDTO(Avis avis) {
        if (avis == null)
            return null;
        
        String travelerName = "Unknown";
        if (avis.getAuteur() != null) {
            travelerName = avis.getAuteur().getNom() != null ? avis.getAuteur().getNom() : avis.getAuteur().getEmail();
        }

        return ReviewResponseDTO.builder()
                .id(avis.getId())
                .commentaire(avis.getCommentaire())
                .datePublication(avis.getDatePublication())
                .note(avis.getNote())
                .status(avis.getStatus())
                .travelerName(travelerName)
                .travelerPhotoUrl(avis.getAuteur() != null ? avis.getAuteur().getPhotoUrl() : null)
                .destinationName(avis.getDestination() != null ? avis.getDestination().getNom() : "Unknown")
                .build();
    }
}
