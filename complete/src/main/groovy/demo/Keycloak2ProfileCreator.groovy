package demo

import com.github.scribejava.core.exceptions.OAuthException
import com.github.scribejava.core.model.OAuth2AccessToken
import org.pac4j.core.context.WebContext
import org.pac4j.core.exception.HttpAction
import org.pac4j.core.exception.HttpCommunicationException
import org.pac4j.core.exception.TechnicalException
import org.pac4j.oauth.config.OAuth20Configuration
import org.pac4j.oauth.credentials.OAuth20Credentials
import org.pac4j.oauth.profile.creator.OAuth20ProfileCreator
import org.pac4j.oauth.profile.definition.OAuthProfileDefinition

class Keycloak2ProfileCreator extends OAuth20ProfileCreator {

    Keycloak2ProfileCreator(OAuth20Configuration configuration) {
        super(configuration)
    }

    @Override
    Keycloak2Profile create(final OAuth20Credentials credentials, final WebContext context) throws HttpAction {
        try {
            final OAuth2AccessToken token = getAccessToken(credentials)
            return retrieveUserProfileFromToken(token)
        } catch (final OAuthException e) {
            throw new TechnicalException(e)
        }
    }

    /**
     * Custom implementation of org.pac4j.oauth.profile.creator.OAuthProfileCreator.retrieveUserProfileFromToken
     * so that we can call profile.retrieveRolesFromToken()
     */
    Keycloak2Profile retrieveUserProfileFromToken(final OAuth2AccessToken accessToken) throws HttpAction {
        final OAuthProfileDefinition profileDefinition = configuration.getProfileDefinition()
        final String profileUrl = profileDefinition.getProfileUrl(accessToken, configuration)
        final String body = sendRequestForData(accessToken, profileUrl, profileDefinition.getProfileVerb())
        logger.info("UserProfile: " + body)
        if (body == null) {
            throw new HttpCommunicationException("No data found for accessToken: " + accessToken)
        }
        final Keycloak2Profile profile = (Keycloak2Profile) configuration.getProfileDefinition().extractUserProfile(body)
        addAccessTokenToProfile(profile, accessToken)
        profile.retrieveRolesFromToken()
        return profile
    }
}
