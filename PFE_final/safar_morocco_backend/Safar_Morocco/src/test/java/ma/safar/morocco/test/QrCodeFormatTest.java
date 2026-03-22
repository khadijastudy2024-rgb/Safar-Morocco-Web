package ma.safar.morocco.test;

import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test simple pour vérifier le format OTP Auth URI
 */
class QrCodeFormatTest {
    
    @Test
    void testQrDataFormat() {
        // 1. Générer secret
        String secret = new DefaultSecretGenerator().generate();
        assertNotNull(secret, "Secret should not be null");
        assertFalse(secret.isEmpty(), "Secret should not be empty");
        assertTrue(secret.matches("[A-Z2-7]+"), "Secret should be Base32");
        
        // 2. Créer QrData exactement comme dans le code
        QrData data = new QrData.Builder()
                .label("admin@safar-morocco.com")
                .secret(secret)
                .issuer("Safar Morocco")
                .digits(6)
                .period(30)
                .build();
        
        String otpauthUri = data.getUri();
        
        // 3. Validation
        assertNotNull(otpauthUri, "URI should not be null");
        assertTrue(otpauthUri.startsWith("otpauth://totp/"), "URI should start with otpauth://totp/");
        assertTrue(otpauthUri.contains("secret="), "URI should contain secret");
        assertTrue(otpauthUri.contains("issuer="), "URI should contain issuer");
        assertTrue(otpauthUri.contains("digits=6"), "URI should contain digits=6");
        assertTrue(otpauthUri.contains("period=30"), "URI should contain period=30");
        assertFalse(otpauthUri.contains(" "), "URI should not contain spaces");
        
        // 4. Extract and validate secret
        int secretStart = otpauthUri.indexOf("secret=") + 7;
        int secretEnd = otpauthUri.indexOf("&", secretStart);
        if (secretEnd == -1) secretEnd = otpauthUri.length();
        String extractedSecret = otpauthUri.substring(secretStart, secretEnd);
        
        assertEquals(secret, extractedSecret, "Extracted secret should match generated secret");
    }
}
