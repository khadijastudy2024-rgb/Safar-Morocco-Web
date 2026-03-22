package ma.safar.morocco.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TwoFactorRequest {
    @NotBlank(message = "Le code est obligatoire")
    private String code;

    private String secret; // Used during setup only

    private String email; // Used for login verification
}
