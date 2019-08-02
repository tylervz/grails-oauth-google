package demo

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.github.scribejava.core.model.OAuth2AccessToken
import com.github.scribejava.core.model.Verb
import org.pac4j.core.exception.HttpAction
import org.pac4j.core.profile.converter.Converters
import org.pac4j.oauth.config.OAuth20Configuration
import org.pac4j.oauth.profile.JsonHelper
import org.pac4j.oauth.profile.converter.JsonConverter
import org.pac4j.oauth.profile.definition.OAuth20ProfileDefinition
import org.pac4j.oauth.profile.google2.Google2Email

/**
 * Based on org.pac4j.oauth.profile.google2.Google2ProfileDefinition
 */
class Keycloak2ProfileDefinition extends OAuth20ProfileDefinition<Keycloak2Profile> {

    public static final String NAME = "name"
    public static final String GIVEN_NAME = "given_name"
    public static final String FAMILY_NAME = "name.familyName"
    public static final String EMAIL_VERIFIED = "email_verified"
    public static final String PREFERRED_USERNAME = "preferred_username"
    public static final String EMAILS = "emails"

    Keycloak2ProfileDefinition() {
        super({ x -> new Keycloak2Profile() })
        primary(NAME, Converters.STRING)
        primary(GIVEN_NAME, Converters.STRING)
        primary(FAMILY_NAME, Converters.STRING)
        primary(EMAIL_VERIFIED, Converters.BOOLEAN)
        primary(PREFERRED_USERNAME, Converters.STRING)
        primary(EMAILS, new JsonConverter(List.class, new TypeReference<List<Google2Email>>() {}))
    }

    /**
     * Returns the HTTP Method to request profile.
     *
     * @return http verb
     */
    @Override
    Verb getProfileVerb() {
        return Verb.POST
    }

    @Override
    String getProfileUrl(final OAuth2AccessToken accessToken, final OAuth20Configuration configuration) {
        final Map<String, String> params = configuration.getCustomParams()
        final String keycloakServerUrl = params['keycloakServerUrl']
        final String keycloakRealm = params['keycloakRealm']
        return "${keycloakServerUrl}/auth/realms/${keycloakRealm}/protocol/openid-connect/userinfo"
    }

    @Override
    Keycloak2Profile extractUserProfile(final String body) throws HttpAction {
        final Keycloak2Profile profile = newProfile()
        final JsonNode json = JsonHelper.getFirstNode(body)
        if (json != null) {
            profile.setId(JsonHelper.getElement(json, "sub"))
            for (final String attribute : getPrimaryAttributes()) {
                convertAndAdd(profile, attribute, JsonHelper.getElement(json, attribute))
            }
        }
        return profile
    }
}
