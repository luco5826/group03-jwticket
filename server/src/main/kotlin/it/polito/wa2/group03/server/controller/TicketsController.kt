package it.polito.wa2.group03.server.controller

import it.polito.wa2.group03.server.model.TicketPayload
import it.polito.wa2.group03.server.service.TicketingService
import it.polito.wa2.group03.server.service.ValidationResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TicketsController {

    @Autowired
    lateinit var ticketingService: TicketingService

    @PostMapping("/validate")
    fun validateTicket(@RequestBody payload: TicketPayload): ResponseEntity<String> {
        return when (ticketingService.validateTicket(payload)) {
            ValidationResult.VALID -> ResponseEntity.status(HttpStatus.OK).body("Valid")
            ValidationResult.UNSUPPORTED_ZONE -> ResponseEntity.status(HttpStatus.FORBIDDEN).body("Zone not supported")
            ValidationResult.EXPIRED -> ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ticket expired")
            ValidationResult.NOT_VALID -> ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not valid")
        }
    }
}