package demo

class ConfirmationCodeInvalidException extends RuntimeException {
    ConfirmationCodeInvalidException() {
        super()
    }
    ConfirmationCodeInvalidException(String message) {
        super(message)
    }
}
