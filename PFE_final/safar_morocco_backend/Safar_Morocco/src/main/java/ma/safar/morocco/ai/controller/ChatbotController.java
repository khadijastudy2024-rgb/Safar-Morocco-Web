package ma.safar.morocco.ai.controller;

import ma.safar.morocco.ai.dto.ChatRequest;
import ma.safar.morocco.ai.dto.ChatResponse;
import ma.safar.morocco.ai.entity.ChatMessage;
import ma.safar.morocco.ai.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("Question: '{}'", request.getMessage());

        try {
            ChatResponse response = chatbotService.processMessage(request);
            log.info("Réponse envoyée (IA: {})", response.getFromAI());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ChatResponse.builder()
                            .response("Désolé, une erreur s'est produite.")
                            .fromAI(false)
                            .build());
        }
    }

    @GetMapping("/conversation/{sessionId}/history")
    public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable String sessionId) {
        List<ChatMessage> history = chatbotService.getConversationHistory(sessionId);
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/conversation/{sessionId}")
    public ResponseEntity<Void> clearConversation(@PathVariable String sessionId) {
        chatbotService.clearConversation(sessionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Chatbot API (Stockage) opérationnel");
    }

    @PostMapping("/save")
    public ResponseEntity<Void> saveMessage(@Valid @RequestBody ma.safar.morocco.ai.dto.SaveMessageRequest request) {
        chatbotService.saveExternalMessage(request);
        return ResponseEntity.ok().build();
    }
}