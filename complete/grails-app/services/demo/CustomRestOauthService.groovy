package demo

import grails.core.GrailsApplication
import grails.plugin.springsecurity.rest.RestOauthService
import grails.plugin.springsecurity.rest.authentication.RestAuthenticationEventPublisher
import grails.plugin.springsecurity.rest.oauth.OauthUser
import grails.plugin.springsecurity.rest.oauth.OauthUserDetailsService
import grails.plugin.springsecurity.rest.token.AccessToken
import grails.plugin.springsecurity.rest.token.generation.TokenGenerator
import grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import groovy.util.logging.Slf4j
import org.pac4j.core.client.IndirectClient
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.Credentials
import org.pac4j.core.profile.CommonProfile
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder

/**
 * Copy of {@link grails.plugin.springsecurity.rest.RestOauthService} that has been
 * tweaked to add roles from the access token because it is a JWT from Keycloak.
 */
@Slf4j
class CustomRestOauthService {

    static transactional = false

    GrailsApplication grailsApplication
    OauthUserDetailsService oauthUserDetailsService
    RestAuthenticationEventPublisher authenticationEventPublisher
    RestOauthService restOauthService
    TokenGenerator tokenGenerator
    TokenStorageService tokenStorageService

    CommonProfile getProfile(String provider, WebContext context) {
        IndirectClient client = restOauthService.getClient(provider)
        Credentials credentials = client.getCredentials context

        log.debug "Querying provider to fetch User ID"
        client.getUserProfile credentials, context
    }

    /**
     * Custom implementation of getOauthUser
     */
    OauthUser getOauthUser(String provider, CommonProfile profile) {
        def providerConfig = grailsApplication.config.grails.plugin.springsecurity.rest.oauth."${provider}"
        Set<String> defaultRoles = providerConfig.defaultRoles as Set<String>
        // Add the roles from the profile
        profile.roles.each { defaultRoles.add(it) }
        // Convert them all to authorities
        List<GrantedAuthority> roles = defaultRoles.collect { new SimpleGrantedAuthority(it) }
        oauthUserDetailsService.loadUserByUserProfile(profile, roles)
    }

    String storeAuthentication(String provider, WebContext context) {
        CommonProfile profile = getProfile(provider, context)
        log.debug "User's ID: ${profile.id}"

        OauthUser userDetails = getOauthUser(provider, profile)
        AccessToken accessToken = tokenGenerator.generateAccessToken(userDetails)
        log.debug "Generated REST authentication token: ${accessToken}"

        log.debug "Storing token on the token storage"
        tokenStorageService.storeToken(accessToken.accessToken, userDetails)

        authenticationEventPublisher.publishTokenCreation(accessToken)

        SecurityContextHolder.context.setAuthentication(accessToken)

        return accessToken.accessToken
    }
}
