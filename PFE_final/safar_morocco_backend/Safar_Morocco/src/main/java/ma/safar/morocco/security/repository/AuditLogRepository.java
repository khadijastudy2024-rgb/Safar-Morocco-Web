package ma.safar.morocco.security.repository;

import ma.safar.morocco.security.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository: AuditLogRepository
 * Accès aux données pour les logs d'audit
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

        /**
         * Récupère les logs par utilisateur, triés par date décroissante
         */
        List<AuditLog> findByPerformedByIdOrderByCreatedDateDesc(Long userId);

        /**
         * Récupère les logs paginés par utilisateur
         */
        Page<AuditLog> findByPerformedById(Long userId, Pageable pageable);

        /**
         * Récupère les logs par action, triés par date décroissante
         */
        List<AuditLog> findByActionOrderByCreatedDateDesc(String action);

        /**
         * Récupère les logs entre deux dates
         */
        List<AuditLog> findByCreatedDateBetween(LocalDateTime start, LocalDateTime end);

        /**
         * Récupère les logs par type d'entité
         */
        List<AuditLog> findByEntityType(String entityType);

        /**
         * Récupère les logs par statut
         */
        List<AuditLog> findByStatus(AuditLog.AuditStatus status);

        /**
         * Récupère les logs échoués
         */
        @Query("SELECT a FROM AuditLog a WHERE a.status = 'FAILURE' OR a.status = 'ERROR' ORDER BY a.createdDate DESC")
        List<AuditLog> findFailedActions();

        /**
         * Récupère les logs pour un utilisateur entre deux dates
         */
        @Query("SELECT a FROM AuditLog a WHERE a.performedBy.id = :userId AND a.createdDate BETWEEN :startDate AND :endDate")
        List<AuditLog> findUserLogsInDateRange(
                        @Param("userId") Long userId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Récupère les logs entre deux dates (paginés)
         */
        @Query("SELECT a FROM AuditLog a WHERE a.createdDate BETWEEN :startDate AND :endDate ORDER BY a.createdDate DESC")
        Page<AuditLog> findLogsInDateRange(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        /**
         * Compte les logs par action pour un utilisateur
         */
        long countByPerformedByIdAndAction(Long userId, String action);

        /**
         * Compte les actions récentes par utilisateur
         */
        @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.performedBy.id = :userId AND a.createdDate > :since")
        long countRecentActionsByUser(@Param("userId") Long userId, @Param("since") LocalDateTime since);

        /**
         * Compte les actions échouées
         */
        @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.status = 'FAILURE' OR a.status = 'ERROR'")
        long countFailedActions();
}