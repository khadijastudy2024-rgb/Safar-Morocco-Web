package ma.safar.morocco.event.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import ma.safar.morocco.destination.entity.Destination;

import java.time.LocalDateTime;

@Entity
@Table(name = "evenements_culturels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvenementCulturel {

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

    private LocalDateTime dateDebut;

    private LocalDateTime dateFin;

    @Column(nullable = false, length = 200)
    private String lieuEn;

    @Column(length = 200)
    private String lieuFr;

    @Column(length = 200)
    private String lieuAr;

    @Column(length = 200)
    private String lieuEs;

    @Column(name = "event_type_en", length = 100)
    private String eventTypeEn;

    @Column(name = "event_type_fr", length = 100)
    private String eventTypeFr;

    @Column(name = "event_type_ar", length = 100)
    private String eventTypeAr;

    @Column(name = "event_type_es", length = 100)
    private String eventTypeEs;

    @Column(columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(columnDefinition = "TEXT")
    private String descriptionFr;

    @Column(columnDefinition = "TEXT")
    private String descriptionAr;

    @Column(columnDefinition = "TEXT")
    private String descriptionEs;

    @Column(columnDefinition = "TEXT")
    private String historiqueEn;

    @Column(columnDefinition = "TEXT")
    private String historiqueFr;

    @Column(columnDefinition = "TEXT")
    private String historiqueAr;

    @Column(columnDefinition = "TEXT")
    private String historiqueEs;

    private String imageUrl;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    private Destination destination;

    // Helper methods for backward compatibility
    public String getNom() { return nomEn; }
    public void setNom(String nom) { this.nomEn = nom; }
    public String getDescription() { return descriptionEn; }
    public void setDescription(String description) { this.descriptionEn = description; }
    public String getHistorique() { return historiqueEn; }
    public void setHistorique(String historique) { this.historiqueEn = historique; }
    public String getLieu() { return lieuEn; }
    public void setLieu(String lieu) { this.lieuEn = lieu; }
    public String getEventType() { return eventTypeEn; }
    public void setEventType(String eventType) { this.eventTypeEn = eventType; }
}
