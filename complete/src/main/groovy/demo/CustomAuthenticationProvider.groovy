package demo

import grails.plugin.springsecurity.userdetails.GrailsUser
import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

@CompileStatic
@Slf4j
class CustomAuthenticationProvider implements AuthenticationProvider {

    @CompileDynamic
    @Override
    Authentication authenticate(Authentication authentication) throws AuthenticationException {
        CustomAuthenticationToken token = authentication as CustomAuthenticationToken
        String confirmationCode = token.confirmationCode
        Device device = Device.findByRegistrationCode(confirmationCode)

        if (device) {
            GrailsUser grailsUser = getUserDetailsFromKeycloak(device.keycloakUserId)

            token.details = authentication.details

            token.principal = grailsUser
            return token
        } else {
            throw new ConfirmationCodeInvalidException("The confirmation code is invalid")
        }
    }

    /**
     * Use the Keycloak Admin Rest API to fetch info about the user
     * https://www.keycloak.org/docs-api/6.0/rest-api/index.html#_users_resource
     */
    GrailsUser getUserDetailsFromKeycloak(String keycloakUserId) {
        // TODO: have these values loaded from config
        String SERVER = "http://localhost:8080"
        String ADMIN_USERNAME = "admin"
        String ADMIN_PASSWORD = "admin"
        String REALM = "hclabs-dev"

        RestBuilder rest = new RestBuilder()
        String url = "$SERVER/auth/realms/master/protocol/openid-connect/token"
        MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>()
        form.add("grant_type", "password")
        form.add("client_id", "admin-cli")
        form.add("username", ADMIN_USERNAME)
        form.add("password", ADMIN_PASSWORD)
        RestResponse restResponse = rest.post(url) {
            accept("application/json")
            contentType("application/x-www-form-urlencoded")
            body(form)
        }

        String authToken

        println "Auth status code: ${restResponse.statusCode.value()}, auth response: ${restResponse.responseEntity.body}"
        if ( restResponse.statusCode.value() == 200 && restResponse.json ) {
            JSONObject responseBody = restResponse.json as JSONObject
            String tokenType = responseBody.token_type
            String accessToken = responseBody.access_token
            authToken = tokenType + " " + accessToken
            println authToken
        }

        if (!authToken) {
            // Quit because the access token was not retrieved
            // TODO: throw exception
            println "Could not get an access token"
            return
        }

        String userUrl = "$SERVER/auth/admin/realms/$REALM/users/${keycloakUserId}"
        RestResponse userResponse = rest.get(userUrl) {
            auth(authToken)
            contentType("application/json")
        }

        String username
        Boolean enabled
        println "User status code: ${userResponse.statusCode.value()}, user response: ${userResponse.responseEntity.body}"
        if ( restResponse.statusCode.value() == 200 && restResponse.json ) {
            println "success!"
            JSONObject responseBody = userResponse.json as JSONObject
            username = responseBody['username']
            enabled = responseBody['enabled']
        } else {
            // TODO: throw exception
            println "error getting user!"
            return
        }

        String userRolesUrl = "${userUrl}/role-mappings/realm"
        RestResponse userRolesResponse = rest.get(userRolesUrl) {
            auth(authToken)
            contentType("application/json")
        }

        GrailsUser grailsUser
        println "User roles status code: ${userRolesResponse.statusCode.value()}, user roles response: ${userRolesResponse.responseEntity.body}"
        if ( restResponse.statusCode.value() == 200 && restResponse.json ) {
            println "success!"
            JSONArray responseBody = userRolesResponse.json as JSONArray
            List<GrantedAuthority> authorities = []
            for (role in responseBody) {
                String name = role['name']
                // Assert that it is not null
                if (name == null || name == "") {
                    println "role was are null or blank: ${name}"
                    // TODO: throw exception
                    return
                }

                log.debug "Adding authority: ${name}"
                authorities << new SimpleGrantedAuthority(name)
            }

            grailsUser = new GrailsUser(username, "passwordrandom", enabled,
                    true, true, true, authorities, keycloakUserId)
        } else {
            println "error getting user roles!"
            // TODO: throw exception
            return
        }

        return grailsUser
    }

    @Override
    boolean supports(Class<?> aClass) {
        return (CustomAuthenticationToken.class.isAssignableFrom(aClass))
    }
}
