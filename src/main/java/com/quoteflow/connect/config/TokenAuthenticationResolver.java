package com.quoteflow.connect.config;

/**
 * Point d'intÃ©gration unique avec votre couche JWT existante.
 * ImplÃ©mentez cette interface (voir JwtTokenAuthenticationResolver) pour
 * relier QuoteFlow Connect Ã  votre JwtService / JwtUtil actuel.
 */
public interface TokenAuthenticationResolver {

    /**
     * @param bearerToken token brut SANS le prÃ©fixe "Bearer "
     * @return le principal si le token est valide, sinon null
     */
    ConnectPrincipal resolve(String bearerToken);
}

