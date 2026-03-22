package ma.safar.morocco.invoice.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import ma.safar.morocco.invoice.dto.InvoiceDTO;
import ma.safar.morocco.invoice.entity.Invoice;
import ma.safar.morocco.invoice.enums.InvoiceStatus;
import ma.safar.morocco.invoice.repository.InvoiceRepository;
import ma.safar.morocco.itinerary.entity.Itineraire;
import ma.safar.morocco.itinerary.repository.ItineraireRepository;
import ma.safar.morocco.reservation.entity.OfferReservation;
import ma.safar.morocco.reservation.repository.OfferReservationRepository;
import ma.safar.morocco.user.entity.Utilisateur;
import ma.safar.morocco.user.repository.UtilisateurRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;


import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private static final String KEY_INVOICE = "INVOICE";
    private static final String KEY_INVOICE_NO = "Invoice No";
    private static final String KEY_DATE = "Date";
    private static final String KEY_BILLED_TO = "Billed To";
    private static final String KEY_ITINERARY = "Itinerary";
    private static final String KEY_NAME = "Name";
    private static final String KEY_TYPE = "Type";
    private static final String KEY_DATES = "Dates";
    private static final String KEY_QTY = "Qty";
    private static final String KEY_UNIT_PRICE = "Unit Price";
    private static final String KEY_TOTAL = "Total";
    private static final String KEY_GRAND_TOTAL = "GRAND TOTAL";
    private static final String KEY_THANK_YOU = "Thank you";

    private final InvoiceRepository invoiceRepository;
    private final ItineraireRepository itineraryRepository;
    private final UtilisateurRepository userRepository;
    private final OfferReservationRepository offerReservationRepository;


    @Override
    @Transactional
    public InvoiceDTO generateInvoice(Long itineraryId, Long userId, String lang) {
        Itineraire itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new IllegalArgumentException("Itinerary not found"));
        Utilisateur user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<OfferReservation> approvedReservations = offerReservationRepository.findByItineraryId(itineraryId).stream()
                .filter(res -> res.getStatus() == ma.safar.morocco.reservation.enums.ReservationStatus.APPROVED)
                .toList();

        if (approvedReservations.isEmpty()) {
            throw new IllegalStateException("No approved reservations found for this itinerary. Cannot generate invoice.");
        }

        Double grandTotal = 0.0;
        for (OfferReservation res : approvedReservations) {
            if (res.getTotalPrice() != null) {
                grandTotal += res.getTotalPrice();
            }
        }

        Invoice invoice = Invoice.builder()
                .itinerary(itinerary)
                .user(user)
                .totalAmount(grandTotal)
                .generatedDate(LocalDateTime.now())
                .status(InvoiceStatus.UNPAID)
                .build();

        Invoice saved = invoiceRepository.save(invoice);

        // Generate PDF
        String pdfPath = generatePdfSafely(saved, approvedReservations, lang);
        saved.setPdfPath(pdfPath);

        saved = invoiceRepository.save(saved);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public InvoiceDTO generateInvoiceForReservation(Long reservationId, String lang) {
        OfferReservation reservation = offerReservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        
        Itineraire itinerary = reservation.getItinerary();
        Utilisateur user = reservation.getUser();

        if (reservation.getStatus() != ma.safar.morocco.reservation.enums.ReservationStatus.APPROVED && 
            reservation.getStatus() != ma.safar.morocco.reservation.enums.ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Reservation must be APPROVED or CONFIRMED to generate invoice.");
        }

        Double grandTotal = reservation.getTotalPrice() != null ? reservation.getTotalPrice() : 0.0;

        Invoice invoice = Invoice.builder()
                .itinerary(itinerary)
                .user(user)
                .totalAmount(grandTotal)
                .generatedDate(LocalDateTime.now())
                .status(InvoiceStatus.UNPAID)
                .build();

        Invoice saved = invoiceRepository.save(invoice);

        // Generate PDF
        String pdfPath = generatePdfSafely(saved, List.of(reservation), lang);
        saved.setPdfPath(pdfPath);

        saved = invoiceRepository.save(saved);
        return mapToDTO(saved);
    }

    private String generatePdfSafely(Invoice invoice, List<OfferReservation> reservations, String lang) {
        try {
            String dirPath = "uploads/invoices/";
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = "Invoice_" + invoice.getId() + ".pdf";
            String fullPath = dirPath + fileName;

            PdfWriter writer = new PdfWriter(fullPath);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4);
            try {
                document.setMargins(40, 40, 40, 40);

                PdfFont fontHelvetica = PdfFontFactory.createFont(StandardFonts.HELVETICA);
                PdfFont fontHelveticaBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

                DeviceRgb brandColor = new DeviceRgb(249, 115, 22);
                DeviceRgb borderColor = new DeviceRgb(220, 220, 220);
                DeviceRgb footerColor = new DeviceRgb(128, 128, 128);

                buildHeaderAndLine(document, fontHelvetica, fontHelveticaBold, brandColor, footerColor);
                buildInfoTable(document, invoice, lang, fontHelvetica, fontHelveticaBold, brandColor);
                buildItemsTable(document, reservations, lang, fontHelvetica, fontHelveticaBold, brandColor, borderColor);
                buildFooterAndTotal(document, invoice, lang, fontHelvetica, fontHelveticaBold, footerColor);

                return fullPath;
            } finally {
                document.close();
            }
        } catch (Exception e) {
            log.error("Failed to generate PDF: {}", e.getMessage(), e);
            return null;
        }
    }

    private void buildHeaderAndLine(Document document, PdfFont fontHelperText, PdfFont fontBold, DeviceRgb brandColor, DeviceRgb footerColor) {
        float[] headerWidths = { 1, 1 };
        Table headerTable = new Table(UnitValue.createPercentArray(headerWidths)).useAllAvailableWidth();

        Cell logoCell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT);
        addLogoToCell(logoCell, fontBold, brandColor);
        headerTable.addCell(logoCell);

        Cell titleCell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        titleCell.add(new Paragraph("SAFAR MOROCCO").setFont(fontBold).setFontSize(18));
        titleCell.add(new Paragraph("Travel & Experiences").setFont(fontHelperText).setFontSize(11).setFontColor(footerColor));
        headerTable.addCell(titleCell);

        document.add(headerTable);
        document.add(new Paragraph("\n"));

        SolidLine line = new SolidLine(1f);
        line.setColor(brandColor);
        document.add(new LineSeparator(line));
        document.add(new Paragraph("\n"));
    }

    private void buildInfoTable(Document document, Invoice invoice, String lang, PdfFont fontHelper, PdfFont fontBold, DeviceRgb brandColor) {
        Itineraire itinerary = invoice.getItinerary();
        Utilisateur user = invoice.getUser();
        
        float[] infoWidths = { 1, 1 };
        Table infoTable = new Table(UnitValue.createPercentArray(infoWidths)).useAllAvailableWidth();

        Cell leftInfoCell = new Cell().setBorder(Border.NO_BORDER);
        leftInfoCell.add(new Paragraph(getTranslatedText(KEY_INVOICE, lang)).setFont(fontBold).setFontSize(22).setFontColor(brandColor));
        leftInfoCell.add(new Paragraph(getTranslatedText(KEY_INVOICE_NO, lang) + " INV-" + invoice.getId()).setFont(fontHelper).setFontSize(11));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        leftInfoCell.add(new Paragraph(getTranslatedText(KEY_DATE, lang) + " " + invoice.getGeneratedDate().format(formatter)).setFont(fontHelper).setFontSize(11));
        infoTable.addCell(leftInfoCell);

        Cell rightInfoCell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        rightInfoCell.add(new Paragraph(getTranslatedText(KEY_BILLED_TO, lang)).setFont(fontBold).setFontSize(11));
        rightInfoCell.add(new Paragraph(user.getNom()).setFont(fontHelper).setFontSize(11));
        rightInfoCell.add(new Paragraph(user.getEmail()).setFont(fontHelper).setFontSize(11));
        rightInfoCell.add(new Paragraph(getTranslatedText(KEY_ITINERARY, lang) + " " + itinerary.getNom()).setFont(fontHelper).setFontSize(11));
        infoTable.addCell(rightInfoCell);

        document.add(infoTable);
        document.add(new Paragraph("\n\n"));
    }

    private void buildItemsTable(Document document, List<OfferReservation> reservations, String lang, PdfFont fontHelper, PdfFont fontBold, DeviceRgb brandColor, DeviceRgb borderColor) {
        float[] columnWidths = { 3, 2, 3, 1, 2, 2 };
        Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();

        String[] headers = { getTranslatedText(KEY_NAME, lang), getTranslatedText(KEY_TYPE, lang), getTranslatedText(KEY_DATES, lang), getTranslatedText(KEY_QTY, lang), getTranslatedText(KEY_UNIT_PRICE, lang), getTranslatedText(KEY_TOTAL, lang) };
        for (String header : headers) {
            Cell cell = new Cell().add(new Paragraph(header).setFont(fontBold).setFontColor(ColorConstants.WHITE));
            cell.setBackgroundColor(brandColor);
            cell.setPadding(6);
            cell.setBorder(new SolidBorder(borderColor, 1));
            if (header.equals(getTranslatedText(KEY_QTY, lang)) || header.equals(getTranslatedText(KEY_UNIT_PRICE, lang)) || header.equals(getTranslatedText(KEY_TOTAL, lang))) {
                cell.setTextAlignment(TextAlignment.RIGHT);
            } else {
                cell.setTextAlignment(TextAlignment.LEFT);
            }
            table.addHeaderCell(cell);
        }

        for (OfferReservation res : reservations) {
            ma.safar.morocco.offer.entity.Offer offer = res.getOffer();
            String nameWithDetails = buildOfferNameDetails(offer);
            
            table.addCell(createCell(nameWithDetails, fontHelper, borderColor, TextAlignment.LEFT));
            table.addCell(createCell(offer.getType().toString(), fontHelper, borderColor, TextAlignment.LEFT));
            table.addCell(createCell(buildReservationDates(res), fontHelper, borderColor, TextAlignment.LEFT));
            table.addCell(createCell(String.valueOf(res.getQuantity()), fontHelper, borderColor, TextAlignment.RIGHT));

            Double unitPrice = offer.getPrice();
            if (unitPrice == null) {
                unitPrice = offer.getAveragePrice() != null ? offer.getAveragePrice() : offer.getPricePerNight();
            }
            table.addCell(createCell(unitPrice != null ? formatCurrency(unitPrice) : "0.00 MAD", fontHelper, borderColor, TextAlignment.RIGHT));
            table.addCell(createCell(res.getTotalPrice() != null ? formatCurrency(res.getTotalPrice()) : "0.00 MAD", fontHelper, borderColor, TextAlignment.RIGHT));
        }
        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private String buildOfferNameDetails(ma.safar.morocco.offer.entity.Offer offer) {
        String nameWithDetails = offer.getName();
        if (offer.getType() == ma.safar.morocco.offer.enums.OfferType.HOTEL) {
            nameWithDetails += "\nRoom: " + (offer.getRoomType() != null ? offer.getRoomType() : "Standard") + (offer.getStars() != null ? " (" + offer.getStars() + "* Stars)" : "");
        } else if (offer.getType() == ma.safar.morocco.offer.enums.OfferType.RESTAURANT) {
            nameWithDetails += "\nCuisine: " + (offer.getCuisineType() != null ? offer.getCuisineType() : "Various");
        } else if (offer.getType() == ma.safar.morocco.offer.enums.OfferType.ACTIVITY) {
            nameWithDetails += "\nDuration: " + (offer.getDuration() != null ? offer.getDuration() : "N/A") + " | " + (offer.getActivityType() != null ? offer.getActivityType() : "");
        }
        return nameWithDetails;
    }

    private String buildReservationDates(OfferReservation res) {
        if (res.getStartDate() != null && res.getEndDate() != null) {
            return res.getStartDate().toString() + " to " + res.getEndDate().toString();
        } else if (res.getStartDate() != null) {
            return res.getStartDate().toString();
        }
        return "N/A";
    }

    private void buildFooterAndTotal(Document document, Invoice invoice, String lang, PdfFont fontHelper, PdfFont fontBold, DeviceRgb footerColor) {
        Table totalTable = new Table(1).setHorizontalAlignment(HorizontalAlignment.RIGHT);
        Cell totalCell = new Cell().setBorder(Border.NO_BORDER).setBackgroundColor(new DeviceRgb(240, 240, 240));
        totalCell.setPadding(10);
        totalCell.add(new Paragraph(getTranslatedText(KEY_GRAND_TOTAL, lang) + " " + formatCurrency(invoice.getTotalAmount()))
                .setFont(fontBold).setFontSize(14).setTextAlignment(TextAlignment.RIGHT));
        totalTable.addCell(totalCell);
        document.add(totalTable);

        Paragraph footer = new Paragraph(getTranslatedText(KEY_THANK_YOU, lang))
                .setFont(fontHelper)
                .setFontSize(9)
                .setFontColor(footerColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setFixedPosition(40, 30, document.getPdfDocument().getDefaultPageSize().getWidth() - 80);
        document.add(footer);
    }

    private void addLogoToCell(Cell logoCell, PdfFont fontHelveticaBold, DeviceRgb brandColor) {
        try {
            ClassPathResource resource = new ClassPathResource("static/logo.png");
            try (InputStream is = resource.getInputStream()) {
                byte[] bytes = is.readAllBytes();
                ImageData imageData = ImageDataFactory.create(bytes);
                Image logo = new Image(imageData);
                logo.setHeight(50);
                logoCell.add(logo);
            }
        } catch (Exception e) {
            logoCell.add(new Paragraph("SAFAR MOROCCO").setFont(fontHelveticaBold).setFontColor(brandColor)
                    .setFontSize(16));
        }
    }

    private Cell createCell(String content, PdfFont font, DeviceRgb borderColor, TextAlignment alignment) {
        Cell cell = new Cell().add(new Paragraph(content).setFont(font));
        cell.setPadding(5);
        cell.setBorder(new SolidBorder(borderColor, 1));
        cell.setTextAlignment(alignment);
        return cell;
    }

    private String formatCurrency(Double amount) {
        return String.format(Locale.US, "%.2f MAD", amount);
    }

    private String getTranslatedText(String key, String lang) {
        if ("en".equalsIgnoreCase(lang) || "gb".equalsIgnoreCase(lang)) {
            switch (key) {
                case KEY_INVOICE: return KEY_INVOICE;
                case KEY_INVOICE_NO: return "Invoice No:";
                case KEY_DATE: return "Date:";
                case KEY_BILLED_TO: return "Billed To:";
                case KEY_ITINERARY: return "Itinerary:";
                case KEY_NAME: return KEY_NAME;
                case KEY_TYPE: return KEY_TYPE;
                case KEY_DATES: return KEY_DATES;
                case KEY_QTY: return KEY_QTY;
                case KEY_UNIT_PRICE: return KEY_UNIT_PRICE;
                case KEY_TOTAL: return KEY_TOTAL;
                case KEY_GRAND_TOTAL: return "GRAND TOTAL:";
                case KEY_THANK_YOU: return "Thank you for choosing Safar Morocco!";
                default: return key;
            }
        } else if ("es".equalsIgnoreCase(lang)) {
            switch (key) {
                case KEY_INVOICE: return "FACTURA";
                case KEY_INVOICE_NO: return "Nº Factura:";
                case KEY_DATE: return "Fecha:";
                case KEY_BILLED_TO: return "Facturado a:";
                case KEY_ITINERARY: return "Itinerario:";
                case KEY_NAME: return "Nombre";
                case KEY_TYPE: return "Tipo";
                case KEY_DATES: return "Fechas";
                case KEY_QTY: return "Cant.";
                case KEY_UNIT_PRICE: return "Precio Unit.";
                case KEY_TOTAL: return KEY_TOTAL;
                case KEY_GRAND_TOTAL: return "TOTAL GENERAL:";
                case KEY_THANK_YOU: return "¡Gracias por elegir Safar Morocco!";
                default: return key;
            }
        } else if ("ar".equalsIgnoreCase(lang) || "ma".equalsIgnoreCase(lang)) {
            switch (key) {
                case KEY_INVOICE: return "FACTURE";
                case KEY_INVOICE_NO: return "N° Facture :";
                case KEY_DATE: return "Date :";
                case KEY_BILLED_TO: return "Facturé à :";
                case KEY_ITINERARY: return "Itinéraire :";
                case KEY_NAME: return "Nom";
                case KEY_TYPE: return "Type";
                case KEY_DATES: return KEY_DATES;
                case KEY_QTY: return "Qté";
                case KEY_UNIT_PRICE: return "Prix Unit.";
                case KEY_TOTAL: return KEY_TOTAL;
                case KEY_GRAND_TOTAL: return "TOTAL GÉNÉRAL :";
                case KEY_THANK_YOU: return "Merci d'avoir choisi Safar Morocco !";
                default: return key;
            }
        }
        switch (key) {
            case KEY_INVOICE: return "FACTURE";
            case KEY_INVOICE_NO: return "N° Facture :";
            case KEY_DATE: return "Date :";
            case KEY_BILLED_TO: return "Facturé à :";
            case KEY_ITINERARY: return "Itinéraire :";
            case KEY_NAME: return "Nom";
            case KEY_TYPE: return "Type";
            case KEY_DATES: return KEY_DATES;
            case KEY_QTY: return "Qté";
            case KEY_UNIT_PRICE: return "Prix Unit.";
            case KEY_TOTAL: return KEY_TOTAL;
            case KEY_GRAND_TOTAL: return "TOTAL GÉNÉRAL :";
            case KEY_THANK_YOU: return "Merci d'avoir choisi Safar Morocco !";
            default: return key;
        }
    }

    @Override
    public InvoiceDTO getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        return mapToDTO(invoice);
    }

    @Override
    public List<InvoiceDTO> getInvoicesByUser(Long userId) {
        return invoiceRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<InvoiceDTO> getInvoicesByItinerary(Long itineraryId) {
        return invoiceRepository.findByItineraryId(itineraryId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    private InvoiceDTO mapToDTO(Invoice invoice) {
        return InvoiceDTO.builder()
                .id(invoice.getId())
                .userId(invoice.getUser().getId())
                .itineraryId(invoice.getItinerary().getId())
                .totalAmount(invoice.getTotalAmount())
                .generatedDate(invoice.getGeneratedDate())
                .status(invoice.getStatus())
                .pdfPath(invoice.getPdfPath())
                .build();
    }
}
