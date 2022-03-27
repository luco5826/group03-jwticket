package it.polito.wa2.group03.server.service

import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import it.polito.wa2.group03.server.model.TicketPayload
import org.apache.tomcat.util.codec.binary.Base64
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

@Service
class TicketingServiceStateful(@Value("\${jwt.key}") private val key: String) : TicketingServiceStateless(key) {

    private var ticketQueue: Queue<String> = ConcurrentLinkedQueue()
    private val parser: JwtParser =
        Jwts.parserBuilder().setSigningKey(Base64.encodeBase64String(key.toByteArray())).build()

    override fun validateTicket(ticket: TicketPayload): ValidationResult {

        try {

            val sub = this.getSub(ticket.token)

            when (sub in ticketQueue){
                true -> return ValidationResult.DUPLICATE
                false -> ticketQueue.add(sub)
            }

            return super.validateTicket(ticket)

        } catch (e: Exception) {
            /**
             * in case the sub field is not available the ticket
             * should be validated as we cannot check its uniqueness.
             * this also catches wrong signatures.
             */
            return ValidationResult.NOT_VALID
        }

    }

    fun getSub(token: String): String {
        val field = "sub"
        return this.parser.parseClaimsJws(token).body.getValue(field).toString()
    }

}