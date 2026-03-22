package ma.safar.morocco.destination.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DestinationResponseDTO {
    private Long id;
    private String nom;
    private String description;
    private String histoire;
    private String historicalDescription;
    private String type;
    private Double latitude;
    private Double longitude;
    private String categorie;
    private Long viewCount;
    private Double averageRating;
    private Long reviewCount;
    private String thumbnailUrl;
    private List<ma.safar.morocco.media.dto.MediaDTO> medias;
    private List<String> imageUrls;
    private String bestTime;
    private String languages;
    private Double averageCost;
    private String videoUrl;
}
