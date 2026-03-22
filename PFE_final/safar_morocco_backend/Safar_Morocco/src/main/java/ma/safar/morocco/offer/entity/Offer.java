package ma.safar.morocco.offer.entity;

import jakarta.persistence.*;
import lombok.*;
import ma.safar.morocco.destination.entity.Destination;
import ma.safar.morocco.offer.enums.OfferType;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nameEn;

    @Column(length = 200)
    private String nameFr;

    @Column(length = 200)
    private String nameAr;

    @Column(length = 200)
    private String nameEs;

    @Column(columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(columnDefinition = "TEXT")
    private String descriptionFr;

    @Column(columnDefinition = "TEXT")
    private String descriptionAr;

    @Column(columnDefinition = "TEXT")
    private String descriptionEs;

    private Double price; // Base price for activity or general purpose

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OfferType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id", nullable = false)
    @JsonIgnore
    private Destination destination;

    @Builder.Default
    private boolean available = true;

    // HOTEL specific fields
    // HOTEL specific fields
    private Integer stars; // 1 to 5 stars
    @Column(length = 100)
    private String roomTypeEn;
    @Column(length = 100)
    private String roomTypeFr;
    @Column(length = 100)
    private String roomTypeAr;
    @Column(length = 100)
    private String roomTypeEs;
    
    private Double pricePerNight;

    // RESTAURANT specific fields
    @Column(length = 100)
    private String cuisineTypeEn;
    @Column(length = 100)
    private String cuisineTypeFr;
    @Column(length = 100)
    private String cuisineTypeAr;
    @Column(length = 100)
    private String cuisineTypeEs;
    
    private Double averagePrice;

    // ACTIVITY specific fields
    @Column(length = 50)
    private String durationEn;
    @Column(length = 50)
    private String durationFr;
    @Column(length = 50)
    private String durationAr;
    @Column(length = 50)
    private String durationEs;

    @Column(length = 100)
    private String activityTypeEn;
    @Column(length = 100)
    private String activityTypeFr;
    @Column(length = 100)
    private String activityTypeAr;
    @Column(length = 100)
    private String activityTypeEs;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    // Helper methods for backward compatibility
    public String getName() { return nameEn; }
    public void setName(String name) { this.nameEn = name; }
    public String getDescription() { return descriptionEn; }
    public void setDescription(String description) { this.descriptionEn = description; }
    public String getRoomType() { return roomTypeEn; }
    public void setRoomType(String roomType) { this.roomTypeEn = roomType; }
    public String getCuisineType() { return cuisineTypeEn; }
    public void setCuisineType(String cuisineType) { this.cuisineTypeEn = cuisineType; }
    public String getDuration() { return durationEn; }
    public void setDuration(String duration) { this.durationEn = duration; }
    public String getActivityType() { return activityTypeEn; }
    public void setActivityType(String activityType) { this.activityTypeEn = activityType; }
}
