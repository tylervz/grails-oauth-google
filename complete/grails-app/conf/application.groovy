//tag::staticRules[]
grails.plugin.springsecurity.controllerAnnotations.staticRules = [
	[pattern: '/',               access: ['permitAll']],
	[pattern: '/error',          access: ['permitAll']],
	[pattern: '/index',          access: ['permitAll']],
	[pattern: '/index.gsp',      access: ['permitAll']],
	[pattern: '/shutdown',       access: ['permitAll']],
	[pattern: '/assets/**',      access: ['permitAll']],
	[pattern: '/**/js/**',       access: ['permitAll']],
	[pattern: '/**/css/**',      access: ['permitAll']],
	[pattern: '/**/images/**',   access: ['permitAll']],
	[pattern: '/**/favicon.ico', access: ['permitAll']]
]
//end::staticRules[]
//tag::grailsPluginSpringSecurityRest[]
grails {
	plugin {
		springsecurity {
			rest {
				oauth {
					keycloak {
						scope = demo.KeycloakOauth2Client.Keycloak2Scope.EMAIL_AND_PROFILE_AND_OPENID //<8>
					}
				}
			}
		}
	}
}
//end::grailsPluginSpringSecurityRest[]

//tag::filterChain[]
// Stateless chain that allows anonymous access when no token is sent. If however a token is on the request, it will be validated.
String ANONYMOUS_FILTERS = 'anonymousAuthenticationFilter,restTokenValidationFilter,restExceptionTranslationFilter,filterInvocationInterceptor' // <1>
grails.plugin.springsecurity.filterChain.chainMap = [
		[pattern: '/dbconsole/**',      filters: 'none'],
		[pattern: '/assets/**',      filters: 'none'],
		[pattern: '/**/js/**',       filters: 'none'],
		[pattern: '/**/css/**',      filters: 'none'],
		[pattern: '/**/images/**',   filters: 'none'],
		[pattern: '/**/favicon.ico', filters: 'none'],
		[pattern: '/', filters: ANONYMOUS_FILTERS], // <1>
		[pattern: '/book/show/*', filters: ANONYMOUS_FILTERS],  // <1>
		[pattern: '/bookFavourite/index', filters: ANONYMOUS_FILTERS], // <1>
		[pattern: '/auth/success', filters: ANONYMOUS_FILTERS], // <1>
		[pattern: '/oauth/authenticate/keycloak', filters: ANONYMOUS_FILTERS], // <1>
		[pattern: '/oauth/callback/keycloak', filters: ANONYMOUS_FILTERS], // <1>
		[pattern: '/**', filters: ANONYMOUS_FILTERS]
		// Stateless chain that doesn’t allow anonymous access. Thus, the token will always be required, and if missing, a Bad Request reponse will be sent back to the client.
		// [pattern: '/**', filters: 'JOINED_FILTERS,-anonymousAuthenticationFilter,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter,-rememberMeAuthenticationFilter'],  // <1>
]
//end::filterChain[]

//tag::logoutHandlers[]
grails.plugin.springsecurity.logout.handlerNames = ['rememberMeServices', 'securityContextLogoutHandler', 'cookieClearingLogoutHandler']
//end::logoutHandlers[]