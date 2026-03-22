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
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

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
    private Integer stars; // 1 to 5 stars
    @Column(length = 100)
    private String roomType;
    private Double pricePerNight;

    // RESTAURANT specific fields
    @Column(length = 100)
    private String cuisineType;
    private Double averagePrice;

    // ACTIVITY specific fields
    @Column(length = 50)
    private String duration; // e.g., "2 hours", "1 day"
    @Column(length = 100)
    private String activityType; // e.g., "surfing", "horse riding"

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;
}
