package ma.safar.morocco.review.controller;

import lombok.RequiredArgsConstructor;
import ma.safar.morocco.review.dto.ReviewResponseDTO;
import ma.safar.morocco.review.entity.Avis;
import ma.safar.morocco.review.service.AvisService;
import ma.safar.morocco.user.service.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/avis")
@RequiredArgsConstructor
public class AvisController {

    private final AvisService avisService;
    private final UtilisateurService utilisateurService;

    /**
     * GET /api/avis/destination/{destinationId}
     * Récupère tous les avis pour une destination (publique)
     */
    @GetMapping("/destination/{destinationId}")
    public ResponseEntity<List<ReviewResponseDTO>> getAvisByDestination(
            @PathVariable("destinationId") Long destinationId) {
        List<ReviewResponseDTO> avis = avisService.findByDestinationDTO(destinationId);
        return ResponseEntity.ok(avis);
    }

    /**
     * GET /api/avis
     * Récupère tous les avis (admin seulement)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReviewResponseDTO>> getAllReviews() {
        return ResponseEntity.ok(avisService.findAllDTO());
    }

    /**
     * GET /api/avis/{id}
     * Récupère un avis par ID (publique)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Avis> getAvisById(@PathVariable("id") Long id) {
        return avisService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST /api/avis
     * Crée un nouvel avis (utilisateur authentifié)
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @SuppressWarnings("java:S4684")
    public ResponseEntity<Avis> createAvis(@RequestBody Avis avis) {
        if (avis.getDestination() == null || avis.getDestination().getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        avis.setAuteur(utilisateurService.getCurrentUser());
        Avis created = avisService.addAvis(avis);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/avis/{id}
     * Met à jour un avis (propriétaire ou admin)
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @SuppressWarnings("java:S4684")
    public ResponseEntity<Avis> updateAvis(
            @PathVariable("id") Long id,
            @RequestBody Avis avis) {
        Avis updated = avisService.update(id, avis);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/avis/{id}
     * Supprime un avis (propriétaire ou admin)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAvis(@PathVariable("id") Long id) {
        avisService.deleteAvis(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/avis/destination/{destinationId}/average
     * Calcule la moyenne des notes (publique)
     */
    @GetMapping("/destination/{destinationId}/average")
    public ResponseEntity<Double> getAverageRating(@PathVariable("destinationId") Long destinationId) {
        double average = avisService.calculateAverageRating(destinationId);
        return ResponseEntity.ok(average);
    }

    /**
     * GET /api/avis/destination/{destinationId}/count
     * Compte les avis (publique)
     */
    @GetMapping("/destination/{destinationId}/count")
    public ResponseEntity<Long> countAvis(@PathVariable("destinationId") Long destinationId) {
        long count = avisService.countByDestination(destinationId);
        return ResponseEntity.ok(count);
    }
}
