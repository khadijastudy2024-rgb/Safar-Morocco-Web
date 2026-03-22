package ma.safar.morocco.util;

import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import lombok.extern.slf4j.Slf4j;

/**
 * Utilitaire de diagnostic pour vérifier le format du QR code 2FA
 */
@Slf4j
public class QrCodeDiagnostic {

    public static void main(String[] args) {
        try {
            log.info("=== QR Code 2FA Diagnostic ===");
            
            // Générer un secret
            String secret = new DefaultSecretGenerator().generate();
            log.info("Secret généré: {}", secret);
            log.info("Secret length: {}", secret.length());
            
            // Vérifier que c'est du Base32
            boolean isBase32 = secret.matches("[A-Z2-7]+");
            log.info("Secret est Base32: {}", isBase32);
            
            // Créer l'URI otpauth
            QrData data = new QrData.Builder()
                    .label("test@safar-morocco.com")
                    .secret(secret)
                    .issuer("Safar Morocco")
                    .digits(6)
                    .period(30)
                    .build();
            
            String otpauthUri = data.getUri();
            log.info("\n=== OTP Auth URI ===");
            log.info("Full URI: {}", otpauthUri);
            
            // Validation
            validateUri(otpauthUri);
            
        } catch (Exception e) {
            log.error("Erreur diagnostic", e);
        }
    }
    
    private static void validateUri(String uri) {
        log.info("\n=== Validation URI ===");
        
        // Check 1: Commence par otpauth://totp/
        boolean startsCorrectly = uri.startsWith("otpauth://totp/");
        log.info("✓ Commence par otpauth://totp/: {}", startsCorrectly);
        
        // Check 2: Contient secret=
        boolean hasSecret = uri.contains("secret=");
        log.info("✓ Contient 'secret=': {}", hasSecret);
        
        // Check 3: Contient issuer=
        boolean hasIssuer = uri.contains("issuer=");
        log.info("✓ Contient 'issuer=': {}", hasIssuer);
        
        // Check 4: Contient digits=6
        boolean hasDigits = uri.contains("digits=6");
        log.info("✓ Contient 'digits=6': {}", hasDigits);
        
        // Check 5: Pas d'espaces
        boolean noSpaces = !uri.contains(" ");
        log.info("✓ Pas d'espaces: {}", noSpaces);
        
        // Extract secret from URI
        int secretStart = uri.indexOf("secret=") + 7;
        int secretEnd = uri.indexOf("&", secretStart);
        if (secretEnd == -1) {
            secretEnd = uri.length();
        }
        String extractedSecret = uri.substring(secretStart, secretEnd);
        log.info("\nSecret extrait de l'URI: {}", extractedSecret);
        log.info("Secret est Base32: {}", extractedSecret.matches("[A-Z2-7]+"));
    }
}
