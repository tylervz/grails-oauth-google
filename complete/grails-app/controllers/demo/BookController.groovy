package demo

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.rest.oauth.OauthUser
import groovy.transform.CompileStatic
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

@Secured('permitAll')
@CompileStatic
class BookController {

    static allowedMethods = [index: 'GET', show: 'GET']

    BookDataService bookDataService
    UserService userService

    def index() {
        [bookList: bookDataService.findAll()]
    }

    def show(Long id) {
        [bookInstance: bookDataService.findById(id)]
    }

    @Secured(['ROLE_OJT_USER'])
    def usersOnly() {
        render([message: "HEY, only signed in users should be able to see this"] as JSON)
    }

    @Secured(['ROLE_OJT_ADMIN'])
    def adminsOnly() {
        render([message: "HEY, only admins should be able to see this"] as JSON)
    }

    @Secured(['ROLE_OJT_USER', 'ROLE_OJT_ADMIN'])
    def userDetails() {
        Authentication auth = SecurityContextHolder.getContext().authentication
        println auth
        Collection authorities = auth.authorities
        Boolean ojtUser = authorities.find { it.authority.equals("ROLE_OJT_USER") } != null
        Boolean ojtAdmin = authorities.find { it.authority.equals("ROLE_OJT_ADMIN") } != null
        OauthUser user = auth.principal as OauthUser
        Keycloak2Profile profile = user.userProfile as Keycloak2Profile
        String keycloakUserId = profile.id
        // Note if a user has a blank first name (or other blank attribute), then that attribute
        // won't be listed in this Map.
        Map userAttributes = profile.attributes

        render([message: "able to access userDetails", isOjtUser: ojtUser,
                isOjtAdmin: ojtAdmin, authorities: authorities,
                keycloakUserId: keycloakUserId, userAttributes: userAttributes] as JSON)
    }

    def testUserService() {
        Boolean ojtUser = userService.isUser()
        Boolean ojtAdmin = userService.isAdmin()
        render([message: "userService is working!", isOjtUser: ojtUser,
                isOjtAdmin: ojtAdmin] as JSON)
    }
}
