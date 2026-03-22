package ma.safar.morocco.reservation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.safar.morocco.reservation.enums.ReservationStatus;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OfferReservationDTO {
    private Long id;

    @NotNull(message = "L'ID de l'utilisateur est requis")
    private Long userId;

    @NotNull(message = "L'ID de l'itinéraire est requis")
    private Long itineraryId;

    @NotNull(message = "L'offre est requise")
    private ma.safar.morocco.offer.dto.OfferDTO offer;

    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate; // Optional depending on offer type, validated in service

    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private Integer quantity;
    private Double totalPrice;
    private ReservationStatus status;
}
