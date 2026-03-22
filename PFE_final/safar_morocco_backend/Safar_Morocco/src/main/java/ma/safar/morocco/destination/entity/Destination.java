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
    private String nomEn;

    @Column(length = 200)
    private String nomFr;

    @Column(length = 200)
    private String nomAr;

    @Column(length = 200)
    private String nomEs;

    @Column(columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(columnDefinition = "TEXT")
    private String descriptionFr;

    @Column(columnDefinition = "TEXT")
    private String descriptionAr;

    @Column(columnDefinition = "TEXT")
    private String descriptionEs;

    @Column(columnDefinition = "TEXT")
    private String histoireEn;

    @Column(columnDefinition = "TEXT")
    private String histoireFr;

    @Column(columnDefinition = "TEXT")
    private String histoireAr;

    @Column(columnDefinition = "TEXT")
    private String histoireEs;

    @Column(columnDefinition = "TEXT", name = "historical_description_en")
    private String historicalDescriptionEn;

    @Column(columnDefinition = "TEXT", name = "historical_description_fr")
    private String historicalDescriptionFr;

    @Column(columnDefinition = "TEXT", name = "historical_description_ar")
    private String historicalDescriptionAr;

    @Column(columnDefinition = "TEXT", name = "historical_description_es")
    private String historicalDescriptionEs;

    @Column(length = 100)
    private String typeEn;

    @Column(length = 100)
    private String typeFr;

    @Column(length = 100)
    private String typeAr;

    @Column(length = 100)
    private String typeEs;

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

    @Column(name = "best_time_en")
    private String bestTimeEn;

    @Column(name = "best_time_fr")
    private String bestTimeFr;

    @Column(name = "best_time_ar")
    private String bestTimeAr;

    @Column(name = "best_time_es")
    private String bestTimeEs;

    @Column(name = "languages_en")
    private String languagesEn;

    @Column(name = "languages_fr")
    private String languagesFr;

    @Column(name = "languages_ar")
    private String languagesAr;

    @Column(name = "languages_es")
    private String languagesEs;

    @Column(name = "average_cost")
    private Double averageCost;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    // Helper methods for backward compatibility or easy access
    public String getNom() { return nomEn; } // Fallback or default
    public void setNom(String nom) { this.nomEn = nom; }
    public String getDescription() { return descriptionEn; }
    public void setDescription(String description) { this.descriptionEn = description; }
    public String getHistoire() { return histoireEn; }
    public void setHistoire(String histoire) { this.histoireEn = histoire; }
    public String getHistoricalDescription() { return historicalDescriptionEn; }
    public void setHistoricalDescription(String historicalDescription) { this.historicalDescriptionEn = historicalDescription; }
    public String getBestTime() { return bestTimeEn; }
    public void setBestTime(String bestTime) { this.bestTimeEn = bestTime; }
    public String getLanguages() { return languagesEn; }
    public void setLanguages(String languages) { this.languagesEn = languages; }
    public String getType() { return typeEn; }
    public void setType(String type) { this.typeEn = type; }
}
