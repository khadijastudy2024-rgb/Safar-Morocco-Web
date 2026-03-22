package ma.safar.morocco.reservation.service;

import lombok.RequiredArgsConstructor;
import ma.safar.morocco.event.entity.EvenementCulturel;
import ma.safar.morocco.event.repository.EvenementCulturelRepository;
import ma.safar.morocco.reservation.entity.Reservation;
import ma.safar.morocco.reservation.repository.ReservationRepository;
import ma.safar.morocco.security.service.AuditService;
import ma.safar.morocco.user.entity.Utilisateur;
import ma.safar.morocco.user.service.UtilisateurService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    public static final String ENTITY_NAME = "Reservation";

    private final ReservationRepository reservationRepository;
    private final EvenementCulturelRepository evenementRepository;
    private final UtilisateurService utilisateurService;
    private final AuditService auditService;

    @Transactional
    public Reservation createReservation(Long evenementId) {
        Utilisateur currentUser = utilisateurService.getCurrentUser();

        Optional<Reservation> existingOpt = reservationRepository.findByUtilisateurIdAndEvenementId(currentUser.getId(),
                evenementId);

        if (existingOpt.isPresent()) {
            Reservation existing = existingOpt.get();
            if ("CONFIRMED".equals(existing.getStatus())) {
                throw new IllegalStateException("Vous avez déjà réservé cet événement.");
            } else {
                // It's cancelled, reactivate it
                existing.setStatus("CONFIRMED");
                Reservation saved = reservationRepository.save(existing);
                auditService.logAction(currentUser.getId(), "RESERVATION_CREATED", ENTITY_NAME, saved.getId(),
                        "Reservation reactivated for event: " + saved.getEvenement().getNom());
                return saved;
            }
        }

        EvenementCulturel event = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new IllegalArgumentException("Événement non trouvé"));

        Reservation reservation = Reservation.builder()
                .utilisateur(currentUser)
                .evenement(event)
                .build();

        Reservation saved = reservationRepository.save(reservation);
        auditService.logAction(currentUser.getId(), "RESERVATION_CREATED", ENTITY_NAME, saved.getId(),
                "Reservation created for event: " + event.getNom());
        return saved;
    }

    @Transactional
    public void cancelReservation(Long reservationId) {
        Utilisateur currentUser = utilisateurService.getCurrentUser();
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        if (!reservation.getUtilisateur().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Unauthorized to cancel this reservation");
        }

        if ("CANCELLED".equals(reservation.getStatus())) {
            throw new IllegalStateException("Reservation is already cancelled");
        }

        reservation.setStatus("CANCELLED");
        reservationRepository.save(reservation);
        auditService.logAction(currentUser.getId(), "RESERVATION_CANCELLED", ENTITY_NAME, reservation.getId(),
                "Reservation cancelled for event: " + reservation.getEvenement().getNom());
    }

    public List<Reservation> getMyReservations() {
        Utilisateur currentUser = utilisateurService.getCurrentUser();
        return reservationRepository.findByUtilisateurId(currentUser.getId());
    }
}
