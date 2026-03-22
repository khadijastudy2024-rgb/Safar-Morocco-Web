package ma.safar.morocco.ai.controller;

import ma.safar.morocco.ai.dto.RecommendationResponse;
import ma.safar.morocco.ai.service.RecommendationService;
import ma.safar.morocco.user.entity.Utilisateur;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class PersonalizedRecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<RecommendationResponse> getRecommendations(@AuthenticationPrincipal Utilisateur user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        
        RecommendationResponse response = new RecommendationResponse();
        response.setRecommendations(recommendationService.getPersonalizedRecommendations(user));
        
        return ResponseEntity.ok(response);
    }
}
