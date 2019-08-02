package demo

import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.rest.error.CallbackErrorHandler
import groovy.util.logging.Slf4j
import org.pac4j.core.context.J2EContext
import org.pac4j.core.context.WebContext

/**
 * Custom implementation of {@link grails.plugin.springsecurity.rest.RestOauthController}
 * so that CustomRestOauthService can be used in order to apply the user roles decoded from the JWT from Keycloak.
 */
@Slf4j
@Secured(['permitAll'])
class RestOauthController {

    CallbackErrorHandler callbackErrorHandler
    CustomRestOauthService customRestOauthService

    final String CALLBACK_ATTR = "spring-security-rest-callback"

    /**
     * Custom implementation of the callback method. The only change is that we are using
     * customRestOauthService instead of restOauthService.
     */
    def callback(String provider) {
        WebContext context = new J2EContext(request, response)
        def frontendCallbackUrl
        if (session[CALLBACK_ATTR]) {
            log.debug "Found callback URL in the HTTP session"
            frontendCallbackUrl = session[CALLBACK_ATTR]
        } else {
            log.debug "Found callback URL in the configuration file"
            frontendCallbackUrl = grailsApplication.config.grails.plugin.springsecurity.rest.oauth.frontendCallbackUrl
        }

        try {
            String tokenValue = customRestOauthService.storeAuthentication(provider, context)
            frontendCallbackUrl = getCallbackUrl(frontendCallbackUrl, tokenValue)

        } catch (Exception e) {
            def errorParams = new StringBuilder()

            Map params = callbackErrorHandler.convert(e)
            params.each { key, value ->
                errorParams << "&${key}=${value.encodeAsURL()}"
            }

            frontendCallbackUrl = getCallbackUrl(frontendCallbackUrl, errorParams.toString())
        }

        log.debug "Redirecting to ${frontendCallbackUrl}"
        redirect url: frontendCallbackUrl
    }

    private String getCallbackUrl(baseUrl, String queryStringSuffix) {
        session[CALLBACK_ATTR] = null
        return baseUrl instanceof Closure ? baseUrl(queryStringSuffix) : baseUrl + queryStringSuffix
    }
}
