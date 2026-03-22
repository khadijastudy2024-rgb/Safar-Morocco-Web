package ma.safar.morocco.reservation.entity;

import jakarta.persistence.*;
import lombok.*;
import ma.safar.morocco.itinerary.entity.Itineraire;
import ma.safar.morocco.offer.entity.Offer;
import ma.safar.morocco.user.entity.Utilisateur;
import ma.safar.morocco.reservation.enums.ReservationStatus;

import java.time.LocalDate;

@Entity
@Table(name = "offer_reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Utilisateur user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itineraire_id", nullable = false)
    private Itineraire itinerary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    private LocalDate startDate;
    private LocalDate endDate;

    @Builder.Default
    private Integer quantity = 1;

    @Column
    private Double totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;
}
