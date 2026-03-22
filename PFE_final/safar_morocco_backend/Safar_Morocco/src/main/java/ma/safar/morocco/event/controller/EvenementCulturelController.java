package ma.safar.morocco.event.controller;

import lombok.RequiredArgsConstructor;
import ma.safar.morocco.event.dto.EvenementResponseDTO;
import ma.safar.morocco.event.entity.EvenementCulturel;
import ma.safar.morocco.event.service.EvenementCulturelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evenements")
@RequiredArgsConstructor
public class EvenementCulturelController {
    
    private final EvenementCulturelService evenementService;
    
    /**
     * GET /api/evenements
     * Récupère tous les événements (publique)
     */
    @GetMapping
    public ResponseEntity<List<EvenementResponseDTO>> getAllEvenements() {
        List<EvenementResponseDTO> evenements = evenementService.findAll();
        return ResponseEntity.ok(evenements);
    }
    
    /**
     * GET /api/evenements/{id}
     * Récupère un événement par ID (publique)
     */
    @GetMapping("/{id}")
    public ResponseEntity<EvenementResponseDTO> getEvenementById(@PathVariable("id") Long id) {
        return evenementService.findByIdDTO(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * GET /api/evenements/destination/{destinationId}
     * Récupère les événements d'une destination (publique)
     */
    @GetMapping("/destination/{destinationId}")
    public ResponseEntity<List<EvenementResponseDTO>> getEvenementsByDestination(@PathVariable("destinationId") Long destinationId) {
        List<EvenementResponseDTO> evenements = evenementService.findByDestinationId(destinationId);
        return ResponseEntity.ok(evenements);
    }
    
    /**
     * GET /api/evenements/upcoming
     * Récupère les événements à venir (publique)
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<EvenementResponseDTO>> getUpcomingEvenements() {
        List<EvenementResponseDTO> evenements = evenementService.findUpcoming();
        return ResponseEntity.ok(evenements);
    }
    
    /**
     * GET /api/evenements/ongoing
     * Récupère les événements en cours (publique)
     */
    @GetMapping("/ongoing")
    public ResponseEntity<List<EvenementResponseDTO>> getOngoingEvenements() {
        List<EvenementResponseDTO> evenements = evenementService.findOngoing();
        return ResponseEntity.ok(evenements);
    }
    
    /**
     * GET /api/evenements/past
     * Récupère les événements passés (publique)
     */
    @GetMapping("/past")
    public ResponseEntity<List<EvenementResponseDTO>> getPastEvenements() {
        List<EvenementResponseDTO> evenements = evenementService.findPast();
        return ResponseEntity.ok(evenements);
    }
    
    /**
     * GET /api/evenements/type/{type}
     * Récupère les événements par type (publique)
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<EvenementResponseDTO>> getEvenementsByType(@PathVariable("type") String type) {
        List<EvenementResponseDTO> evenements = evenementService.findByType(type);
        return ResponseEntity.ok(evenements);
    }
    
    /**
     * GET /api/evenements/lieu/{lieu}
     * Récupère les événements par lieu (publique)
     */
    @GetMapping("/lieu/{lieu}")
    public ResponseEntity<List<EvenementCulturel>> getEvenementsByLieu(@PathVariable("lieu") String lieu) {
        List<EvenementCulturel> evenements = evenementService.findByLieu(lieu);
        return ResponseEntity.ok(evenements);
    }
    
    /**
     * POST /api/evenements/destination/{destinationId}
     * Crée un nouvel événement (admin seulement)
     */
    @PostMapping("/destination/{destinationId}")
    @PreAuthorize("hasRole('ADMIN')")
    @SuppressWarnings("java:S4684")
    public ResponseEntity<EvenementCulturel> createEvenement(
            @PathVariable("destinationId") Long destinationId,
            @RequestBody EvenementCulturel evenement) {
        EvenementCulturel created = evenementService.create(destinationId, evenement);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    /**
     * PUT /api/evenements/{id}
     * Met à jour un événement (admin seulement)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SuppressWarnings("java:S4684")
    public ResponseEntity<EvenementCulturel> updateEvenement(
            @PathVariable("id") Long id,
            @RequestBody EvenementCulturel evenement) {
        EvenementCulturel updated = evenementService.update(id, evenement);
        return ResponseEntity.ok(updated);
    }
    
    /**
     * DELETE /api/evenements/{id}
     * Supprime un événement (admin seulement)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEvenement(@PathVariable("id") Long id) {
        evenementService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * GET /api/evenements/destination/{destinationId}/count
     * Compte les événements d'une destination (publique)
     */
    @GetMapping("/destination/{destinationId}/count")
    public ResponseEntity<Long> countEvenementsByDestination(@PathVariable("destinationId") Long destinationId) {
        long count = evenementService.countByDestinationId(destinationId);
        return ResponseEntity.ok(count);
    }
}
