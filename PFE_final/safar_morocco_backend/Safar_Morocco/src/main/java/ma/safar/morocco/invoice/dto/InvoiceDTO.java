package ma.safar.morocco.invoice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.safar.morocco.invoice.enums.InvoiceStatus;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDTO {
    private Long id;
    private Long userId;
    private Long itineraryId;
    private Double totalAmount;
    private LocalDateTime generatedDate;
    private InvoiceStatus status;
    private String pdfPath;
}
