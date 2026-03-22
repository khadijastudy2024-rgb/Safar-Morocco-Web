package ma.safar.morocco.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.safar.morocco.auth.dto.*;
import ma.safar.morocco.auth.service.AuthService;
import ma.safar.morocco.user.entity.Utilisateur;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @GetMapping("/verify")
    public ResponseEntity<AuthResponse> verify(@RequestParam("token") String token) {
        try {
            AuthResponse response = authService.verifyEmailToken(token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @GetMapping("/me")
    public ResponseEntity<Utilisateur> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        authService.updateProfile(request);
        return ResponseEntity.ok("Profil mis à jour avec succès");
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok("Mot de passe modifié avec succès");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Un email de réinitialisation a été envoyé.");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/validate-reset-token")
    public ResponseEntity<Boolean> validateResetToken(@RequestParam("token") String token) {
        boolean isValid = authService.validateResetToken(token);
        return ResponseEntity.ok(isValid);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Mot de passe réinitialisé avec succès.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // Le logout est géré côté client en supprimant le token
        return ResponseEntity.ok("Déconnexion réussie");
    }
}