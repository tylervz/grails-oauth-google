An example Grails 3.3.2 application based off of the following guide that has
Keycloak Oauth2 authentication using Spring Security Rest

[Guide](https://guides.grails.org/grails-oauth-google/guide/index.html)

# Getting Started

To quickly get a Keycloak server running in a Docker container, clone this other repo and follow its instructions on the `docker` branch:

https://github.com/hypercision/keycloak/tree/master/docker

Or if you don't have access to that repo, you can use this one on the `docker` branch:

https://github.com/tylervz/grails3-spring-security-keycloak-minimal/tree/docker

You'll have to change some values in [`applicaiton.yml`](/complete/grails-app/conf/application.yml)
to match the configuration settings of your Keycloak server.

    # The Keycloak URL
    grails.plugin.springsecurity.rest.oauth.keycloak.serverUrl
    # The realm you want to use with the Grails application
    grails.plugin.springsecurity.rest.oauth.keycloak.realm
    # Client ID for the Keycloak client you want to use
    grails.plugin.springsecurity.rest.oauth.keycloak.key
    # Client secret
    grails.plugin.springsecurity.rest.oauth.keycloak.secret

Note that when running the Grails application you will need to set environment variables for `KeycloakOauth2Client` to load the configuration.

    KEYCLOAK_REALM=hclabs-dev
    KEYCLOAK_SERVER=http://localhost:8080

# Authentication

Once you have the Grails application running, open your browser to <http://localhost:8082>.
Then click on the "Sign in With Google" button to sign in with Keycloak.

After entering the credentials of a Keycloak user, you will be authenticated using OAuth2 and the token will be stored in a cookie.
