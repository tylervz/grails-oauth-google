package demo

import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.rest.token.AccessToken
import grails.plugin.springsecurity.userdetails.GrailsUser

import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR

/**
 * The controller used by the mobile app to sign in via an access code
 */
@Secured('permitAll')
class HandshakeController {

    AuthService authService
    DeviceManagementService deviceManagementService

    def register() {
        try {
            if (!(params.user)) {
                response.status = BAD_REQUEST.value()
                return render(["status": "fail", "msg": "fail.missing.user"] as JSON)
            }

            if (!(params.deviceID)) {
                response.status = BAD_REQUEST.value()
                return render(["status": "fail", "msg": "fail.missing.deviceID"] as JSON)
            }

            // TODO: make an API call to Keycloak to verify there is an enabled user with the specified email
//            if (!keycloakUser) {
//                response.status = BAD_REQUEST.value()
//                return render(["status": "fail", "msg": "fail.identifying.valid.user"] as JSON)
//            }

            String keycloakUserId = params.user
            Device device = deviceManagementService.registerDevice(params.deviceID, keycloakUserId)

            // Returning the registration code for the sake of testing/development
            return render(["status": "success", "msg": "successful.device.addition",
                           registrationCode: device.registrationCode, "userId": device.keycloakUserId] as JSON)
        } catch (Exception e) {
            response.status = INTERNAL_SERVER_ERROR.value()
            log.error "${e.class.name} in HandshakeController.register()", e
            return render(["status": "fail", "msg": e.getMessage()] as JSON)
        }
    }

    def authenticate() {
        def result = [
                errors: []
        ]
        String registrationCode = params.registrationCode
        try {
            if (!registrationCode) {
                throw new RuntimeException("missing registration code")
//                throw new MissingDeviceAuthenticationCodeException()
            }

            String deviceID = params.deviceID

            if (!deviceID) {
                throw new RuntimeException("handshake.authenticate.missingDeviceIdException.error")
//                throw new MissingDeviceIdException(g.message(code: "handshake.authenticate.missingDeviceIdException.error"))
            }

            deviceManagementService.authenticateDevice(deviceID, registrationCode)
            AccessToken accessToken = authService.authenticateUser(registrationCode)

            List<String> roles = accessToken.authorities.collect { it.authority }
            GrailsUser user = accessToken.principal as GrailsUser

            result.put("id", user.id)
            result.put("username", user.username)
//            result.put("firstName", user.firstName)
//            result.put("lastName", user.lastName)
            result.put("roles", roles)
            result.put("access_token", accessToken.accessToken)
            result.put("refresh_token", accessToken.refreshToken)

            def conf = SpringSecurityUtils.securityConfig
            Boolean useBearerToken = conf.rest.token.validation.useBearerToken

            if (useBearerToken) {
                result.put("token_type", "Bearer")
            }
//        } catch (MissingDeviceAuthenticationCodeException e) {
//            response.status = BAD_REQUEST.value()
//            result.errors << [message: g.message(code: 'handshake.authenticate.missingDeviceAuthenticationCodeException.error')]
//        } catch (MissingDeviceIdException e) {
//            response.status = BAD_REQUEST.value()
//            result.errors << [message: g.message(code: 'handshake.authenticate.missingDeviceIdException.error')]
//        } catch (DeviceNotFoundException e) {
//            response.status = BAD_REQUEST.value()
//            result.errors << [message: g.message(code: 'handshake.authenticate.deviceNotFoundException.error')]
//        } catch (ConfirmationCodeExpired e) {
//            response.status = BAD_REQUEST.value()
//            result.errors << [message: g.message(code: 'handshake.authenticate.confirmationCodeExpired.error')]
        } catch (ConfirmationCodeInvalidException e) {
            response.status = BAD_REQUEST.value()
            result.errors << [message: g.message(code: 'handshake.authenticate.confirmationCodeInvalidException.error')]
        } catch (Exception e) {
            response.status = INTERNAL_SERVER_ERROR.value()
            result.errors << [message: g.message(code: 'handshake.authenticate.defaultException.error')]
            log.error("An error occurred when trying to authenticate the device", e)
        } finally {
            render result as JSON
        }
    }
}
