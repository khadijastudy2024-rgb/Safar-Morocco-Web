package ma.safar.morocco.destination.controller;

import lombok.RequiredArgsConstructor;
import ma.safar.morocco.destination.dto.DestinationResponseDTO;
import ma.safar.morocco.destination.entity.Destination;
import ma.safar.morocco.destination.service.DestinationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/destinations")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService destinationService;

    /**
     * GET /api/destinations
     * Récupère toutes les destinations (publique)
     */
    @GetMapping
    public ResponseEntity<List<DestinationResponseDTO>> getAllDestinations() {
        List<DestinationResponseDTO> destinations = destinationService.findAllDTO();
        return ResponseEntity.ok(destinations);
    }

    /**
     * GET /api/destinations/{id}
     * Récupère une destination par ID (publique)
     */
    @GetMapping("/{id}")
    public ResponseEntity<DestinationResponseDTO> getDestinationById(@PathVariable("id") Long id) {
        destinationService.incrementViewCount(id);
        return destinationService.findByIdDTO(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/destinations/category/{category}
     * Récupère les destinations par catégorie (publique)
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<DestinationResponseDTO>> getDestinationsByCategory(@PathVariable("category") String category) {
        List<DestinationResponseDTO> destinations = destinationService.findByCategorieDTO(category);
        return ResponseEntity.ok(destinations);
    }

    /**
     * POST /api/destinations
     * Crée une nouvelle destination (admin seulement)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SuppressWarnings("java:S4684")
    public ResponseEntity<Destination> createDestination(@RequestBody Destination destination) {
        Destination created = destinationService.create(destination);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/destinations/{id}
     * Met à jour une destination (admin seulement)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SuppressWarnings("java:S4684")
    public ResponseEntity<Destination> updateDestination(
            @PathVariable("id") Long id,
            @RequestBody Destination destination) {
        Destination updated = destinationService.update(id, destination);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/destinations/{id}
     * Supprime une destination (admin seulement)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDestination(@PathVariable("id") Long id) {
        destinationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
