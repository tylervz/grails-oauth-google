package demo

import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

@Transactional
class UserService {

    SpringSecurityService springSecurityService

    def getCurrentUser() {
        // TODO: figure out why springSecurityService is null when I try to inject it
        // https://grails-plugins.github.io/grails-spring-security-core/3.2.x/index.html#springSecurityService
        return springSecurityService.currentUser
    }

    Boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().authentication
        Collection authorities = auth.authorities
        return authorities.find { it.authority.equals("ROLE_OJT_ADMIN") } != null
    }

    Boolean isUser() {
        Authentication auth = SecurityContextHolder.getContext().authentication
        Collection authorities = auth.authorities
        return authorities.find { it.authority.equals("ROLE_OJT_USER") } != null
    }
}
