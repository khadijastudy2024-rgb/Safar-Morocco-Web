package ma.safar.morocco.security.entity;

import jakarta.persistence.*;
import lombok.*;
import ma.safar.morocco.user.entity.Utilisateur;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity: AuditLog
 * Enregistre toutes les actions importantes de l'application
 * - Connexions/Déconnexions
 * - Modifications de données
 * - Suppressions
 * - Accès aux ressources sensibles
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_action", columnList = "action"),
        @Index(name = "idx_created_date", columnList = "created_date"),
        @Index(name = "idx_entity_type", columnList = "entity_type")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Utilisateur performedBy;

    @Column(nullable = true, length = 100)
    private String userEmail;

    @Column(nullable = false, length = 50)
    private String action; // LOGIN, LOGOUT, CREATE, UPDATE, DELETE, ACCESS

    @Column(length = 100)
    private String entityType; // Utilisateur, Destination, Avis, etc.

    @Column
    private Long entityId;

    @Column(length = 500)
    private String description;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(columnDefinition = "LONGTEXT")
    private String oldValues; // JSON format

    @Column(columnDefinition = "LONGTEXT")
    private String newValues; // JSON format

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "timestamp", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AuditStatus status = AuditStatus.SUCCESS;

    @Column(length = 1000)
    private String errorMessage;

    public enum AuditStatus {
        SUCCESS, FAILURE, ERROR
    }
}