package ma.safar.morocco.offer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.safar.morocco.offer.enums.OfferType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OfferDTO {
    private Long id;
    private String name; // Current locale
    private String nameEn;
    private String nameFr;
    private String nameAr;
    private String nameEs;
    
    private String description; // Current locale
    private String descriptionEn;
    private String descriptionFr;
    private String descriptionAr;
    private String descriptionEs;
    
    private Double price;
    private OfferType type;
    private Long destinationId;
    private Boolean available;
    private Integer stars;
    
    private String roomType; // Current locale
    private String roomTypeEn;
    private String roomTypeFr;
    private String roomTypeAr;
    private String roomTypeEs;
    
    private Double pricePerNight;
    
    private String cuisineType; // Current locale
    private String cuisineTypeEn;
    private String cuisineTypeFr;
    private String cuisineTypeAr;
    private String cuisineTypeEs;
    
    private Double averagePrice;
    private Double displayPrice;
    
    private String duration; // Current locale
    private String durationEn;
    private String durationFr;
    private String durationAr;
    private String durationEs;
    
    private String activityType; // Current locale
    private String activityTypeEn;
    private String activityTypeFr;
    private String activityTypeAr;
    private String activityTypeEs;
    
    private Boolean deleted;
}
