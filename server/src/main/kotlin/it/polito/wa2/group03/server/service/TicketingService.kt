package it.polito.wa2.group03.server.service

import it.polito.wa2.group03.server.model.TicketPayload

interface TicketingService {

    /**
     * function that validates a ticket.
     * must always validate zone and exp, could be
     * stateless or stateful by checking for duplicate
     * via sub field of jwt.
     */
    fun validateTicket(ticket: TicketPayload): ValidationResult

    /**
     * zone must always be validated. should return:
     * - NOT_VALID = valid zones, ticket zone or both are empty
     * - UNSUPPORTED_ZONE = ticket zone is note part of valid zones
     * - VALID = ticket zone is valid
     * if possible reuse definition from TicketingServiceStateless.
     */
    fun validateZone(validityZones: String, ticketZone: String): ValidationResult

}