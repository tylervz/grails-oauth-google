package demo

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import org.apache.commons.codec.binary.Base64
import org.pac4j.oauth.profile.JsonHelper
import org.pac4j.oauth.profile.OAuth20Profile
import org.pac4j.oauth.profile.google2.Google2Email

/**
 * <p>This class is the user profile for Keycloak (using OAuth protocol version 2) with appropriate getters.</p>
 * <p>It is returned by the {@link demo.KeycloakOauth2Client}.</p>
 *
 * Based on org.pac4j.oauth.profile.google2.Google2Profile
 */
class Keycloak2Profile extends OAuth20Profile {

    private static final long serialVersionUID = -1507361238506547901L

    /**
     * Extracts the roles from profile's access token (since it is a JWT)
     * and adds them to the profile's roles.
     *
     * @return the roles
     */
    Set<String> retrieveRolesFromToken() {
        Set<String> roles = []
        // Decode the profile's access token and retrieve the roles from it
        String accessToken = getAccessToken()
        String jwtBody = decodeJwt(accessToken)
        final JsonNode json = JsonHelper.getFirstNode(jwtBody, "realm_access")
        if (json != null) {
            ArrayNode rolesArray = JsonHelper.getElement(json, "roles") as ArrayNode
            for (String role in rolesArray) {
                // Remove the quotation marks at the front and end of the string
                String convertedRole = role.replaceAll('"', '')
                roles.add(convertedRole)
            }
        }
        // Add the roles to the profile
        addRoles(roles)
        return roles
    }

    @Override
    String getEmail() {
        final List<Google2Email> list = getEmails()
        if (list != null && !list.isEmpty()) {
            return list.get(0).getEmail()
        } else {
            return null
        }
    }

    @Override
    String getFirstName() {
        return (String) getAttribute(Keycloak2ProfileDefinition.GIVEN_NAME)
    }

    @Override
    String getFamilyName() {
        return (String) getAttribute(Keycloak2ProfileDefinition.FAMILY_NAME)
    }

    String getName() {
        return (String) getAttribute(Keycloak2ProfileDefinition.NAME)
    }

    Boolean getEmailVerified() {
        return (Boolean) getAttribute(Keycloak2ProfileDefinition.EMAIL_VERIFIED)
    }

    String getPreferredUsername() {
        return (String) getAttribute(Keycloak2ProfileDefinition.PREFERRED_USERNAME)
    }

    @SuppressWarnings("unchecked")
    List<Google2Email> getEmails() {
        return (List<Google2Email>) getAttribute(Keycloak2ProfileDefinition.EMAILS)
    }

    /**
     * Decode a JWT (JSON Web Token).
     * Taken from https://stackoverflow.com/a/38916927/8049180
     *
     * @param jwtToken
     * @return the decoded body of the JWT
     */
    static String decodeJwt(String jwtToken) {
        String[] split_string = jwtToken.split("\\.")
        String base64EncodedHeader = split_string[0]
        String base64EncodedBody = split_string[1]
        String base64EncodedSignature = split_string[2]

        Base64 base64Url = new Base64(true)
        String body = new String(base64Url.decode(base64EncodedBody))
        return body
    }
}
