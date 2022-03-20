package it.polito.wa2.group03.server.service

enum class ValidationResult {
    VALID,            // The ticket is not expired and valid for the selected station
    EXPIRED,          // The ticket has expired
    UNSUPPORTED_ZONE, // The ticket does not support the selected zone
    NOT_VALID         // The ticket is not valid (unknown reason)
}