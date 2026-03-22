package ma.safar.morocco.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @NotBlank(message = "Le message ne peut pas être vide")
    @Size(min = 1, max = 1000, message = "Message entre 1 et 1000 caractères")
    private String message;

    private String sessionId;

    @Pattern(regexp = "fr|ar|en|es", message = "Langue non supportée: fr, ar, en, es")
    private String langue = "fr";

    private Long userId;
}