package com.quoteflow.service;

import com.quoteflow.entity.DemandeDevis;
import com.quoteflow.entity.PieceJointe;
import com.quoteflow.entity.StatutDemande;
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
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class DemandePdfService {

    // Palette harmonisee avec le frontend (navy / teal Estatmar)
    private static final DeviceRgb NAVY = new DeviceRgb(10, 15, 30);        // #0a0f1e - fond bandeau
    private static final DeviceRgb ACCENT = new DeviceRgb(13, 148, 136);    // #0d9488 - teal, entetes/labels
    private static final DeviceRgb TEAL_PALE = new DeviceRgb(94, 234, 212); // #5eead4 - texte clair sur navy
    private static final DeviceRgb GRIS_CLAIR = new DeviceRgb(238, 246, 245); // #eef6f5 - alternance lignes
    private static final DeviceRgb BLANC = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb BLEU_PALE = TEAL_PALE;
    private static final DateTimeFormatter DATE_FR =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Value("${app.pdf.demandes-dir:./generated/demandes}")
    private String outputDir;

    public byte[] genererRecap(DemandeDevis demande) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);
            doc.setMargins(36, 36, 36, 36);
            construireContenu(doc, demande);
            doc.close();
        } catch (Exception e) {
            throw new RuntimeException("Erreur generation recap PDF demande "
                    + (demande != null ? demande.getReference() : "?"), e);
        }
        return baos.toByteArray();
    }

    public String genererEtSauvegarder(DemandeDevis demande) {
        byte[] contenu = genererRecap(demande);
        try {
            Path dir = Paths.get(outputDir);
            Files.createDirectories(dir);
            Path fichier = dir.resolve("recap-" + safe(demande.getReference()) + ".pdf");
            Files.write(fichier, contenu);
            return fichier.toAbsolutePath().toString();
        } catch (Exception e) {
            throw new RuntimeException("Impossible d ecrire le recap PDF", e);
        }
    }

    private void construireContenu(Document doc, DemandeDevis demande) {
        Table band = new Table(UnitValue.createPercentArray(new float[]{2, 1})).useAllAvailableWidth();
        Cell gauche = noBorder(new Cell()).setBackgroundColor(NAVY).setPadding(14);
        gauche.add(new Paragraph("QuoteFlow").setFontColor(ColorConstants.WHITE).setBold().setFontSize(22));
        gauche.add(new Paragraph("Recapitulatif de demande").setFontColor(BLEU_PALE)
                .setFontSize(12).setMarginTop(2));
        Cell droite = noBorder(new Cell()).setBackgroundColor(NAVY).setPadding(14)
                .setTextAlignment(TextAlignment.RIGHT);
        droite.add(new Paragraph("Reference").setFontColor(BLEU_PALE).setFontSize(8));
        droite.add(new Paragraph(valeur(demande.getReference()))
                .setFontColor(ColorConstants.WHITE).setBold().setFontSize(12));
        band.addCell(gauche);
        band.addCell(droite);
        doc.add(band);

        Table accentBand = new Table(UnitValue.createPercentArray(new float[]{1})).useAllAvailableWidth();
        accentBand.addCell(noBorder(new Cell()).setBackgroundColor(ACCENT).setHeight(4).setPadding(0));
        doc.add(accentBand);

        Table meta = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth().setMarginTop(16);
        Cell badgeCell = noBorder(new Cell()).setPadding(4);
        badgeCell.add(label("STATUT"));
        badgeCell.add(badge(demande.getStatut()));
        Cell dateCell = noBorder(new Cell()).setPadding(4).setTextAlignment(TextAlignment.RIGHT);
        dateCell.add(label("SOUMISE LE"));
        dateCell.add(new Paragraph(demande.getDateCreation() != null
                ? demande.getDateCreation().format(DATE_FR) : "-").setFontSize(10));
        meta.addCell(badgeCell);
        meta.addCell(dateCell);
        doc.add(meta);

        doc.add(label("CLIENT").setMarginTop(16));
        User client = demande.getClient();
        if (client != null) {
            doc.add(new Paragraph(valeur(client.getPrenom()) + " " + valeur(client.getNom())).setBold());
            doc.add(petit(valeur(client.getEmail())));
        } else {
            doc.add(petit("-"));
        }

        doc.add(label("BUDGET PROPOSE").setMarginTop(14));
        doc.add(new Paragraph(formatMontant(demande.getBudget()))
                .setFontSize(13).setBold().setFontColor(ACCENT));

        doc.add(label("DESCRIPTION DU PROJET").setMarginTop(14));
        doc.add(new Paragraph(valeur(demande.getDescription())).setFontSize(10));

        doc.add(label("PIECES JOINTES").setMarginTop(14));
        if (demande.getPiecesJointes() != null && !demande.getPiecesJointes().isEmpty()) {
            Table pj = new Table(UnitValue.createPercentArray(new float[]{4, 2, 2}))
                    .useAllAvailableWidth();
            pj.addHeaderCell(headerCell("Fichier"));
            pj.addHeaderCell(headerCell("Type"));
            pj.addHeaderCell(headerCell("Taille"));
            int i = 0;
            for (PieceJointe p : demande.getPiecesJointes()) {
                DeviceRgb bg = (i % 2 == 1) ? GRIS_CLAIR : BLANC;
                pj.addCell(bodyCell(valeur(p.getNomFichier()), bg));
                pj.addCell(bodyCell(valeur(p.getType()), bg));
                pj.addCell(bodyCell(formatTaille(p.getTaille()), bg));
                i++;
            }
            doc.add(pj);
        } else {
            doc.add(petit("Aucune piece jointe."));
        }

        doc.add(new Paragraph("Document genere automatiquement par QuoteFlow - usage interne.")
                .setFontSize(8).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER).setMarginTop(30)
                .setBorderTop(new SolidBorder(GRIS_CLAIR, 1f)).setPaddingTop(8));
    }

    private Cell noBorder(Cell c) { return c.setBorder(Border.NO_BORDER); }

    private Paragraph label(String t) {
        return new Paragraph(t).setFontSize(8).setBold().setFontColor(ACCENT).setMarginBottom(3);
    }

    private Paragraph petit(String t) {
        return new Paragraph(valeur(t)).setFontSize(10).setFontColor(ColorConstants.GRAY);
    }

    private Paragraph badge(StatutDemande statut) {
        DeviceRgb couleur;
        if (statut == null) {
            couleur = new DeviceRgb(107, 114, 128);
        } else {
            switch (statut) {
                case EN_ATTENTE: couleur = new DeviceRgb(163, 98, 10);  break; // #a3620a
                case EN_COURS:   couleur = new DeviceRgb(3, 105, 161);  break; // #0369a1
                case VALIDE:     couleur = new DeviceRgb(21, 128, 61);  break; // #15803d
                case REFUSE:     couleur = new DeviceRgb(185, 28, 28);  break; // #b91c1c
                default:         couleur = new DeviceRgb(107, 114, 128);
            }
        }
        return new Paragraph(statut != null ? statut.toString() : "-")
                .setBackgroundColor(couleur).setFontColor(ColorConstants.WHITE)
                .setBold().setFontSize(10).setPadding(5)
                .setTextAlignment(TextAlignment.CENTER)
                .setWidth(UnitValue.createPointValue(110));
    }

    private Cell headerCell(String t) {
        return noBorder(new Cell()).setBackgroundColor(ACCENT).setPadding(6)
                .add(new Paragraph(t).setBold().setFontColor(ColorConstants.WHITE).setFontSize(10));
    }

    private Cell bodyCell(String t, DeviceRgb bg) {
        return noBorder(new Cell()).setBackgroundColor(bg).setPadding(6)
                .add(new Paragraph(t).setFontSize(10));
    }

    private String formatMontant(BigDecimal m) {
        if (m == null) return "Non precise";
        return String.format(Locale.FRANCE, "%,.2f MAD", m);
    }

    private String formatTaille(Long octets) {
        if (octets == null) return "-";
        if (octets < 1024) return octets + " o";
        if (octets < 1024 * 1024) return String.format(Locale.FRANCE, "%.1f Ko", octets / 1024.0);
        return String.format(Locale.FRANCE, "%.1f Mo", octets / (1024.0 * 1024.0));
    }

    private String valeur(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private String safe(String s) {
        return (s == null ? "demande" : s).replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
