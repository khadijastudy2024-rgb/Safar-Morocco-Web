package ma.safar.morocco.review.repository;

import ma.safar.morocco.review.entity.Avis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvisRepository extends JpaRepository<Avis, Long> {
    List<Avis> findByDestinationId(Long destinationId);

    List<Avis> findByAuteurId(Long auteurId);

    @Query("SELECT AVG(a.note) FROM Avis a WHERE a.destination.id = :destinationId")
    Double getAverageRating(@Param("destinationId") Long destinationId);

    @Query("SELECT COUNT(a) FROM Avis a WHERE a.destination.id = :destinationId")
    Long getReviewCount(@Param("destinationId") Long destinationId);
}
