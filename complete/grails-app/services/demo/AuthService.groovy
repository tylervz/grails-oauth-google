package demo

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.rest.token.AccessToken
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import groovy.transform.CompileStatic
import org.grails.web.json.JSONObject
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

@CompileStatic
@Transactional
class AuthService implements GrailsConfigurationAware {

    ProviderManager authenticationManager

    String KEYCLOAK_SERVER_URL
    String KEYCLOAK_REALM
    String KEYCLOAK_CLIENT_ID
    String KEYCLOAK_CLIENT_SECRET
    String REALM_ADMIN_USERNAME
    String REALM_ADMIN_PASSWORD

    @Override
    void setConfiguration(Config co) {
        KEYCLOAK_SERVER_URL = co.getProperty('grails.plugin.springsecurity.rest.oauth.keycloak.serverUrl', String)
        KEYCLOAK_REALM = co.getProperty('grails.plugin.springsecurity.rest.oauth.keycloak.realm', String)
        KEYCLOAK_CLIENT_ID = co.getProperty('grails.plugin.springsecurity.rest.oauth.keycloak.key', String)
        KEYCLOAK_CLIENT_SECRET = co.getProperty('grails.plugin.springsecurity.rest.oauth.keycloak.secret', String)
        REALM_ADMIN_USERNAME = co.getProperty('grails.plugin.springsecurity.rest.oauth.keycloak.realmAdminUsername', String)
        REALM_ADMIN_PASSWORD = co.getProperty('grails.plugin.springsecurity.rest.oauth.keycloak.realmAdminPassword', String)
    }

    /**
     * Get a Keycloak access token for the specified user
     *
     * @param keycloakUserId the username or user id of the Keycloak user we want to get a token for
     * @return an AccessToken for the specified user (that has both access_token and refresh_token)
     */
    AccessToken authenticateUser(String keycloakUserId) {
        Authentication auth
        try {
            String startingToken = getTokenForRealmAdmin(KEYCLOAK_SERVER_URL, KEYCLOAK_REALM,
                    REALM_ADMIN_USERNAME, REALM_ADMIN_PASSWORD)
            log.debug "Starting access token: ${startingToken}"
            AccessToken token = impersonationTokenExchange(KEYCLOAK_SERVER_URL, KEYCLOAK_REALM,
                    KEYCLOAK_CLIENT_ID, KEYCLOAK_CLIENT_SECRET, startingToken, keycloakUserId)
            log.debug "Exchanged access token with minimal values: ${token}"
            // Calls CustomKeycloakAuthenticationProvider.authenticate()
            auth = authenticationManager.authenticate(token)
        } catch (AuthenticationException e) {
            throw e
        }

        SecurityContextHolder.getContext().setAuthentication(auth)
        AccessToken accessToken = auth as AccessToken

        log.debug "Exchanged token: ${accessToken}"
        return accessToken
    }

    /**
     * Get an access token String for the realm admin (aka a user in the specified realm
     * that has the impersonation role for that realm)
     */
    static String getTokenForRealmAdmin(String keycloakServerUrl, String realm,
            String realmAdminUsername, String realmAdminPassword) {

        RestBuilder rest = new RestBuilder()
        String url = "$keycloakServerUrl/auth/realms/$realm/protocol/openid-connect/token"
        MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>()
        form.add("grant_type", "password")
        form.add("client_id", "admin-cli")
        form.add("username", realmAdminUsername)
        form.add("password", realmAdminPassword)
        RestResponse restResponse = rest.post(url) {
            accept("application/json")
            contentType("application/x-www-form-urlencoded")
            body(form)
        }

        println "Starting auth status code: ${restResponse.statusCode.value()}, starting auth response: ${restResponse.responseEntity.body}"
        if ( restResponse.statusCode.value() == 200 && restResponse.json ) {
            JSONObject responseBody = restResponse.json as JSONObject
            return responseBody.access_token
        } else {
            // TODO: make this a more specific exception
            throw new RuntimeException("Error when getting access token from Keycloak for " +
                    "realm admin ${realmAdminUsername}: ${restResponse.statusCode.value()}")
        }
    }

    /**
     * Make a token exchange call to Keycloak to get an access token for the specified user.
     * The AccessToken returned just has the access_token and refresh_token properties set.
     *
     * https://www.keycloak.org/docs/latest/securing_apps/index.html#impersonation
     *
     * @param keycloakServerUrl the URL of the Keycloak server
     * @param realm the Keycloak realm that the specified user (identified by keycloakUserId) belongs to
     * @param clientId the Keycloak client we are using for this Grails application
     * @param clientSecret the secret of the Keycloak client we are using for this Grails application
     * @param startingAccessToken the token we are exchanging for an entirely different token
     * @param keycloakUserId the username or user id of the Keycloak user we want to get a token for
     * @return an AccessToken for the specified user (that has both access_token and refresh_token)
     */
    private static AccessToken impersonationTokenExchange(String keycloakServerUrl, String realm,
            String clientId, String clientSecret, String startingAccessToken, String keycloakUserId) {

        RestBuilder rest = new RestBuilder()
        String url = "$keycloakServerUrl/auth/realms/$realm/protocol/openid-connect/token"
        MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>()
        form.add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
        // The client id of the subject token's client aka starting client
        form.add("client_id", clientId)
        // The client secret of the subject token's client
        form.add("client_secret", clientSecret)
        form.add("subject_token", startingAccessToken)
        form.add("requested_subject", keycloakUserId)
        form.add("requested_token_type", "urn:ietf:params:oauth:token-type:refresh_token")
        RestResponse restResponse = rest.post(url) {
            accept("application/json")
            contentType("application/x-www-form-urlencoded")
            body(form)
        }

        println "Target auth status code: ${restResponse.statusCode.value()}, target auth response: ${restResponse.responseEntity.body}"

        JSONObject responseBody
        if ( restResponse.statusCode.value() == 200 && restResponse.json ) {
            responseBody = restResponse.json as JSONObject
        } else {
            // TODO: make this a more specific exception
            throw new RuntimeException("Error when getting target access token from Keycloak: ${restResponse.statusCode.value()}")
        }

        String accessToken = responseBody.access_token
        String refreshToken = responseBody.refresh_token

        List<GrantedAuthority> authorities = []
        AccessToken token = new AccessToken(authorities)
        token.accessToken = accessToken
        token.refreshToken = refreshToken
        return token
    }
}
