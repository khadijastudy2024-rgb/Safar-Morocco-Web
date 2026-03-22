package ma.safar.morocco.offer.controller;

import lombok.RequiredArgsConstructor;
import ma.safar.morocco.offer.dto.OfferDTO;
import ma.safar.morocco.offer.service.OfferService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OfferController {

    private final OfferService offerService;

    @PostMapping
    public ResponseEntity<OfferDTO> createOffer(@RequestBody OfferDTO offerDTO) {
        return new ResponseEntity<>(offerService.createOffer(offerDTO), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OfferDTO>> getAllOffers() {
        return ResponseEntity.ok(offerService.getAllOffers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfferDTO> getOfferById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(offerService.getOfferById(id));
    }

    @GetMapping("/destination/{destinationId}")
    public ResponseEntity<List<OfferDTO>> getOffersByDestination(@PathVariable("destinationId") Long destinationId) {
        return ResponseEntity.ok(offerService.getOffersByDestination(destinationId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OfferDTO> updateOffer(@PathVariable("id") Long id, @RequestBody OfferDTO offerDTO) {
        return ResponseEntity.ok(offerService.updateOffer(id, offerDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable("id") Long id) {
        offerService.deleteOffer(id);
        return ResponseEntity.noContent().build();
    }
}
