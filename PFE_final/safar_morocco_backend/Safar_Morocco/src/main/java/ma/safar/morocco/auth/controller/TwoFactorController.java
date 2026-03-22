package ma.safar.morocco.auth.controller;

import lombok.RequiredArgsConstructor;
import ma.safar.morocco.auth.dto.AuthResponse;
import ma.safar.morocco.auth.dto.TwoFactorRequest;
import ma.safar.morocco.auth.service.AuthService;
import ma.safar.morocco.security.JwtService;
import ma.safar.morocco.user.entity.Utilisateur;
import ma.safar.morocco.user.service.TwoFactorAuthService;
import ma.safar.morocco.user.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/2fa")
@RequiredArgsConstructor
public class TwoFactorController {

    private final AuthService authService;
    private final TwoFactorAuthService twoFactorAuthService;
    private final UtilisateurService utilisateurService;
    private final JwtService jwtService;

    @PostMapping("/setup")
    public ResponseEntity<Map<String, String>> setup2FA() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Authentication is required");
        }
        Utilisateur user = authService.getCurrentUser();
        try {
            var twoFA = twoFactorAuthService.generateSecretAndQrCode(user);
            
            Map<String, String> response = new HashMap<>();
            response.put("secret", twoFA.getSecretKey());
            response.put("qrCodeUri", twoFA.getQrCodeUrl());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new IllegalStateException("Erreur de configuration 2FA", e);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verify2FA(@Valid @RequestBody TwoFactorRequest request) {
        Utilisateur user = authService.getCurrentUser();
        twoFactorAuthService.enableTwoFactorAuth(user, request.getCode());

        Map<String, String> response = new HashMap<>();
        response.put("message", "2FA activé avec succès");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/disable")
    public ResponseEntity<Map<String, String>> disable2FA() {
        Utilisateur user = authService.getCurrentUser();
        twoFactorAuthService.disableTwoFactorAuth(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "2FA désactivé avec succès");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate-login")
    public ResponseEntity<AuthResponse> validateLogin(@Valid @RequestBody TwoFactorRequest request) {
        Utilisateur user = utilisateurService.getUserByEmailEntity(request.getEmail());

        if (!twoFactorAuthService.verifyCode(user, request.getCode())) {
            throw new IllegalArgumentException("Code 2FA invalide");
        }

        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .nom(user.getNom())
                .role(user.getRole())
                .build());
    }
}
