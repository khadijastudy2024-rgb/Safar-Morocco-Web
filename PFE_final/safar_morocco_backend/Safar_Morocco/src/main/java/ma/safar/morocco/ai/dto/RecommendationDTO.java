package ma.safar.morocco.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDTO {
    private Long id;
    private String name;
    private String description;
    private String reason;
    private String imageUrl;
}
