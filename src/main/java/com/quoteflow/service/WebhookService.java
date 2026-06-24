package com.quoteflow.service;

import com.quoteflow.config.WebhookProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Envoie des webhooks HTTP POST a n8n a chaque evenement cle.
 * n8n se charge ensuite de l'envoi SMTP des emails.
 * Les payloads correspondent au referentiel des workflows n8n :
 *  - confirmation : { email, nom, reference }
 *  - changement   : { email, nom, reference, statut }
 *  - envoi devis  : { email, nom, reference, pdfUrl }
 */

@Service
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final WebhookProperties props;
    private final RestClient restClient;

    public WebhookService(WebhookProperties props) {
        this.props = props;
        // Timeouts courts : 2s pour se connecter, 3s pour la reponse.
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    public void notifierConfirmationDemande(String email, String nom, String reference) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("email", email);
        payload.put("nom", nom);
        payload.put("reference", reference);
        envoyer(props.getWebhook().getConfirmationDemande(), payload);
    }

    public void notifierChangementStatut(String email, String nom, String reference, String nouveauStatut) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("email", email);
        payload.put("nom", nom);
        payload.put("reference", reference);
        payload.put("statut", nouveauStatut);
        envoyer(props.getWebhook().getChangementStatut(), payload);
    }

    public void notifierEnvoiDevis(String email, String nom, String reference, String pdfUrl) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("email", email);
        payload.put("nom", nom);
        payload.put("reference", reference);
        payload.put("pdfUrl", pdfUrl);
        envoyer(props.getWebhook().getEnvoiDevis(), payload);
    }

    // Appel HTTP defensif ET non bloquant : lance en arriere-plan avec timeout.
    // Le flux metier (creation demande, devis...) n'attend jamais n8n.
     
    private void envoyer(String url, Map<String, Object> payload) {
        if (!props.isEnabled() || url == null || url.isBlank()) {
            log.info("[n8n desactive] payload non envoye : {}", payload);
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                restClient.post().uri(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(payload)
                        .retrieve()
                        .toBodilessEntity();
                log.info("Webhook n8n envoye a {}", url);
            } catch (Exception e) {
                log.warn("Echec d'envoi du webhook n8n ({}) : {}", url, e.getMessage());
            }
        });
    }
}
