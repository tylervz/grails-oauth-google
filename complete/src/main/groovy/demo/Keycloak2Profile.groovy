package demo

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
}
