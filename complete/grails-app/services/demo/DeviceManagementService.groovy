package demo

import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import java.text.DecimalFormat

@CompileStatic
@Transactional
class DeviceManagementService {

    @CompileDynamic
    Device registerDevice(String deviceId, String keycloakUserId) {
        Device device = Device.findByDeviceId(deviceId)

        Random randomGenerator = new Random(System.currentTimeMillis())
        int randomInt = randomGenerator.nextInt(99999)
        // Ensure randomInt is not 0, for the sake of the mobile app's acceptance testing
        while (randomInt == 0) {
            randomInt = randomGenerator.nextInt(99999)
        }
        String accessCode = (new DecimalFormat("#00000")).format(randomInt)
        println "access code is " + accessCode
        if (device) {
            device.keycloakUserId = keycloakUserId
            device.registrationCode = accessCode
            device.registrationCodeTS = new Date(System.currentTimeMillis())
            device.save(failOnError: true)
        } else {
            device = Device.create()
            device.deviceId = deviceId
            device.keycloakUserId = keycloakUserId
            device.registrationCode = accessCode
            device.registrationCodeTS = new Date(System.currentTimeMillis())
            device.save(failOnError: true)
        }

        // TODO: sendDeviceRegistrationPageEmail

        return device
    }

    @CompileDynamic
    Device authenticateDevice(String deviceID, String accessCode) {
        // if it matches, update the device to confirmed.
        Device device = Device.findByDeviceId(deviceID)
        if (!device) {
            throw new RuntimeException("device.id.not.found")
            //            throw new DeviceNotFoundException("device.id.not.found")
        }
        // registration must be within an hour of registration email
        if (device.registrationCodeTS.time + (1000 * 60 * 60 * 1) < System.currentTimeMillis())
            throw new RuntimeException("device.registration.expired.")
        //            throw new ConfirmationCodeExpired("device.registration.expired.")
        if (device.registrationCode.compareTo(accessCode) != 0)
            throw new ConfirmationCodeInvalidException("incorrect.accesscode")

        //        device.status = Device.Status.REGISTERED
        //        device.save(true)

        return device
    }
}
