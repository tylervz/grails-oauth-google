package demo

import org.pac4j.core.context.WebContext
import org.pac4j.core.util.CommonHelper
import org.pac4j.oauth.client.OAuth20Client
import org.pac4j.oauth.credentials.authenticator.OAuth20Authenticator
import org.pac4j.oauth.credentials.extractor.OAuth20CredentialsExtractor
import org.pac4j.oauth.exception.OAuthCredentialsException
import org.pac4j.oauth.redirect.OAuth20RedirectActionBuilder

/**
 * <p>This class is the OAuth client to authenticate users in Keycloak using OAuth protocol version 2.0.</p>
 * <p>The <i>scope</i> is by default : {@link Keycloak2Scope#EMAIL_AND_PROFILE}, but it can also but set to : {@link Keycloak2Scope#PROFILE}
 * or {@link Keycloak2Scope#EMAIL}.</p>
 * <p>It returns a {@link Keycloak2Profile}.</p>
 *
 * Based off of org.pac4j.oauth.client.Google2Client
 */
class KeycloakOauth2Client extends OAuth20Client<Keycloak2Profile> {

    enum Keycloak2Scope {
        EMAIL,
        PROFILE,
        EMAIL_AND_PROFILE_AND_OPENID
    }

    protected final static String PROFILE_SCOPE = "profile"

    protected final static String EMAIL_SCOPE = "email"

    protected final static String OPENID_SCOPE = "openid"

    protected Keycloak2Scope scope = Keycloak2Scope.EMAIL_AND_PROFILE_AND_OPENID

    String keycloakServerUrl
    String keycloakRealm

    KeycloakOauth2Client() {
        setConfiguration()
    }

    KeycloakOauth2Client(final String key, final String secret) {
        setKey(key)
        setSecret(secret)
        setConfiguration()
    }

    void setConfiguration() {
        keycloakServerUrl = System.getenv('grails.plugin.springsecurity.rest.oauth.keycloak.serverUrl') ?:
                System.getenv('KEYCLOAK_SERVER')
        keycloakRealm = System.getenv('grails.plugin.springsecurity.rest.oauth.keycloak.realm') ?:
                System.getenv('KEYCLOAK_REALM')
    }

    @Override
    protected void clientInit(final WebContext context) {
        CommonHelper.assertNotNull("scope", this.scope)
        final String scopeValue
        if (this.scope == Keycloak2Scope.EMAIL) {
            scopeValue = this.EMAIL_SCOPE
        } else if (this.scope == Keycloak2Scope.PROFILE) {
            scopeValue = this.PROFILE_SCOPE
        } else {
            scopeValue = this.PROFILE_SCOPE + " " + this.EMAIL_SCOPE + " " + this.OPENID_SCOPE
        }
        Map<String, String> customParams = [keycloakRealm: keycloakRealm, keycloakServerUrl: keycloakServerUrl]
        KeycloakApi20.instance().keycloakRealm = customParams.keycloakRealm
        KeycloakApi20.instance().keycloakServerUrl = customParams.keycloakServerUrl
        configuration.setApi(KeycloakApi20.instance())
        configuration.setProfileDefinition(new Keycloak2ProfileDefinition())
        configuration.setScope(scopeValue)
        configuration.setWithState(true)
        configuration.setTokenAsHeader(true)
        configuration.setCustomParams(customParams)
        configuration.setHasBeenCancelledFactory({ ctx ->
            final String error = ctx.getRequestParameter(OAuthCredentialsException.ERROR)
            // user has denied permissions
            if ("access_denied".equals(error)) {
                return true
            }
            return false
        })
        setConfiguration(configuration)

        defaultRedirectActionBuilder(new OAuth20RedirectActionBuilder(configuration))
        defaultCredentialsExtractor(new OAuth20CredentialsExtractor(configuration))
        defaultAuthenticator(new OAuth20Authenticator(configuration))
        defaultProfileCreator(new Keycloak2ProfileCreator<>(configuration))
    }

    Keycloak2Scope getScope() {
        return this.scope
    }

    void setScope(final Keycloak2Scope scope) {
        this.scope = scope
    }
}
