package com.quoteflow.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Pont Spring Boot -> n8n.
 * Appelle les 3 workflows webhook publiés (confirmation-demande /
 * changement-statut / envoi-devis) avec les payloads exacts du doc
 * N8N_WORKFLOWS.md. Un echec n'interrompt JAMAIS la transaction metier.
 */
@Service
public class N8nWebhookService {

    private final RestClient restClient;

    public N8nWebhookService(
            @Value("${app.n8n.base-url:http://localhost:5678/webhook}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /** 4.1 — Confirmation de demande : { email, nom, reference } */
    public void confirmationDemande(String email, String nom, String reference) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email", email);
        body.put("nom", nom);
        body.put("reference", reference);
        post("/confirmation-demande", body);
    }

    /** 4.2 — Changement de statut : { email, nom, reference, statut } */
    public void changementStatut(String email, String nom, String reference, String statut) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email", email);
        body.put("nom", nom);
        body.put("reference", reference);
        body.put("statut", statut);
        post("/changement-statut", body);
    }

    /** 4.3 — Envoi du devis PDF : { email, nom, reference, pdfUrl } */
    public void envoiDevis(String email, String nom, String reference, String pdfUrl) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email", email);
        body.put("nom", nom);
        body.put("reference", reference);
        body.put("pdfUrl", pdfUrl);
        post("/envoi-devis", body);
    }

    private void post(String path, Map<String, Object> body) {
        try {
            restClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            System.out.println("[n8n] Webhook OK -> " + path);
        } catch (Exception e) {
            // Best effort : si n8n n'est pas lance, on log et on continue.
            System.err.println("[n8n] Webhook KO -> " + path + " : " + e.getMessage());
        }
    }
}
