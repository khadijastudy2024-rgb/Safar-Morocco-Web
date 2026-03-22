package ma.safar.morocco.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {

    private String response;
    private String sessionId;
    private LocalDateTime timestamp;

    @Builder.Default
    private List<String> suggestions = new ArrayList<>();

    @Builder.Default
    private Boolean fromAI = true;
}