package ma.safar.morocco.ai.service;

import ma.safar.morocco.ai.dto.ChatRequest;
import ma.safar.morocco.ai.dto.ChatResponse;
import ma.safar.morocco.ai.entity.ChatMessage;
import ma.safar.morocco.ai.entity.Conversation;
import ma.safar.morocco.ai.repository.ChatMessageRepository;
import ma.safar.morocco.ai.repository.ConversationRepository;
import ma.safar.morocco.ai.repository.UserMemoryRepository;
import ma.safar.morocco.ai.entity.UserMemory;
import ma.safar.morocco.user.repository.UtilisateurRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatbotService {

    private final RecommendationService recommendationService;

    private final ConversationRepository conversationRepository;

    private final ChatMessageRepository chatMessageRepository;

    private final UserMemoryRepository userMemoryRepository;

    private final UtilisateurRepository utilisateurRepository;

    @Value("${ai.conversation.max.history:10}")
    private int maxHistory;

    @Value("${ai.fallback.enabled:true}")
    private boolean fallbackEnabled;

    @Transactional
    public ChatResponse processMessage(ChatRequest request) {
        log.info("NOUVEAU MESSAGE");
        log.info("Message: '{}'", request.getMessage());
        log.info("Langue demandée: {}", request.getLangue());

        Conversation conversation = getOrCreateConversation(request);
        saveUserMessage(conversation, request.getMessage());

        // L'IA est gérée par Python, le backend Spring renvoie juste le fallback
        // et continue d'enregistrer la conversation dans la base de données.
        String aiResponse = getEmergencyResponse(request.getLangue());
        boolean fromAI = false;

        log.info("Généré par IA: {}", fromAI);

        // Détection de faits importants à mémoriser
        detectAndSaveUserFacts(conversation, request.getMessage());

        saveAssistantMessage(conversation, aiResponse);
        List<String> suggestions = recommendationService.generateSuggestions(request.getMessage());

        return ChatResponse.builder()
                .response(aiResponse)
                .sessionId(conversation.getSessionId())
                .timestamp(LocalDateTime.now())
                .suggestions(suggestions)
                .fromAI(fromAI)
                .build();
    }



    private void detectAndSaveUserFacts(Conversation conversation, String message) {
        if (conversation.getUser() == null) return;

        String lowerMsg = message.toLowerCase();
        
        // Détection du nom
        // English: My name is X
        // French: Mon nom est X, Je m'appelle X
        String detectedName = null;
        if (lowerMsg.contains("my name is ")) {
            detectedName = message.substring(lowerMsg.indexOf("my name is ") + 11).trim();
        } else if (lowerMsg.contains("mon nom est ")) {
            detectedName = message.substring(lowerMsg.indexOf("mon nom est ") + 12).trim();
        } else if (lowerMsg.contains("je m'appelle ")) {
            detectedName = message.substring(lowerMsg.indexOf("je m'appelle ") + 13).trim();
        }

        if (detectedName != null && !detectedName.isEmpty()) {
            // Nettoyer le nom des ponctuations finales si présentes
            if (detectedName.endsWith(".") || detectedName.endsWith("!")) {
                detectedName = detectedName.substring(0, detectedName.length() - 1);
            }
            
            final String finalName = detectedName;
            log.info("Fait détecté : Nom = {}", finalName);
            
            List<UserMemory> memories = userMemoryRepository.findByUserId(conversation.getUser().getId());
            Optional<UserMemory> existingName = memories.stream()
                    .filter(m -> m.getMemoryKey().equalsIgnoreCase("name"))
                    .findFirst();
            
            if (existingName.isPresent()) {
                UserMemory memory = existingName.get();
                memory.setMemoryValue(finalName);
                userMemoryRepository.save(memory);
            } else {
                UserMemory memory = UserMemory.builder()
                        .user(conversation.getUser())
                        .memoryKey("name")
                        .memoryValue(finalName)
                        .build();
                userMemoryRepository.save(memory);
            }
        }
    }



    // Réponses d'urgence multilingues

    private String getEmergencyResponse(String langue) {
        return switch (langue) {
            case "ar" -> "مرحباً! أنا مساعدك السياحي للمغرب. كيف يمكنني مساعدتك؟";
            case "en" -> "Hello! I'm your Moroccan tourism assistant. How can I help you?";
            case "es" -> "¡Hola! Soy tu asistente turístico de Marruecos. ¿Cómo puedo ayudarte?";
            default -> "Bonjour! Je suis votre assistant touristique pour le Maroc. Comment puis-je vous aider?";
        };
    }

    private Conversation getOrCreateConversation(ChatRequest request) {
        if (request.getSessionId() != null) {
            Optional<Conversation> existing = conversationRepository.findBySessionId(request.getSessionId());
            if (existing.isPresent())
                return existing.get();
        }
        Conversation conversation = new Conversation();
        conversation.setLangue(request.getLangue());
        if (request.getUserId() != null) {
            utilisateurRepository.findById(request.getUserId()).ifPresent(conversation::setUser);
        }
        return conversationRepository.save(conversation);
    }

    private void saveUserMessage(Conversation conversation, String content) {
        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        if (conversation.getUser() != null) {
            message.setUserId(conversation.getUser().getId());
        }
        message.setRole("user");
        message.setContenu(content);
        chatMessageRepository.save(message);
    }

    private void saveAssistantMessage(Conversation conversation, String content) {
        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        if (conversation.getUser() != null) {
            message.setUserId(conversation.getUser().getId());
        }
        message.setRole("assistant");
        message.setContenu(content);
        chatMessageRepository.save(message);
    }

    public List<ChatMessage> getConversationHistory(String sessionId) {
        Optional<Conversation> conversation = conversationRepository.findBySessionId(sessionId);
        return conversation.map(conv -> chatMessageRepository.findByConversationIdOrderByTimestampAsc(conv.getId()))
                .orElse(List.of());
    }

    @Transactional
    public void clearConversation(String sessionId) {
        conversationRepository.findBySessionId(sessionId).ifPresent(conv -> {
            chatMessageRepository.deleteByConversationId(conv.getId());
            conversationRepository.delete(conv);
        });
    }

    @Transactional
    public void saveExternalMessage(ma.safar.morocco.ai.dto.SaveMessageRequest request) {
        Optional<Conversation> existing = conversationRepository.findBySessionId(request.getSessionId());
        Conversation conversation;
        if (existing.isPresent()) {
            conversation = existing.get();
        } else {
            conversation = new Conversation();
            conversation.setSessionId(request.getSessionId());
            conversation.setLangue(request.getLangue() != null ? request.getLangue() : "fr");
            if (request.getUserId() != null) {
                utilisateurRepository.findById(request.getUserId()).ifPresent(conversation::setUser);
            }
            conversation = conversationRepository.save(conversation);
        }

        ChatMessage message = new ChatMessage();
        message.setConversation(conversation);
        message.setRole(request.getRole()); // "user" or "assistant"
        message.setContenu(request.getMessage());
        chatMessageRepository.save(message);
    }
}