An example Grails 3.3.2 application based off of the following guide that has
Keycloak bearer token authentication using Spring Security Rest

[Guide](https://guides.grails.org/grails-oauth-google/guide/index.html)

It used to have working Oauth2 authentication with Keycloak, but I broke that
in order to get bearer token authentication working.

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

Once you have the Grails application running, you can interact with it by sending requests to the BookController
i.e. http://localhost:8082/book/userDetails

Unless the method that you are calling has a `@Secured('permitAll')` annotation, the request needs to have an Authorization header
of "Bearer <access_token>" with an access token obtained from your Keycloak server.

You can get an access token by making a request like this:

    curl --data "username=$USERNAME&password=$PASSWORD&grant_type=password&client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET" $SERVER/auth/realms/$REALM/protocol/openid-connect/token

If you're using Postman, you'll want to put the data in the Body of the request and use x-www-form-urlencoded.

![image](https://user-images.githubusercontent.com/8753239/62800777-df4b3c80-baa9-11e9-81de-2c03414fe140.png)


## Signing in using the UI does not work

If you open your browser to <http://localhost:8082> and then click on the "Sign in With Google" button
to sign in with Keycloak, you'll get an error message after you sign in.

This is because the `KeycloakTokenValidationFilter` will be invoked and throw an exception since the token
stored in a cookie is formatted differently and so we cannot verify it is of type Bearer.

## Notes

I tried getting an access token from Keycloak then disabling that user
and seeing if Grails and/or Keycloak still accepted the access token. They both accepted the token.
Keycloak won't grant new access tokens for the disabled user, but it still accepts non-expired ones.
