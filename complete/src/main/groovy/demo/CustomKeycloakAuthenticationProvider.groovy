package demo

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import grails.plugin.springsecurity.rest.token.AccessToken
import grails.plugin.springsecurity.userdetails.GrailsUser
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.web.json.JSONObject
import org.pac4j.oauth.profile.JsonHelper
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.util.Assert

/**
 * TODO: try having this class extend {@link grails.plugin.springsecurity.rest.RestAuthenticationProvider}
 *
 * Authenticates a request based on the token passed to verify it is a valid Keycloak token.
 * This is called by {@link KeycloakTokenValidationFilter}.
 */
@CompileStatic
@Slf4j
class CustomKeycloakAuthenticationProvider implements AuthenticationProvider, GrailsConfigurationAware {

    String KEYCLOAK_SERVER_URL
    String KEYCLOAK_REALM

    @Override
    void setConfiguration(Config co) {
        KEYCLOAK_SERVER_URL = co.getProperty('grails.plugin.springsecurity.rest.oauth.keycloak.serverUrl', String)
        KEYCLOAK_REALM = co.getProperty('grails.plugin.springsecurity.rest.oauth.keycloak.realm', String)
    }

    /**
     * TODO: figure out how to have a failure handler gracefully return the proper HTTP response when this throws
     * a KeycloakAuthenticationException because right now it is causing an infinite loop since it redirects
     * to /login/authfail?login_error=1 and that request is filtered through here and is throwing another exception
     *
     * Returns an authentication object based on the token value contained in the authentication parameter.
     * @throws AuthenticationException
     */
    @CompileDynamic
    @Override
    Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(AccessToken, authentication, "Only AccessToken is supported")
        AccessToken accessToken = authentication as AccessToken
        log.debug "Trying to validate token ${accessToken.accessToken}"
        String jwtBody = Keycloak2Profile.decodeJwt(accessToken.accessToken)
        final JsonNode json = JsonHelper.getFirstNode(jwtBody)
        String keycloakUserId = JsonHelper.getElement(json, "sub")
        String tokenType = JsonHelper.getElement(json, "typ")
        if (tokenType != "Bearer") {
            throw new KeycloakAuthenticationException("Token is not of type Bearer. It is of type ${tokenType}")
        }
        // Number of seconds since Unix epoch
        Long expirationTime = JsonHelper.getElement(json, "exp") as Long
        Date expiration = new Date(expirationTime * 1000)
        Boolean credentialsNonExpired = new Date() < expiration
        if (!credentialsNonExpired) {
            throw new KeycloakAuthenticationException("Token is expired. It expired on ${expiration}")
        }
        String username = JsonHelper.getElement(json, "preferred_username")
        // Maybe extract other properties like session_state
        /**
         *   "session_state": "3b2f8d12-b737-404f-81ae-7d4197825499",
         *   "scope": "email profile",
         *   "email_verified": false,
         *   "name": "Admin User",
         *   "preferred_username": "adminuser",
         *   "given_name": "Admin",
         *   "family_name": "User",
         *   "email": "adminuser@email.com"
         */

        final JsonNode realmAccessJson = JsonHelper.getElement(json, "realm_access") as JsonNode
        List<GrantedAuthority> authorities = []
        if (realmAccessJson != null) {
            ArrayNode rolesArray = JsonHelper.getElement(realmAccessJson, "roles") as ArrayNode
            for (String role in rolesArray) {
                // Remove the quotation marks at the front and end of the string
                String convertedRole = role.replaceAll('"', '')
                log.debug "Adding authority: ${convertedRole}"
                authorities << new SimpleGrantedAuthority(convertedRole)
            }
        }

        RestResponse restResponse = getUserInfoFromKeycloak(KEYCLOAK_SERVER_URL, KEYCLOAK_REALM, accessToken.accessToken)
        HttpStatus statusCode = restResponse.statusCode
        if (statusCode.value() != 200) {
            if (restResponse.json) {
                JSONObject responseBody = restResponse.json as JSONObject
                String message = "Fetching user info from Keycloak failed with status " +
                        "${statusCode.value()} (${statusCode.reasonPhrase}): ${responseBody}"
                log.debug message
                throw new KeycloakAuthenticationException("Token invalid. ${message}")
            }
            String message = "Fetching user info from Keycloak failed with status " +
                    "${statusCode.value()} (${statusCode.reasonPhrase})"
            log.debug message
            throw new KeycloakAuthenticationException("Token invalid. ${message}")
        }

        AccessToken token = new AccessToken(authorities)
        token.accessToken = accessToken.accessToken
        token.refreshToken = accessToken.refreshToken
        // Number of seconds left until the token expires
        token.expiration = expirationTime - System.currentTimeSeconds()
        token.details = authentication.details

        GrailsUser grailsUser = new GrailsUser(username, "passwordrandom", true,
                true, credentialsNonExpired, true, authorities, keycloakUserId)

        token.principal = grailsUser as UserDetails
        return token
    }

    /**
     * Call the Keycloak user info endpoint to validate the access token.
     * https://www.keycloak.org/docs/latest/server_admin/index.html#keycloak-server-oidc-uri-endpoints
     *
     * @param keycloakServerUrl
     * @param realm
     * @param accessToken
     * @return the RestResponse of the API call to the Keycloak user info endpoint
     */
    static RestResponse getUserInfoFromKeycloak(String keycloakServerUrl, String realm, String accessToken) {
        RestBuilder rest = new RestBuilder()
        String url = "$keycloakServerUrl/auth/realms/$realm/protocol/openid-connect/userinfo"
        String authToken = "Bearer ${accessToken}"
        RestResponse restResponse = rest.post(url) {
            auth(authToken)
            accept("application/json")
        }

        return restResponse
    }

    // TODO: try using the same definition of this method found in {@link grails.plugin.springsecurity.rest.RestAuthenticationProvider}
    @Override
    boolean supports(Class<?> aClass) {
        return AccessToken.isAssignableFrom(aClass)
    }
}
