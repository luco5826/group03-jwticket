package it.polito.wa2.group03.server.service

import io.jsonwebtoken.ExpiredJwtException
import it.polito.wa2.group03.server.model.TicketPayload

interface ITicketingService {

    /**
     * Validates a ticket.
     * The default implementation validates zone and exp, any specialization
     * of this method should call this method first and proceed with further
     * inspections whenever the ticket results valid from this function
     */
    fun validateTicket(ticket: TicketPayload): ValidationResult {
        return try {

            val validityZones = getValidityZones(ticket.token)
            val ticketZone = ticket.zone

            this.validateZone(validityZones, ticketZone)

        } catch (e: ExpiredJwtException) {
            ValidationResult.EXPIRED
        } catch (e: Exception) {
            ValidationResult.NOT_VALID
        }
    }

    /**
     * zone must always be validated. should return:
     * - NOT_VALID = valid zones, ticket zone or both are empty
     * - UNSUPPORTED_ZONE = ticket zone is note part of valid zones
     * - VALID = ticket zone is valid
     */
    fun validateZone(validityZones: String, ticketZone: String): ValidationResult {
        return if (validityZones.isBlank() || ticketZone.isBlank()) {
            ValidationResult.NOT_VALID
        } else if (!validityZones.contains(ticketZone)) {
            ValidationResult.UNSUPPORTED_ZONE
        } else {
            ValidationResult.VALID
        }
    }

    /**
     * given a token, gets the validity zones.
     */
    fun getValidityZones(token: String): String

}