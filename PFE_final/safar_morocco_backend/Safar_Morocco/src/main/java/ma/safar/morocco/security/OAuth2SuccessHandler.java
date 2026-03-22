package ma.safar.morocco.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.safar.morocco.security.service.AuditService;
import ma.safar.morocco.user.entity.Utilisateur;
import ma.safar.morocco.user.repository.UtilisateurRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

/**
 * OAuth2SuccessHandler
 * Gère la redirection après authentification OAuth2 (Google)
 * - Création automatique d'utilisateurs
 * - Génération de JWT
 * - Enregistrement d'audit
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String PROVIDER_GOOGLE = "GOOGLE";

    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;
    private final AuditService auditService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            // Récupérer les infos de l'utilisateur Google
            String email = oAuth2User.getAttribute("email");
            String nom = oAuth2User.getAttribute("name");
            String photoUrl = oAuth2User.getAttribute("picture");
            String providerId = oAuth2User.getAttribute("sub");

            log.info("OAuth2 Login attempt for email: {}", email);

            // Chercher l'utilisateur existant d'abord par provider/providerId
            Optional<Utilisateur> existingUser = utilisateurRepository
                    .findByProviderAndProviderId(PROVIDER_GOOGLE, providerId);

            // Si pas trouvé, chercher par email (au cas où il existe déjà avec une
            // tentative échouée)
            if (existingUser.isEmpty()) {
                existingUser = utilisateurRepository.findByEmail(email);
                // Si trouvé par email, mettre à jour le provider et providerId
                if (existingUser.isPresent()) {
                    Utilisateur existingByEmail = existingUser.get();
                    existingByEmail.setProvider(PROVIDER_GOOGLE);
                    existingByEmail.setProviderId(providerId);
                }
            }

            Utilisateur user;
            boolean isNewUser = false;

            if (existingUser.isEmpty()) {
                // Créer un nouvel utilisateur
                user = Utilisateur.builder()
                        .email(email)
                        .nom(nom)
                        .photoUrl(photoUrl)
                        .provider(PROVIDER_GOOGLE)
                        .providerId(providerId)
                        .motDePasseHache("OAUTH2_USER_" + System.currentTimeMillis())
                        .role("USER")
                        .actif(true)
                        .compteBloquer(false)
                        .langue("fr")
                        .build();
                user = utilisateurRepository.save(user);
                isNewUser = true;

                log.info("New OAuth2 user created: {}", email);
            } else {
                user = existingUser.get();

                // Mettre à jour les infos si nécessaire
                if (photoUrl != null && !photoUrl.equals(user.getPhotoUrl())) {
                    user.setPhotoUrl(photoUrl);
                    utilisateurRepository.save(user);
                    log.info("Updated photo URL for user: {}", email);
                }
            }

            // Vérifier que le compte n'est pas bloqué
            if (Boolean.TRUE.equals(user.getCompteBloquer())) {
                log.warn("Blocked account OAuth2 login attempt: {}", email);
                throw new IllegalStateException("Votre compte a été bloqué");
            }

            // Générer les tokens JWT
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            // Enregistrer l'authentification dans l'audit
            auditService.logAction(
                    user.getId(),
                    "LOGIN_OAUTH2",
                    "Utilisateur",
                    user.getId(),
                    "Connexion via Google - " + (isNewUser ? "Nouvel utilisateur" : "Utilisateur existant"));

            log.info("OAuth2 authentication successful for user: {}", email);

            // Rediriger vers le front-end avec les tokens
            String targetUrl = UriComponentsBuilder
                    .fromUriString("http://localhost:4200/oauth2/redirect")
                    .queryParam("token", accessToken)
                    .queryParam("refreshToken", refreshToken)
                    .queryParam("userId", user.getId())
                    .queryParam("email", user.getEmail())
                    .queryParam("nom", user.getNom())
                    .queryParam("role", user.getRole())
                    .build()
                    .toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception e) {
            log.error("OAuth2 authentication failed: {}", e.getMessage());
            String targetUrl = UriComponentsBuilder
                    .fromUriString("http://localhost:4200/auth/error")
                    .queryParam("error", "oauth2_failed")
                    .queryParam("message", e.getMessage())
                    .build()
                    .toUriString();
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
    }
}
