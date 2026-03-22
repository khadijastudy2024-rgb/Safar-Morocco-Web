package ma.safar.morocco.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity: TwoFactorAuth
 * Gère l'authentification à deux facteurs (2FA) via TOTP/QR Code
 */
@Entity
@Table(name = "two_factor_auth")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TwoFactorAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "utilisateur_id", nullable = false, unique = true)
    private Utilisateur utilisateur;

    // Secret key pour TOTP (base32 encoded)
    @Column(nullable = false, length = 100)
    private String secretKey;

    // Code QR en format Base64
    @Column(columnDefinition = "LONGTEXT")
    private String qrCodeUrl;

    // Flag pour indiquer si 2FA est activé
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = false;

    // Flag pour indiquer si l'utilisateur a confirmé la configuration du 2FA
    @Column(nullable = false)
    @Builder.Default
    private Boolean confirmed = false;

    // Codes de secours (backup codes) en cas de perte du téléphone
    @Column(columnDefinition = "LONGTEXT")
    private String backupCodes; // Format JSON: ["code1", "code2", ...]

    // Date de création
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Date de dernière utilisation du 2FA
    private LocalDateTime lastUsedAt;

    // Nombre de tentatives échouées
    @Column(nullable = false)
    @Builder.Default
    private Integer failedAttempts = 0;

    // Flag pour bloquer après plusieurs tentatives échouées
    @Column(nullable = false)
    @Builder.Default
    private Boolean locked = false;
}
