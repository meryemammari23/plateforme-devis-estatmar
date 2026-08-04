package com.quoteflow.service;

import com.quoteflow.entity.Devis;
import com.quoteflow.entity.LigneDevis;
import com.quoteflow.entity.User;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class PdfService {

    private static final String SOCIETE_NOM = "QuoteFlow";
    private static final String SOCIETE_LIGNE1 = "123 Avenue Mohammed V";
    private static final String SOCIETE_LIGNE2 = "Oujda, Maroc";
    private static final String SOCIETE_CONTACT = "contact@quoteflow.ma";
    private static final BigDecimal TAUX_TVA = new BigDecimal("0.20");

    // Palette harmonisee avec le frontend (navy / teal Estatmar)
    private static final DeviceRgb NAVY = new DeviceRgb(10, 15, 30);      // #0a0f1e - fond bandeau
    private static final DeviceRgb ACCENT = new DeviceRgb(13, 148, 136);  // #0d9488 - teal, entetes/totaux
    private static final DeviceRgb TEAL_PALE = new DeviceRgb(94, 234, 212); // #5eead4 - texte clair sur navy

    private static final DeviceRgb GRIS_CLAIR = new DeviceRgb(238, 246, 245); // #eef6f5 - alternance lignes
    private static final DeviceRgb BLANC = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb BLEU_PALE = TEAL_PALE;
    private static final DateTimeFormatter DATE_FR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Value("${app.pdf.devis-dir:./generated/devis}")
    private String outputDir;

    public byte[] genererDevis(Devis devis) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);
            doc.setMargins(36, 36, 36, 36);
            construireContenu(doc, devis);
            doc.close();
        } catch (Exception e) {
            throw new RuntimeException("Erreur generation PDF devis #"
                    + (devis != null ? devis.getId() : "?"), e);
        }
        return baos.toByteArray();
    }

    public String genererEtSauvegarder(Devis devis) {
        byte[] contenu = genererDevis(devis);
        try {
            Path dir = Paths.get(outputDir);
            Files.createDirectories(dir);
            String ref = devis.getDemande() != null ? devis.getDemande().getReference()
                    : String.valueOf(devis.getId());
            Path fichier = dir.resolve("devis-" + safe(ref) + ".pdf");
            Files.write(fichier, contenu);
            return fichier.toAbsolutePath().toString();
        } catch (Exception e) {
            throw new RuntimeException("Impossible d ecrire le PDF du devis", e);
        }
    }

    private void construireContenu(Document doc, Devis devis) {
        String ref = devis.getDemande() != null ? valeur(devis.getDemande().getReference()) : "-";
        String date = devis.getDateCreation() != null ? devis.getDateCreation().format(DATE_FR) : "-";

        Table band = new Table(UnitValue.createPercentArray(new float[]{2, 1})).useAllAvailableWidth();
        Cell gauche = noBorder(new Cell()).setBackgroundColor(NAVY).setPadding(14);
        gauche.add(new Paragraph(SOCIETE_NOM).setFontColor(ColorConstants.WHITE).setBold().setFontSize(22));
        gauche.add(new Paragraph("DEVIS").setFontColor(BLEU_PALE).setFontSize(12).setMarginTop(2));
        Cell droite = noBorder(new Cell()).setBackgroundColor(NAVY).setPadding(14)
                .setTextAlignment(TextAlignment.RIGHT);
        droite.add(new Paragraph("Reference").setFontColor(BLEU_PALE).setFontSize(8));
        droite.add(new Paragraph(ref).setFontColor(ColorConstants.WHITE).setBold().setFontSize(12));
        droite.add(new Paragraph("Date : " + date).setFontColor(BLEU_PALE).setFontSize(9).setMarginTop(4));
        band.addCell(gauche);
        band.addCell(droite);
        doc.add(band);

        Table accentBand = new Table(UnitValue.createPercentArray(new float[]{1})).useAllAvailableWidth();
        accentBand.addCell(noBorder(new Cell()).setBackgroundColor(ACCENT).setHeight(4).setPadding(0));
        doc.add(accentBand);

        Table infos = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth().setMarginTop(18);
        Cell emetteur = noBorder(new Cell()).setPadding(4);
        emetteur.add(label("EMETTEUR"));
        emetteur.add(new Paragraph(SOCIETE_NOM).setBold());
        emetteur.add(petit(SOCIETE_LIGNE1));
        emetteur.add(petit(SOCIETE_LIGNE2));
        emetteur.add(petit(SOCIETE_CONTACT));
        Cell clientCell = noBorder(new Cell()).setPadding(4);
        clientCell.add(label("CLIENT"));
        if (devis.getDemande() != null && devis.getDemande().getClient() != null) {
            User c = devis.getDemande().getClient();
            clientCell.add(new Paragraph(valeur(c.getPrenom()) + " " + valeur(c.getNom())).setBold());
            clientCell.add(petit(valeur(c.getEmail())));
        } else {
            clientCell.add(petit("-"));
        }
        infos.addCell(emetteur);
        infos.addCell(clientCell);
        doc.add(infos);

        Table t = new Table(UnitValue.createPercentArray(new float[]{5, 1, 2, 2}))
                .useAllAvailableWidth().setMarginTop(18);
        t.addHeaderCell(headerCell("Designation", TextAlignment.LEFT));
        t.addHeaderCell(headerCell("Qte", TextAlignment.CENTER));
        t.addHeaderCell(headerCell("Prix U.", TextAlignment.RIGHT));
        t.addHeaderCell(headerCell("Sous-total", TextAlignment.RIGHT));

        int i = 0;
        if (devis.getLignes() != null) {
            for (LigneDevis l : devis.getLignes()) {
                DeviceRgb bg = (i % 2 == 1) ? GRIS_CLAIR : BLANC;
                t.addCell(bodyCell(valeur(l.getDesignation()), TextAlignment.LEFT, bg));
                t.addCell(bodyCell(String.valueOf(l.getQuantite()), TextAlignment.CENTER, bg));
                t.addCell(bodyCell(formatMontant(l.getPrixUnitaire()), TextAlignment.RIGHT, bg));
                t.addCell(bodyCell(formatMontant(l.getSousTotal()), TextAlignment.RIGHT, bg));
                i++;
            }
        }
        doc.add(t);

        BigDecimal ht = devis.getMontantTotal() != null ? devis.getMontantTotal() : BigDecimal.ZERO;
        BigDecimal tva = ht.multiply(TAUX_TVA).setScale(2, RoundingMode.HALF_UP);
        BigDecimal ttc = ht.add(tva).setScale(2, RoundingMode.HALF_UP);

        Table totaux = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .setWidth(UnitValue.createPercentValue(45))
                .setHorizontalAlignment(HorizontalAlignment.RIGHT).setMarginTop(14);
        totaux.addCell(totalLabel("Total HT", false));
        totaux.addCell(totalValeur(formatMontant(ht), false, false));
        totaux.addCell(totalLabel("TVA (20%)", false));
        totaux.addCell(totalValeur(formatMontant(tva), false, false));
        totaux.addCell(totalLabel("Total TTC", true));
        totaux.addCell(totalValeur(formatMontant(ttc), true, true));
        doc.add(totaux);

        if (devis.getCommentaire() != null && !devis.getCommentaire().isBlank()) {
            doc.add(label("COMMENTAIRE").setMarginTop(18));
            doc.add(new Paragraph(devis.getCommentaire()).setFontSize(10));
        }

        doc.add(new Paragraph("Devis valable 30 jours. " + SOCIETE_NOM + " - " + SOCIETE_CONTACT)
                .setFontSize(8).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER).setMarginTop(30)
                .setBorderTop(new SolidBorder(GRIS_CLAIR, 1f)).setPaddingTop(8));
    }

    private Cell noBorder(Cell c) { return c.setBorder(Border.NO_BORDER); }

    private Paragraph label(String t) {
        return new Paragraph(t).setFontSize(8).setBold().setFontColor(ACCENT).setMarginBottom(3);
    }

    private Paragraph petit(String t) {
        return new Paragraph(valeur(t)).setFontSize(9).setFontColor(ColorConstants.GRAY);
    }

    private Cell headerCell(String t, TextAlignment align) {
        return noBorder(new Cell()).setBackgroundColor(ACCENT).setPadding(6)
                .add(new Paragraph(t).setBold().setFontColor(ColorConstants.WHITE).setFontSize(10)
                        .setTextAlignment(align));
    }

    private Cell bodyCell(String t, TextAlignment align, DeviceRgb bg) {
        return noBorder(new Cell()).setBackgroundColor(bg).setPadding(6)
                .add(new Paragraph(t).setFontSize(10).setTextAlignment(align));
    }

    private Cell totalLabel(String t, boolean fort) {
        Cell c = noBorder(new Cell()).setPadding(6);
        Paragraph p = new Paragraph(t).setBold().setFontSize(10);
        if (fort) { c.setBackgroundColor(ACCENT); p.setFontColor(ColorConstants.WHITE); }
        return c.add(p);
    }

    private Cell totalValeur(String t, boolean fort, boolean fond) {
        Paragraph p = new Paragraph(t).setFontSize(fort ? 12 : 10).setTextAlignment(TextAlignment.RIGHT);
        Cell c = noBorder(new Cell()).setPadding(6);
        if (fort) p.setBold();
        if (fond) { c.setBackgroundColor(ACCENT); p.setFontColor(ColorConstants.WHITE); }
        else if (fort) p.setFontColor(ACCENT);
        return c.add(p);
    }

    private String formatMontant(BigDecimal m) {
        if (m == null) return "-";
        return String.format(Locale.FRANCE, "%,.2f MAD", m);
    }

    private String valeur(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private String safe(String s) {
        return (s == null ? "devis" : s).replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
