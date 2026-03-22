package ma.safar.morocco.destination.entity;

import jakarta.persistence.*;
import lombok.*;
import ma.safar.morocco.media.entity.Media;
import ma.safar.morocco.meteo.entity.Meteo;
import ma.safar.morocco.review.entity.Avis;
import ma.safar.morocco.event.entity.EvenementCulturel;
import ma.safar.morocco.offer.entity.Offer;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "destinations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Destination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String histoire;

    @Column(columnDefinition = "TEXT", name = "historical_description")
    private String historicalDescription;

    @Column(length = 100)
    private String type;

    private Double latitude;

    private Double longitude;

    @Column(length = 100)
    private String categorie;

    @OneToMany(mappedBy = "destination", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Media> medias = new ArrayList<>();

    @OneToMany(mappedBy = "destination", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Avis> avis = new ArrayList<>();

    @OneToMany(mappedBy = "destination", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EvenementCulturel> evenements = new ArrayList<>();

    @OneToOne(mappedBy = "destination", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Meteo meteo;

    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;

    @OneToMany(mappedBy = "destination", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private List<Offer> offers = new ArrayList<>();

    @Column(name = "best_time")
    private String bestTime;

    @Column(name = "languages")
    private String languages;

    @Column(name = "average_cost")
    private Double averageCost;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

}
