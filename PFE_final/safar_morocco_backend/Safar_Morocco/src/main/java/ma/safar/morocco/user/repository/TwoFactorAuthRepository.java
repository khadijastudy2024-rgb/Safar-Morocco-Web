package ma.safar.morocco.user.repository;

import ma.safar.morocco.user.entity.TwoFactorAuth;
import ma.safar.morocco.user.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository: TwoFactorAuthRepository
 */
@Repository
public interface TwoFactorAuthRepository extends JpaRepository<TwoFactorAuth, Long> {
    Optional<TwoFactorAuth> findByUtilisateur(Utilisateur utilisateur);
    Optional<TwoFactorAuth> findByUtilisateurId(Long utilisateurId);
}
