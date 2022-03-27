package it.polito.wa2.group03.server.service

import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import it.polito.wa2.group03.server.model.TicketPayload
import org.apache.tomcat.util.codec.binary.Base64
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

@Service
@Primary
class TicketingServiceStateful(@Value("\${jwt.key}") private val key: String) : ITicketingService {

    private var ticketQueue: Queue<String> = ConcurrentLinkedQueue()
    private val parser: JwtParser =
        Jwts.parserBuilder().setSigningKey(Base64.encodeBase64String(key.toByteArray())).build()

    override fun validateTicket(ticket: TicketPayload): ValidationResult {

        val validation = super.validateTicket(ticket)
        if (validation !== ValidationResult.VALID)
            return validation

        // Proceed with further inspections if ticket is valid
        val sub: String?
        try {
            sub = this.getSub(ticket.token)
        } catch (e: Exception) {
            return ValidationResult.NOT_VALID
        }

        if (sub.isNullOrEmpty())
            return ValidationResult.NOT_VALID

        when (sub in ticketQueue) {
            true -> return ValidationResult.DUPLICATE
            false -> ticketQueue.add(sub)
        }
        return ValidationResult.VALID

    }

    override fun getValidityZones(token: String): String {
        val field = "vz"
        return this.parser.parseClaimsJws(token).body.getValue(field).toString()
    }

    fun getSub(token: String): String? {
        val field = "sub"
        return this.parser.parseClaimsJws(token).body.getValue(field)?.toString()
    }

}