package ma.safar.morocco.media.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MediaDTO {
    private Long id;
    private String url;
    private String type;
}
