package com.quoteflow.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// URLs des webhooks n8n, injectees depuis application.properties (prefixe app.n8n)

@Component
@ConfigurationProperties(prefix = "app.n8n")
@Getter
@Setter
public class WebhookProperties {

    private boolean enabled = true;
    private String baseUrl;
    private Webhook webhook = new Webhook();

    @Getter
    @Setter
    public static class Webhook {
        private String confirmationDemande;
        private String changementStatut;
        private String envoiDevis;
    }
}
