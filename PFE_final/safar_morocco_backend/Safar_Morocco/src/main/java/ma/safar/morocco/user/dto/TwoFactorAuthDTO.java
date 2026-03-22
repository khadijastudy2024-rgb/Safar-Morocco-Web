package ma.safar.morocco.user.dto;

import lombok.*;

/**
 * DTO: TwoFactorAuthDTO
 * Pour les requêtes/réponses 2FA
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorAuthDTO {

    private Long id;
    private String secretKey;
    private String qrCodeUrl;
    private Boolean enabled;
    private Boolean confirmed;
    private Integer failedAttempts;
    private Boolean locked;
}

/**
 * DTO: TwoFactorVerifyDTO
 * Pour vérifier le code 2FA
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class TwoFactorVerifyDTO {
    private String code;
}

/**
 * DTO: TwoFactorSetupDTO
 * Pour configurer le 2FA
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class TwoFactorSetupDTO {
    private String qrCodeUrl;
    private String secretKey;
    private String message;
}
