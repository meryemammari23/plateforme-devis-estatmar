package com.quoteflow.connect.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * Configuration STOMP/SockJS.
 *  - Endpoint de handshake : /ws (avec fallback SockJS)
 *  - Broker simple en mÃ©moire : /topic (diffusion) et /queue (point Ã  point)
 *  - PrÃ©fixe applicatif : /app (vers les @MessageMapping)
 *  - PrÃ©fixe utilisateur : /user (routage par session authentifiÃ©e)
 *
 * En production multi-instances, remplacer le simple broker par RabbitMQ/ActiveMQ
 * (enableStompBrokerRelay) â€” le code applicatif reste identique.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor authInterceptor;

    @Value("${quoteflow.cors.frontend:http://localhost:5173}")
    private String frontendOrigin;

    public WebSocketConfig(StompAuthChannelInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(frontendOrigin)
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authInterceptor);
    }
}

