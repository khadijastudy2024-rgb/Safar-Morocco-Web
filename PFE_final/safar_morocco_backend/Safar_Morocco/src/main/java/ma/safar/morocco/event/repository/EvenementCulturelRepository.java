package ma.safar.morocco.event.repository;

import ma.safar.morocco.event.entity.EvenementCulturel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository: EvenementCulturelRepository
 * Gère la persistence des événements culturels
 */
@Repository
public interface EvenementCulturelRepository extends JpaRepository<EvenementCulturel, Long> {

    List<EvenementCulturel> findByDestinationId(Long destinationId);

    boolean existsByNom(String nom);

    Optional<EvenementCulturel> findByNom(String nom);

    @Query("SELECT e FROM EvenementCulturel e WHERE e.dateDebut > CURRENT_TIMESTAMP ORDER BY e.dateDebut ASC")
    List<EvenementCulturel> findUpcomingEvents();

    @Query("SELECT e FROM EvenementCulturel e WHERE e.dateDebut <= CURRENT_TIMESTAMP AND e.dateFin >= CURRENT_TIMESTAMP ORDER BY e.dateDebut ASC")
    List<EvenementCulturel> findOngoingEvents();

    @Query("SELECT e FROM EvenementCulturel e WHERE e.dateFin < CURRENT_TIMESTAMP ORDER BY e.dateDebut DESC")
    List<EvenementCulturel> findPastEvents();

    List<EvenementCulturel> findByEventType(String eventType);

    List<EvenementCulturel> findByLieu(String lieu);

    @Query("SELECT e FROM EvenementCulturel e WHERE e.dateDebut BETWEEN :dateDebut AND :dateFin ORDER BY e.dateDebut ASC")
    List<EvenementCulturel> findByDateRange(@Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin);

    Long countByDestinationId(Long destinationId);

    Long countByEventType(String eventType);
}
