package ma.safar.morocco.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;

import java.io.UnsupportedEncodingException;
import org.springframework.scheduling.annotation.Async;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Async
    public void sendVerificationEmail(String toEmail, String nom, String token) {
        try {
            String subject = "Vérifiez votre adresse email - Safar Morocco";
            String senderName = "Safar Morocco";
            String verificationUrl = frontendUrl + "?token=" + token;

            String mailContent = "<p>Bonjour " + nom + ",</p>";
            mailContent += "<p>Merci de vous être inscrit sur Safar Morocco. Veuillez cliquer sur le lien ci-dessous pour vérifier votre adresse email.</p>";
            mailContent += "<h3><a href=\"" + verificationUrl + "\">VÉRIFIER MON EMAIL</a></h3>";
            mailContent += "<p>Ce lien expirera dans 24 heures.</p>";
            mailContent += "<p>Si vous n'avez pas créé de compte, vous pouvez ignorer cet email.</p>";
            mailContent += "<br><p>L'équipe Safar Morocco</p>";

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("contact@safarmorocco.ma", senderName); // Will use the authenticated Gmail account
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(mailContent, true); // true indicates HTML content

            mailSender.send(message);
            log.info("Email de vérification envoyé à {}", toEmail);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Erreur lors de l'envoi de l'email de vérification à {}: {}", toEmail, e.getMessage());
            throw new IllegalStateException("Erreur lors de l'envoi de l'email", e);
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            String subject = "Réinitialisation de votre mot de passe - Safar Morocco";
            String senderName = "Safar Morocco";
            String resetUrl = "http://localhost:4200/auth/reset-password?token=" + token;

            String mailContent = "<p>Bonjour,</p>";
            mailContent += "<p>Vous avez demandé la réinitialisation de votre mot de passe. Veuillez cliquer sur le lien ci-dessous pour choisir un nouveau mot de passe :</p>";
            mailContent += "<h3><a href=\"" + resetUrl + "\">RÉINITIALISER MON MOT DE PASSE</a></h3>";
            mailContent += "<p>Ce lien expirera dans 15 minutes.</p>";
            mailContent += "<p>Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet email.</p>";
            mailContent += "<br><p>L'équipe Safar Morocco</p>";

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("contact@safarmorocco.ma", senderName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(mailContent, true);

            mailSender.send(message);
            log.info("Email de réinitialisation de mot de passe envoyé à {}", toEmail);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Erreur lors de l'envoi de l'email de réinitialisation à {}: {}", toEmail, e.getMessage());
            throw new IllegalStateException("Erreur lors de l'envoi de l'email", e);
        }
    }
}
