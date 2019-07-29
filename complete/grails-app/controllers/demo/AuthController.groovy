package demo

import grails.config.Config
import grails.converters.JSON
import grails.core.support.GrailsConfigurationAware
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.rest.token.reader.TokenReader
import groovy.util.logging.Slf4j

import javax.servlet.http.Cookie

@Slf4j
class AuthController implements GrailsConfigurationAware {

    TokenReader tokenReader

    int jwtExpiration

    String grailsServerUrl

    static allowedMethods = [
            success: 'GET',
            logout: 'POST'
    ]

    @Secured('permitAll')
    def success(String token) {
        log.debug('token value {}', token)
        if (token) {
            Cookie cookie = jwtCookie(token)
            response.addCookie(cookie) // <1>
        }
        [grailsServerUrl: grailsServerUrl]
    }

    /**
     * Responding a cookie with the same name and maxAge equals 0 deletes the cookie.
     * Thus, it logs out the user when the JWT cookie and KEYCLOAK_IDENTITY cookie are both deleted.
     */
    @Secured('permitAll')
    def logout() {
        Cookie cookie = new Cookie( cookieName(), "" )
        cookie.setVersion(-1)
        cookie.path = "/"
        cookie.maxAge = 0
        response.addCookie(cookie)
        Cookie keycloakCookie = new Cookie( "KEYCLOAK_IDENTITY", "" )
        keycloakCookie.setVersion(-1)
        // TODO: have this set dynamically from configuration
        keycloakCookie.path = "/auth/realms/hclabs-dev/"
        keycloakCookie.maxAge = 0
        response.addCookie(keycloakCookie)
        [grailsServerUrl: grailsServerUrl]
    }

    protected Cookie jwtCookie(String tokenValue) {
        Cookie jwtCookie = new Cookie( cookieName(), tokenValue )
        jwtCookie.maxAge = jwtExpiration // <5>
        jwtCookie.path = '/'
        jwtCookie.setHttpOnly(true) // <3>
        if ( httpOnly() ) {
            jwtCookie.setSecure(true) // <4>
        }
        return jwtCookie
    }

    @Override
    void setConfiguration(Config co) {
        jwtExpiration = co.getProperty('grails.plugin.springsecurity.rest.token.storage.memcached.expiration', Integer, 3600) // <5>
        grailsServerUrl = co.getProperty('grails.serverURL', String)
    }

    protected boolean httpOnly() {
        grailsServerUrl?.startsWith('https')
    }

    protected String cookieName() {
        if ( tokenReader instanceof JwtCookieTokenReader ) {
            return ((JwtCookieTokenReader) tokenReader).cookieName  // <6>
        }
        return 'jwt'
    }
}