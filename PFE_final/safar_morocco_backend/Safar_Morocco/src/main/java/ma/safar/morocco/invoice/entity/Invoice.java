package ma.safar.morocco.invoice.entity;

import jakarta.persistence.*;
import lombok.*;
import ma.safar.morocco.itinerary.entity.Itineraire;
import ma.safar.morocco.user.entity.Utilisateur;
import ma.safar.morocco.invoice.enums.InvoiceStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Utilisateur user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itineraire_id", nullable = false)
    private Itineraire itinerary;

    @Column(nullable = false)
    private Double totalAmount;

    @Column(nullable = false)
    private LocalDateTime generatedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.UNPAID;

    @Column(length = 500)
    private String pdfPath;
}
