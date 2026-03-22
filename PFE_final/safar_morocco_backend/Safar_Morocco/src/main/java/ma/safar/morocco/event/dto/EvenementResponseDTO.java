package ma.safar.morocco.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EvenementResponseDTO {
    private Long id;
    private String nom; // Field for current locale
    private String nomEn;
    private String nomFr;
    private String nomAr;
    private String nomEs;
    
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    
    private String lieu; // Field for current locale
    private String lieuEn;
    private String lieuFr;
    private String lieuAr;
    private String lieuEs;
    
    private String eventType; // Field for current locale
    private String eventTypeEn;
    private String eventTypeFr;
    private String eventTypeAr;
    private String eventTypeEs;
    
    private String description; // Field for current locale
    private String descriptionEn;
    private String descriptionFr;
    private String descriptionAr;
    private String descriptionEs;
    
    private String historique; // Field for current locale
    private String historiqueEn;
    private String historiqueFr;
    private String historiqueAr;
    private String historiqueEs;
    
    private String imageUrl;
    private Long destinationId;
}
