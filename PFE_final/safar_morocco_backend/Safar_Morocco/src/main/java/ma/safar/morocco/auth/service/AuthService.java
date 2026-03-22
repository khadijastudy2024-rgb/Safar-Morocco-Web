package ma.safar.morocco.auth.service;

import lombok.RequiredArgsConstructor;
import ma.safar.morocco.auth.dto.*;
import ma.safar.morocco.security.JwtService;
import ma.safar.morocco.security.service.AuditService;
import ma.safar.morocco.user.entity.Utilisateur;
import ma.safar.morocco.user.repository.UtilisateurRepository;
import ma.safar.morocco.user.service.ActivityLogService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ma.safar.morocco.user.entity.VerificationToken;
import ma.safar.morocco.user.repository.VerificationTokenRepository;
import ma.safar.morocco.user.entity.PasswordResetToken;
import ma.safar.morocco.user.repository.PasswordResetTokenRepository;
import ma.safar.morocco.user.service.TwoFactorAuthService;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String USER_NOT_FOUND_MSG = "Utilisateur non trouvé";
    private static final String BEARER_PREFIX = "Bearer";

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ActivityLogService activityLogService;
    private final AuditService auditService;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TwoFactorAuthService twoFactorAuthService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un compte existe déjà avec cet email");
        }

        var user = Utilisateur.builder()
                .nom(request.getNom())
                .email(request.getEmail())
                .motDePasseHache(passwordEncoder.encode(request.getMotDePasse()))
                .telephone(request.getTelephone())
                .langue(request.getLangue() != null ? request.getLangue() : "fr")
                .role("USER")
                .photoUrl("/uploads/users/default-avatar.png")
                .actif(false)
                .compteBloquer(false)
                .provider("LOCAL")
                .build();
        user = utilisateurRepository.save(user);

        // Generate Verification Token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .utilisateur(user)
                .expirationTime(LocalDateTime.now().plusHours(24))
                .build();
        verificationTokenRepository.save(verificationToken);

        // Send Email
        emailService.sendVerificationEmail(user.getEmail(), user.getNom(), token);

        auditService.logAction(user.getId(), "USER_REGISTERED", "Utilisateur", user.getId(),
                "New user registered: " + user.getEmail());
        activityLogService.logActivity(user, "ACCOUNT_CREATED",
                "Compte créé via enregistrement. En attente de vérification.");

        // Return empty tokens as verification is required
        return AuthResponse.builder()
                .email(user.getEmail())
                .nom(user.getNom())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public AuthResponse verifyEmailToken(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide"));

        if (verificationToken.isExpired()) {
            throw new IllegalStateException("Token expiré");
        }

        Utilisateur user = verificationToken.getUtilisateur();
        user.setActif(true);
        utilisateurRepository.save(user);

        verificationTokenRepository.delete(verificationToken);

        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(BEARER_PREFIX)
                .userId(user.getId())
                .email(user.getEmail())
                .nom(user.getNom())
                .role(user.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getMotDePasse()));

        var user = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_MSG));

        auditService.logAction(user.getId(), "USER_LOGIN", "Utilisateur", user.getId(),
                "User logged in: " + user.getEmail());

        if (Boolean.TRUE.equals(user.getCompteBloquer())) {
            throw new IllegalStateException("Votre compte a été bloqué. Contactez l'administrateur.");
        }

        if (!Boolean.TRUE.equals(user.getActif())) {
            throw new IllegalStateException("Votre compte est désactivé.");
        }

        if (twoFactorAuthService.isTwoFactorEnabled(user)) {
            return AuthResponse.builder()
                    .requiresTwoFactor(true)
                    .build();
        }

        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(BEARER_PREFIX)
                .userId(user.getId())
                .email(user.getEmail())
                .nom(user.getNom())
                .role(user.getRole())
                .build();
    }

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        final String refreshToken = request.getRefreshToken();
        final String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            var user = utilisateurRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_MSG));

            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                var newRefreshToken = jwtService.generateRefreshToken(user);

                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(newRefreshToken)
                        .tokenType(BEARER_PREFIX)
                        .userId(user.getId())
                        .email(user.getEmail())
                        .nom(user.getNom())
                        .role(user.getRole())
                        .build();
            }
        }
        throw new IllegalArgumentException("Refresh token invalide");
    }

    @Transactional
    public void updateProfile(UpdateProfileRequest request) {
        Utilisateur user = getCurrentUser();

        if (request.getNom() != null) {
            user.setNom(request.getNom());
        }
        if (request.getTelephone() != null) {
            user.setTelephone(request.getTelephone());
        }
        if (request.getLangue() != null) {
            user.setLangue(request.getLangue());
        }

        utilisateurRepository.save(user);
        activityLogService.logActivity(user, "PROFILE_UPDATED", "Profil mis à jour via AuthService");
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Utilisateur user = getCurrentUser();

        if (!passwordEncoder.matches(request.getAncienMotDePasse(), user.getMotDePasseHache())) {
            throw new IllegalArgumentException("Ancien mot de passe incorrect");
        }

        user.setMotDePasseHache(passwordEncoder.encode(request.getNouveauMotDePasse()));
        utilisateurRepository.save(user);
    }

    public Utilisateur getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Utilisateur non authentifié");
        }
        String email = authentication.getName();
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(USER_NOT_FOUND_MSG));
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        Utilisateur user = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec cet email"));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .utilisateur(user)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build();

        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), token);

        auditService.logAction(user.getId(), "PASSWORD_RESET_REQUESTED", "Utilisateur", user.getId(),
                "Password reset requested for: " + user.getEmail());
        activityLogService.logActivity(user, "PASSWORD_RESET_REQUEST",
                "Demande de réinitialisation de mot de passe envoyée.");
    }

    public boolean validateResetToken(String token) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElse(null);

        return resetToken != null && !resetToken.isExpired() && !resetToken.isUsed();
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Token invalide ou inexistant"));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new IllegalStateException("Token de réinitialisation expiré");
        }

        Utilisateur user = resetToken.getUtilisateur();
        user.setMotDePasseHache(passwordEncoder.encode(request.getNewPassword()));
        utilisateurRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        auditService.logAction(user.getId(), "PASSWORD_RESET_COMPLETED", "Utilisateur", user.getId(),
                "Password reset completed for: " + user.getEmail());
        activityLogService.logActivity(user, "PASSWORD_RESET", "Mot de passe réinitialisé avec succès.");
    }
}