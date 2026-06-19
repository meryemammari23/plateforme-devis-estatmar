package com.quoteflow.connect.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * Intercepte la frame STOMP CONNECT pour authentifier la session WebSocket
 * Ã  partir du JWT envoyÃ© dans l'en-tÃªte "Authorization: Bearer ...".
 * Une fois le principal posÃ©, toutes les frames suivantes de la session en hÃ©ritent.
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final TokenAuthenticationResolver resolver;

    public StompAuthChannelInterceptor(TokenAuthenticationResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Token manquant pour la connexion WebSocket");
            }
            String token = authHeader.substring(7);
            ConnectPrincipal principal = resolver.resolve(token);
            if (principal == null) {
                throw new IllegalArgumentException("Token WebSocket invalide");
            }
            accessor.setUser(principal);
        }
        return message;
    }
}

