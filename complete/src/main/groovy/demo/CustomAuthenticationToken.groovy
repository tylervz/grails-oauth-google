package demo

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class CustomAuthenticationToken extends AbstractAuthenticationToken {
    String confirmationCode
    Object principal

    CustomAuthenticationToken(Collection<? extends GrantedAuthority> authorities) {
        super(authorities)
    }

    @Override
    Object getCredentials() {
        return null
    }

    @Override
    Object getPrincipal() {
        return principal
    }
}
