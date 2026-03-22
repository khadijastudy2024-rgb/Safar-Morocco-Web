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
    private String name;
    private String description;
    private Double price;
    private OfferType type;
    private Long destinationId;
    private Boolean available;
    private Integer stars;
    private String roomType;
    private Double pricePerNight;
    private String cuisineType;
    private Double averagePrice;
    private Double displayPrice;
    private String duration;
    private String activityType;
    private Boolean deleted;
}
