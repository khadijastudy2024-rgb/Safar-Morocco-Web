package ma.safar.morocco.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaveMessageRequest {
    @NotBlank
    private String sessionId;

    private Long userId; // optional

    @NotBlank
    private String role; // "user" or "assistant"

    @NotBlank
    private String message;

    private String langue; // optional
}
