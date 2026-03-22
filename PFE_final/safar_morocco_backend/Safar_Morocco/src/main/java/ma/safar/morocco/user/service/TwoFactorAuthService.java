package ma.safar.morocco.user.service;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.safar.morocco.user.entity.TwoFactorAuth;
import ma.safar.morocco.user.entity.Utilisateur;
import ma.safar.morocco.user.repository.TwoFactorAuthRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Base64;

/**
 * Service: TwoFactorAuthService
 * Gère l'authentification à deux facteurs (2FA)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TwoFactorAuthService {

    private final TwoFactorAuthRepository twoFactorAuthRepository;
    private final CodeVerifier codeVerifier = createConfiguredVerifier();

    private static CodeVerifier createConfiguredVerifier() {
        DefaultCodeVerifier verifier = new DefaultCodeVerifier(
                new DefaultCodeGenerator(),
                new SystemTimeProvider());
        verifier.setAllowedTimePeriodDiscrepancy(4); // Allow +/- 2 minutes (4 * 30s period)
        return verifier;
    }

    /**
     * Génère un nouveau secret 2FA et retourne le QR code
     */
    public TwoFactorAuth generateSecretAndQrCode(Utilisateur utilisateur) throws QrGenerationException {
        // Générer un secret
        String secret = new DefaultSecretGenerator().generate();

        // Créer les données du QR code
        QrData data = new QrData.Builder()
                .label(utilisateur.getEmail())
                .secret(secret)
                .issuer("Safar Morocco")
                .digits(6)
                .period(30)
                .build();

        // Log la URI otpauth générée pour diagnostic
        String otpauthUri = data.getUri();
        log.info("OTP Auth URI: {}", otpauthUri);
        log.info("Secret: {}", secret);
        log.info("User: {}", utilisateur.getEmail());

        // Générer le QR code en PNG
        QrGenerator generator = new ZxingPngQrGenerator();
        byte[] imageData = generator.generate(data);
        // Format correct du data-uri
        String base64Png = Base64.getEncoder().encodeToString(imageData);
        String qrCodeUrl = "data:image/png;base64," + base64Png;

        // Chercher ou créer TwoFactorAuth
        Optional<TwoFactorAuth> existing = twoFactorAuthRepository.findByUtilisateur(utilisateur);
        TwoFactorAuth twoFA;

        if (existing.isPresent()) {
            twoFA = existing.get();
            twoFA.setSecretKey(secret);
            twoFA.setQrCodeUrl(qrCodeUrl);
            twoFA.setConfirmed(false);
            twoFA.setEnabled(false);
        } else {
            twoFA = TwoFactorAuth.builder()
                    .utilisateur(utilisateur)
                    .secretKey(secret)
                    .qrCodeUrl(qrCodeUrl)
                    .enabled(false)
                    .confirmed(false)
                    .failedAttempts(0)
                    .locked(false)
                    .build();
        }

        twoFactorAuthRepository.save(twoFA);
        log.info("Generated 2FA secret and QR code for user: {}", utilisateur.getEmail());

        return twoFA;
    }

    /**
     * Vérifie le code 2FA fourni par l'utilisateur
     */
    public boolean verifyCode(Utilisateur utilisateur, String code) {
        Optional<TwoFactorAuth> twoFA = twoFactorAuthRepository.findByUtilisateur(utilisateur);

        if (twoFA.isEmpty()) {
            log.warn("No 2FA configuration found for user: {}", utilisateur.getEmail());
            return false;
        }

        TwoFactorAuth twoFactorAuth = twoFA.get();

        // Vérifier si le compte est bloqué
        if (Boolean.TRUE.equals(twoFactorAuth.getLocked())) {
            log.warn("2FA account locked for user: {}", utilisateur.getEmail());
            return false;
        }

        // Vérifier le code
        boolean isValid = codeVerifier.isValidCode(twoFactorAuth.getSecretKey(), code);
        log.info("🔍 2FA Verification: User={}, Code={}, IsValid={}", utilisateur.getEmail(), code, isValid);

        if (isValid) {
            // Réinitialiser les tentatives échouées
            twoFactorAuth.setFailedAttempts(0);
            twoFactorAuth.setLastUsedAt(java.time.LocalDateTime.now());
            twoFactorAuthRepository.save(twoFactorAuth);
            log.info("Valid 2FA code for user: {}", utilisateur.getEmail());
        } else {
            // Incrémenter les tentatives échouées
            int newFailedAttempts = twoFactorAuth.getFailedAttempts() + 1;
            twoFactorAuth.setFailedAttempts(newFailedAttempts);

            // Bloquer après 5 tentatives échouées
            if (newFailedAttempts >= 5) {
                twoFactorAuth.setLocked(true);
                log.warn("2FA account locked after 5 failed attempts for user: {}", utilisateur.getEmail());
            }

            twoFactorAuthRepository.save(twoFactorAuth);
            log.warn("Invalid 2FA code for user: {} (attempt {})", utilisateur.getEmail(), newFailedAttempts);
        }

        return isValid;
    }

    /**
     * Active le 2FA pour un utilisateur après vérification du code
     */
    public void enableTwoFactorAuth(Utilisateur utilisateur, String code) {
        if (!verifyCode(utilisateur, code)) {
            throw new IllegalArgumentException("Code 2FA invalide");
        }

        Optional<TwoFactorAuth> twoFA = twoFactorAuthRepository.findByUtilisateur(utilisateur);
        if (twoFA.isPresent()) {
            TwoFactorAuth twoFactorAuth = twoFA.get();
            twoFactorAuth.setEnabled(true);
            twoFactorAuth.setConfirmed(true);
            twoFactorAuthRepository.save(twoFactorAuth);
            log.info("2FA enabled for user: {}", utilisateur.getEmail());
        }
    }

    /**
     * Désactive le 2FA pour un utilisateur
     */
    public void disableTwoFactorAuth(Utilisateur utilisateur) {
        Optional<TwoFactorAuth> twoFA = twoFactorAuthRepository.findByUtilisateur(utilisateur);
        if (twoFA.isPresent()) {
            TwoFactorAuth twoFactorAuth = twoFA.get();
            twoFactorAuth.setEnabled(false);
            twoFactorAuth.setConfirmed(false);
            twoFactorAuth.setFailedAttempts(0);
            twoFactorAuth.setLocked(false);
            twoFactorAuthRepository.save(twoFactorAuth);
            log.info("2FA disabled for user: {}", utilisateur.getEmail());
        }
    }

    /**
     * Vérifie si le 2FA est activé pour un utilisateur
     */
    public boolean isTwoFactorEnabled(Utilisateur utilisateur) {
        if (utilisateur == null || utilisateur.getId() == null) {
            return false;
        }
        return twoFactorAuthRepository.findByUtilisateurId(utilisateur.getId())
                .map(TwoFactorAuth::getEnabled)
                .orElse(false);
    }

    /**
     * Obtient la configuration 2FA d'un utilisateur
     */
    public Optional<TwoFactorAuth> getTwoFactorAuth(Utilisateur utilisateur) {
        return twoFactorAuthRepository.findByUtilisateur(utilisateur);
    }
}
