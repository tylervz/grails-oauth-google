package demo

import groovy.transform.CompileStatic
import org.springframework.security.core.AuthenticationException

@CompileStatic
class KeycloakAuthenticationException extends AuthenticationException {
    KeycloakAuthenticationException(String msg, Throwable t) {
        super(msg, t)
    }

    KeycloakAuthenticationException(String msg) {
        super(msg)
    }
}
