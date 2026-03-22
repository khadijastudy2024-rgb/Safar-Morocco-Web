package ma.safar.morocco.destination.repository;

import ma.safar.morocco.destination.entity.Destination;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long> {
    List<Destination> findByCategorie(String categorie);

    boolean existsByNom(String nom);

    java.util.Optional<Destination> findByNom(String nom);

    @EntityGraph(attributePaths = { "medias" })
    @org.springframework.data.jpa.repository.Query("SELECT d FROM Destination d")
    List<Destination> findAllWithMedias();
}
