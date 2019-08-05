package demo

import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.rest.authentication.RestAuthenticationEventPublisher
import grails.plugin.springsecurity.rest.token.AccessToken
import grails.plugin.springsecurity.rest.token.generation.TokenGenerator
import grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

@Transactional
class AuthService {

    ProviderManager authenticationManager
    RestAuthenticationEventPublisher authenticationEventPublisher
    TokenGenerator tokenGenerator
    TokenStorageService tokenStorageService

    AccessToken authenticateUser(String confirmationCode) {
        CustomAuthenticationToken token = new CustomAuthenticationToken()
        token.confirmationCode = confirmationCode

        AccessToken accessToken
        // Call CustomAuthenticationProvider.authenticate()
        Authentication auth = authenticationManager.authenticate(token)
        SecurityContextHolder.getContext().setAuthentication(auth)

        accessToken = tokenGenerator.generateAccessToken(auth.principal as UserDetails)
        log.debug "Generated token: ${accessToken}"
        tokenStorageService.storeToken(accessToken.accessToken, auth.principal as UserDetails)
        authenticationEventPublisher.publishTokenCreation(accessToken)
        return accessToken
    }
}
