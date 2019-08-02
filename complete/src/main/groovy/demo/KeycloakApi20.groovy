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

    String keycloakServerUrl
    String keycloakRealm

    private static class InstanceHolder {
        private static final KeycloakApi20 INSTANCE = new KeycloakApi20()
    }

    static KeycloakApi20 instance() {
        return InstanceHolder.INSTANCE
    }

    @Override
    String getAccessTokenEndpoint() {
        return "${keycloakServerUrl}/auth/realms/${keycloakRealm}/protocol/openid-connect/token"
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return "${keycloakServerUrl}/auth/realms/${keycloakRealm}/protocol/openid-connect/auth"
    }

    @Override
    TokenExtractor<OAuth2AccessToken> getAccessTokenExtractor() {
        return KeycloakOAuth2AccessTokenJsonExtractor.instance()
    }
}
