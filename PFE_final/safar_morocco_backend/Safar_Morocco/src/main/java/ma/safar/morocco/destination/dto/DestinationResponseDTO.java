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
    private String nomEn;
    private String nomFr;
    private String nomAr;
    private String nomEs;
    
    private String description;
    private String descriptionEn;
    private String descriptionFr;
    private String descriptionAr;
    private String descriptionEs;
    
    private String histoire;
    private String histoireEn;
    private String histoireFr;
    private String histoireAr;
    private String histoireEs;
    
    private String historicalDescription;
    private String historicalDescriptionEn;
    private String historicalDescriptionFr;
    private String historicalDescriptionAr;
    private String historicalDescriptionEs;
    
    private String type;
    private String typeEn;
    private String typeFr;
    private String typeAr;
    private String typeEs;
    
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
    private String bestTimeEn;
    private String bestTimeFr;
    private String bestTimeAr;
    private String bestTimeEs;
    
    private String languages;
    private String languagesEn;
    private String languagesFr;
    private String languagesAr;
    private String languagesEs;
    
    private Double averageCost;
    private String videoUrl;
}
