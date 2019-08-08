package demo

import grails.compiler.GrailsCompileStatic

/**
 * Represents a mobile device
 */
@GrailsCompileStatic
class Device {
    // A unique id for the mobile device
    String deviceId
    String keycloakUserId

    String registrationCode
    Date registrationCodeTS = new Date()

    static constraints = {
        deviceId nullable: false, unique: true
        keycloakUserId nullable: false
    }
}
