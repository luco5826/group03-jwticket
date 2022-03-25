package it.polito.wa2.group03.server.service

import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import it.polito.wa2.group03.server.model.TicketPayload
import org.apache.tomcat.util.codec.binary.Base64
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class TicketingServiceStateful(@Value("\${jwt.key}") private val key: String): TicketingServiceStateless(key) {

    private lateinit var processedTickets: MutableList<String>
    private val parser: JwtParser = Jwts.parserBuilder().setSigningKey(Base64.encodeBase64String(key.toByteArray())).build()

    override fun validateTicket(ticket: TicketPayload): ValidationResult {

        val sub = this.getSub(ticket.token)
        /** TODO: this is NOT thread-safe */
        when (processedTickets.contains(sub)) {
            true -> return ValidationResult.DUPLICATE
            false -> processedTickets.add(sub)
        }

        return super.validateTicket(ticket)

    }

    fun getSub(token: String): String {
        val field = "sub"
        return this.parser.parseClaimsJws(token).body.getValue(field).toString()
    }

}