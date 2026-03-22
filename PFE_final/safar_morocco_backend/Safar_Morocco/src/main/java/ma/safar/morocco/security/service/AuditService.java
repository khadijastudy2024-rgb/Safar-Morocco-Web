package ma.safar.morocco.security.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.safar.morocco.security.entity.AuditLog;
import ma.safar.morocco.security.repository.AuditLogRepository;
import ma.safar.morocco.user.entity.Utilisateur;
import ma.safar.morocco.user.repository.UtilisateurRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service: AuditService
 * Gestion complète des logs d'audit
 * - Enregistrement des actions
 * - Consultation des logs
 * - Détection des anomalies
 * - Monitoring de la sécurité
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private static final String USER_AGENT_HEADER = "User-Agent";

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final UtilisateurRepository utilisateurRepository;

    /**
     * Enregistre une action d'audit (asynchrone)
     */
    @Async
    public void logAction(Long userId, String action, String entityType, Long entityId, String description) {
        try {
            HttpServletRequest request = getCurrentRequest();
            Utilisateur user = userId != null ? utilisateurRepository.findById(userId).orElse(null) : null;

            AuditLog auditLog = AuditLog.builder()
                    .performedBy(user)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .ipAddress(request != null ? getClientIpAddress(request) : null)
                    .userAgent(request != null ? request.getHeader(USER_AGENT_HEADER) : null)
                    .status(AuditLog.AuditStatus.SUCCESS)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Action logged: {} by user {}", action, userId);
        } catch (Exception e) {
            log.error("Error logging action", e);
        }
    }

    /**
     * Enregistre une action avec détails des modifications
     */
    @Async
    @Transactional
    public void logActionWithDetails(Long userId, String action, String entityType, Long entityId,
            Object oldValues, Object newValues) {
        try {
            HttpServletRequest request = getCurrentRequest();
            Utilisateur user = userId != null ? utilisateurRepository.findById(userId).orElse(null) : null;

            AuditLog auditLog = AuditLog.builder()
                    .performedBy(user)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValues(oldValues != null ? objectMapper.writeValueAsString(oldValues) : null)
                    .newValues(newValues != null ? objectMapper.writeValueAsString(newValues) : null)
                    .ipAddress(request != null ? getClientIpAddress(request) : null)
                    .userAgent(request != null ? request.getHeader(USER_AGENT_HEADER) : null)
                    .status(AuditLog.AuditStatus.SUCCESS)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Action logged with details: {} by user {}", action, userId);
        } catch (Exception e) {
            log.error("Error logging action with details", e);
        }
    }

    /**
     * Enregistre une action échouée
     */
    @Async
    public void logFailedAction(Long userId, String action, String errorMessage) {
        try {
            HttpServletRequest request = getCurrentRequest();
            Utilisateur user = userId != null ? utilisateurRepository.findById(userId).orElse(null) : null;

            AuditLog auditLog = AuditLog.builder()
                    .performedBy(user)
                    .action(action)
                    .status(AuditLog.AuditStatus.FAILURE)
                    .errorMessage(errorMessage)
                    .ipAddress(request != null ? getClientIpAddress(request) : null)
                    .userAgent(request != null ? request.getHeader(USER_AGENT_HEADER) : null)
                    .build();

            auditLogRepository.save(auditLog);
            log.warn("Failed action logged: {} by user {}: {}", action, userId, errorMessage);
        } catch (Exception e) {
            log.error("Error logging failed action", e);
        }
    }

    /**
     * Récupère les logs pour un utilisateur
     */
    public List<AuditLog> getUserLogs(Long userId) {
        return auditLogRepository.findByPerformedByIdOrderByCreatedDateDesc(userId);
    }

    /**
     * Récupère les logs pour un utilisateur (paginés)
     */
    public Page<AuditLog> getUserLogsPaginated(Long userId, Pageable pageable) {
        return auditLogRepository.findByPerformedById(userId, pageable);
    }

    /**
     * Récupère les logs d'une action spécifique
     */
    public List<AuditLog> getActionLogs(String action) {
        return auditLogRepository.findByActionOrderByCreatedDateDesc(action);
    }

    /**
     * Récupère les logs entre deux dates
     */
    public List<AuditLog> getLogsInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByCreatedDateBetween(startDate, endDate);
    }

    /**
     * Récupère les logs entre deux dates (paginés)
     */
    public Page<AuditLog> getLogsInDateRangePaginated(LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable) {
        return auditLogRepository.findLogsInDateRange(startDate, endDate, pageable);
    }

    /**
     * Récupère les logs pour un utilisateur entre deux dates
     */
    public List<AuditLog> getUserLogsInDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findUserLogsInDateRange(userId, startDate, endDate);
    }

    /**
     * Récupère les logs par type d'entité
     */
    public List<AuditLog> getEntityTypeLogs(String entityType) {
        return auditLogRepository.findByEntityType(entityType);
    }

    /**
     * Récupère les logs échoués
     */
    public List<AuditLog> getFailedActions() {
        return auditLogRepository.findFailedActions();
    }

    /**
     * Récupère les logs par statut
     */
    public List<AuditLog> getLogsByStatus(AuditLog.AuditStatus status) {
        return auditLogRepository.findByStatus(status);
    }

    /**
     * Compte les actions échouées
     */
    public long countFailedActions() {
        return auditLogRepository.countFailedActions();
    }

    /**
     * Compte les actions récentes d'un utilisateur
     */
    public long countRecentUserActions(Long userId, LocalDateTime since) {
        return auditLogRepository.countRecentActionsByUser(userId, since);
    }

    /**
     * Détecte les activités suspectes (trop d'actions en peu de temps)
     */
    public boolean detectSuspiciousActivity(Long userId) {
        LocalDateTime lastHour = LocalDateTime.now().minusHours(1);
        long recentActions = countRecentUserActions(userId, lastHour);
        return recentActions > 100; // Plus de 100 actions en 1h = suspect
    }

    /**
     * Récupère les logs récents (avec limite)
     */
    public List<AuditLog> getRecentLogs(int limit) {
        return auditLogRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedDate().compareTo(a.getCreatedDate()))
                .limit(limit)
                .toList();
    }

    /**
     * Supprime les logs anciens (pour maintenance - rétention RGPD)
     */
    @Transactional
    public long deleteOldLogs(LocalDateTime beforeDate) {
        List<AuditLog> oldLogs = auditLogRepository.findByCreatedDateBetween(LocalDateTime.MIN, beforeDate);
        long count = oldLogs.size();
        auditLogRepository.deleteAll(oldLogs);
        log.info("Deleted {} old audit logs", count);
        return count;
    }

    /**
     * Récupère l'adresse IP du client
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    /**
     * Récupère la requête HTTP courante
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs != null ? attrs.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
}