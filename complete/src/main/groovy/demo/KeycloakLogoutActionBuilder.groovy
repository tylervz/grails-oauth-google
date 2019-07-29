package demo

import org.pac4j.core.context.WebContext
import org.pac4j.core.logout.LogoutActionBuilder
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.redirect.RedirectAction
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Based on org.pac4j.core.logout.GoogleLogoutActionBuilder
 */
class KeycloakLogoutActionBuilder <U extends CommonProfile> implements LogoutActionBuilder<U> {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakLogoutActionBuilder.class)

    @Override
    RedirectAction getLogoutAction(final WebContext context, final U currentProfile, final String targetUrl) {
        // TODO: have this set dynamically from configuration
        // TODO: maybe add a redirect_uri parameter at the end so that it comes back to the home page of the app
        final String redirectUrl = "http://localhost:8080/auth/realms/hclabs-dev/protocol/openid-connect/logout"
        logger.debug("redirectUrl: {}", redirectUrl)
        return RedirectAction.redirect(redirectUrl)
    }
}
