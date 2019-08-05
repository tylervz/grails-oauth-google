package demo

import grails.plugin.springsecurity.rest.token.AccessToken
import grails.plugin.springsecurity.rest.token.bearer.BearerTokenReader
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.http.MediaType

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest

@Slf4j
@CompileStatic
class JwtCookieTokenReader extends BearerTokenReader {

    final static String DEFAULT_COOKIE_NAME = 'JWT'

    String cookieName = DEFAULT_COOKIE_NAME

    @Override
    AccessToken findToken(HttpServletRequest request) {

        log.debug "Looking for jwt token in a cookie named {}", cookieName
        String tokenValue = null
        Cookie cookie = request.getCookies()?.find { Cookie cookie -> cookie.name.equalsIgnoreCase(cookieName) }

        if ( cookie ) {
            tokenValue = cookie.value
        }

        log.debug "Token: ${tokenValue}"
        if (tokenValue) {
            return new AccessToken(tokenValue)
        }

        log.debug "No jwt token in a cookie named ${cookieName} was found"
        log.debug "Looking for bearer token in Authorization header, query string or Form-Encoded body parameter"
        String header = request.getHeader('Authorization')

        if (header?.startsWith('Bearer') && header.length() >= 8) {
            log.debug "Found bearer token in Authorization header"
            tokenValue = header.substring(7)
        } else if (isFormEncoded(request) && !request.get) {
            log.debug "Found bearer token in request body"
            tokenValue = request.parameterMap['access_token']?.first()
        } else if (request.queryString?.contains('access_token')) {
            log.debug "Found bearer token in query string"
            tokenValue = request.getParameter('access_token')
        } else {
            log.debug "No token found"
        }

        log.debug "Token: ${tokenValue}"
        return tokenValue ? new AccessToken(tokenValue) : null
    }

    private static boolean isFormEncoded(HttpServletRequest servletRequest) {
        servletRequest.contentType && MediaType.parseMediaType(servletRequest.contentType).isCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED)
    }
}
