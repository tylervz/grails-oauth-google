package demo

import org.pac4j.core.context.WebContext
import org.pac4j.core.util.CommonHelper
import org.pac4j.oauth.client.OAuth20Client
import org.pac4j.oauth.exception.OAuthCredentialsException

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

    KeycloakOauth2Client() {
    }

    KeycloakOauth2Client(final String key, final String secret) {
        setKey(key)
        setSecret(secret)
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
        configuration.setApi(KeycloakApi20.instance())
        configuration.setProfileDefinition(new Keycloak2ProfileDefinition())
        configuration.setScope(scopeValue)
        configuration.setWithState(true)
        configuration.setTokenAsHeader(true)
        configuration.setHasBeenCancelledFactory({ ctx ->
            final String error = ctx.getRequestParameter(OAuthCredentialsException.ERROR)
            // user has denied permissions
            if ("access_denied".equals(error)) {
                return true
            }
            return false
        })
        setConfiguration(configuration)
        defaultLogoutActionBuilder(new KeycloakLogoutActionBuilder<>())

        super.clientInit(context)
    }

    Keycloak2Scope getScope() {
        return this.scope
    }

    void setScope(final Keycloak2Scope scope) {
        this.scope = scope
    }
}
