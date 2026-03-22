package ma.safar.morocco.event.service;

import lombok.RequiredArgsConstructor;
import ma.safar.morocco.event.entity.EvenementCulturel;
import ma.safar.morocco.event.repository.EvenementCulturelRepository;
import ma.safar.morocco.destination.entity.Destination;
import ma.safar.morocco.destination.repository.DestinationRepository;
import ma.safar.morocco.event.dto.EvenementResponseDTO;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EvenementCulturelService {

    private final EvenementCulturelRepository evenementRepository;
    private final DestinationRepository destinationRepository;

    /**
     * Récupère tous les événements
     */
    public List<EvenementResponseDTO> findAll() {
        return evenementRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * Récupère un événement par ID
     */
    public Optional<EvenementResponseDTO> findByIdDTO(Long id) {
        return evenementRepository.findById(id).map(this::mapToDTO);
    }

    /**
     * Récupère les événements d'une destination
     */
    public List<EvenementResponseDTO> findByDestinationId(Long destinationId) {
        return evenementRepository.findByDestinationId(destinationId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * Crée un nouvel événement
     */
    @Transactional
    public EvenementCulturel create(Long destinationId, EvenementCulturel evenement) {
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new IllegalArgumentException("Destination non trouvée"));

        evenement.setDestination(destination);
        return evenementRepository.save(evenement);
    }

    /**
     * Met à jour un événement
     */
    @Transactional
    public EvenementCulturel update(Long id, EvenementCulturel updated) {
        EvenementCulturel existing = evenementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Événement non trouvé"));

        existing.setNomEn(updated.getNomEn());
        existing.setNomFr(updated.getNomFr());
        existing.setNomAr(updated.getNomAr());
        existing.setNomEs(updated.getNomEs());

        existing.setDescriptionEn(updated.getDescriptionEn());
        existing.setDescriptionFr(updated.getDescriptionFr());
        existing.setDescriptionAr(updated.getDescriptionAr());
        existing.setDescriptionEs(updated.getDescriptionEs());

        existing.setDateDebut(updated.getDateDebut());
        existing.setDateFin(updated.getDateFin());

        existing.setLieuEn(updated.getLieuEn());
        existing.setLieuFr(updated.getLieuFr());
        existing.setLieuAr(updated.getLieuAr());
        existing.setLieuEs(updated.getLieuEs());

        existing.setEventTypeEn(updated.getEventTypeEn());
        existing.setEventTypeFr(updated.getEventTypeFr());
        existing.setEventTypeAr(updated.getEventTypeAr());
        existing.setEventTypeEs(updated.getEventTypeEs());

        existing.setHistoriqueEn(updated.getHistoriqueEn());
        existing.setHistoriqueFr(updated.getHistoriqueFr());
        existing.setHistoriqueAr(updated.getHistoriqueAr());
        existing.setHistoriqueEs(updated.getHistoriqueEs());

        existing.setImageUrl(updated.getImageUrl());

        return evenementRepository.save(existing);
    }

    /**
     * Supprime un événement
     */
    @Transactional
    public void delete(Long id) {
        if (!evenementRepository.existsById(id)) {
            throw new IllegalArgumentException("Événement non trouvé");
        }
        evenementRepository.deleteById(id);
    }

    /**
     * Récupère les événements à venir
     */
    public List<EvenementResponseDTO> findUpcoming() {
        LocalDateTime now = LocalDateTime.now();
        return evenementRepository.findAll().stream()
                .filter(e -> e.getDateDebut().isAfter(now))
                .sorted((e1, e2) -> e1.getDateDebut().compareTo(e2.getDateDebut()))
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * Récupère les événements en cours
     */
    public List<EvenementResponseDTO> findOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return evenementRepository.findAll().stream()
                .filter(e -> e.getDateDebut().isBefore(now) && e.getDateFin().isAfter(now))
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * Récupère les événements passés
     */
    public List<EvenementResponseDTO> findPast() {
        LocalDateTime now = LocalDateTime.now();
        return evenementRepository.findAll().stream()
                .filter(e -> e.getDateFin().isBefore(now))
                .sorted((e1, e2) -> e2.getDateFin().compareTo(e1.getDateFin()))
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * Récupère les événements par type
     */
    public List<EvenementResponseDTO> findByType(String type) {
        return evenementRepository.findAll().stream()
                .filter(e -> e.getEventTypeEn().equalsIgnoreCase(type) || 
                             (e.getEventTypeFr() != null && e.getEventTypeFr().equalsIgnoreCase(type)))
                .map(this::mapToDTO)
                .toList();
    }

    /**
     * Récupère les événements par lieu
     */
    public List<EvenementCulturel> findByLieu(String lieu) {
        return evenementRepository.findByLieuEn(lieu);
    }

    /**
     * Compte les événements d'une destination
     */
    public long countByDestinationId(Long destinationId) {
        return evenementRepository.findByDestinationId(destinationId).size();
    }

    private EvenementResponseDTO mapToDTO(EvenementCulturel e) {
        String lang = LocaleContextHolder.getLocale().getLanguage();

        String nom = e.getNomEn();
        String desc = e.getDescriptionEn();
        String lieu = e.getLieuEn();
        String eventType = e.getEventTypeEn();
        String hist = e.getHistoriqueEn();

        if ("fr".equals(lang)) {
            if (e.getNomFr() != null) nom = e.getNomFr();
            if (e.getDescriptionFr() != null) desc = e.getDescriptionFr();
            if (e.getLieuFr() != null) lieu = e.getLieuFr();
            if (e.getEventTypeFr() != null) eventType = e.getEventTypeFr();
            if (e.getHistoriqueFr() != null) hist = e.getHistoriqueFr();
        } else if ("ar".equals(lang)) {
            if (e.getNomAr() != null) nom = e.getNomAr();
            if (e.getDescriptionAr() != null) desc = e.getDescriptionAr();
            if (e.getLieuAr() != null) lieu = e.getLieuAr();
            if (e.getEventTypeAr() != null) eventType = e.getEventTypeAr();
            if (e.getHistoriqueAr() != null) hist = e.getHistoriqueAr();
        } else if ("es".equals(lang)) {
            if (e.getNomEs() != null) nom = e.getNomEs();
            if (e.getDescriptionEs() != null) desc = e.getDescriptionEs();
            if (e.getLieuEs() != null) lieu = e.getLieuEs();
            if (e.getEventTypeEs() != null) eventType = e.getEventTypeEs();
            if (e.getHistoriqueEs() != null) hist = e.getHistoriqueEs();
        }

        return EvenementResponseDTO.builder()
                .id(e.getId())
                .nom(nom)
                .nomEn(e.getNomEn())
                .nomFr(e.getNomFr())
                .nomAr(e.getNomAr())
                .nomEs(e.getNomEs())
                .dateDebut(e.getDateDebut())
                .dateFin(e.getDateFin())
                .lieu(lieu)
                .lieuEn(e.getLieuEn())
                .lieuFr(e.getLieuFr())
                .lieuAr(e.getLieuAr())
                .lieuEs(e.getLieuEs())
                .eventType(eventType)
                .eventTypeEn(e.getEventTypeEn())
                .eventTypeFr(e.getEventTypeFr())
                .eventTypeAr(e.getEventTypeAr())
                .eventTypeEs(e.getEventTypeEs())
                .description(desc)
                .descriptionEn(e.getDescriptionEn())
                .descriptionFr(e.getDescriptionFr())
                .descriptionAr(e.getDescriptionAr())
                .descriptionEs(e.getDescriptionEs())
                .historique(hist)
                .historiqueEn(e.getHistoriqueEn())
                .historiqueFr(e.getHistoriqueFr())
                .historiqueAr(e.getHistoriqueAr())
                .historiqueEs(e.getHistoriqueEs())
                .imageUrl(e.getImageUrl())
                .destinationId(e.getDestination() != null ? e.getDestination().getId() : null)
                .build();
    }
}
