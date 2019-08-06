//tag::tokenReaderImport[]
import demo.JwtCookieTokenReader
import demo.CustomKeycloakAuthenticationProvider
import demo.KeycloakTokenValidationFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean

//end::tokenReaderImport[]
//tag::cookieClearingImport[]
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler
//end::cookieClearingImport[]
beans = {
    //tag::tokenReader[]
    tokenReader(JwtCookieTokenReader) {
        cookieName = 'jwt'
    }
    //end::tokenReader[]
    //tag::cookieClearing[]
    cookieClearingLogoutHandler(CookieClearingLogoutHandler, ['jwt'])
    //end::cookieClearing[]
    myAuthenticationProvider(CustomKeycloakAuthenticationProvider) {}
    keycloakTokenValidationFilter(KeycloakTokenValidationFilter) {
        keycloakAuthenticationProvider = ref('myAuthenticationProvider')
        authenticationSuccessHandler = ref('authenticationSuccessHandler')
        authenticationFailureHandler = ref('authenticationFailureHandler')
        authenticationEventPublisher = ref('authenticationEventPublisher')
        tokenReader = ref('tokenReader')
        validationEndpointUrl = "/api/validate"
        active = true
        enableAnonymousAccess = true
    }
    keycloakTokenValidationFilterDeregistrationBean(FilterRegistrationBean) {
        filter = ref('keycloakTokenValidationFilter')
        enabled = false
    }
}
