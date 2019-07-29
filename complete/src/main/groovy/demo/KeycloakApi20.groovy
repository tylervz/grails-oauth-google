package demo

import com.github.scribejava.core.builder.api.DefaultApi20
import com.github.scribejava.core.extractors.TokenExtractor
import com.github.scribejava.core.model.OAuth2AccessToken

/**
 * Based on com.github.scribejava.apis.GoogleApi20.java
 */
class KeycloakApi20 extends DefaultApi20 {

    protected KeycloakApi20() {
    }

    private static class InstanceHolder {
        private static final KeycloakApi20 INSTANCE = new KeycloakApi20()
    }

    static KeycloakApi20 instance() {
        return InstanceHolder.INSTANCE
    }

    // TODO: have this set dynamically from configuration once everything is working
    @Override
    String getAccessTokenEndpoint() {
        return "http://localhost:8080/auth/realms/hclabs-dev/protocol/openid-connect/token"
    }

    // TODO: have this set dynamically from configuration once everything is working
    @Override
    protected String getAuthorizationBaseUrl() {
        return "http://localhost:8080/auth/realms/hclabs-dev/protocol/openid-connect/auth"
    }

    @Override
    TokenExtractor<OAuth2AccessToken> getAccessTokenExtractor() {
        return KeycloakOAuth2AccessTokenJsonExtractor.instance()
    }
}
