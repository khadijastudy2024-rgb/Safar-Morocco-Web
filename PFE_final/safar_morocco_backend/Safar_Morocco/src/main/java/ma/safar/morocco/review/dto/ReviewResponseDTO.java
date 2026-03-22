package ma.safar.morocco.review.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDTO {
    private Long id;
    private String commentaire;
    private LocalDateTime datePublication;
    private Integer note;
    private String status;
    private String travelerName;
    private String travelerPhotoUrl;
    private String destinationName;
}
