package it.polito.wa2.group03.server.service

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import it.polito.wa2.group03.server.model.TicketPayload
import org.apache.tomcat.util.codec.binary.Base64
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class TicketingServiceStateless(@Value("\${jwt.key}") private val key: String) : TicketingService {

    override fun validateTicket(ticket: TicketPayload): ValidationResult {

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

    override fun validateZone(validityZones: String, ticketZone: String): ValidationResult {

        return if (validityZones.isBlank() || ticketZone.isBlank()) {
            ValidationResult.NOT_VALID
        } else if (!validityZones.contains(ticketZone)) {
            ValidationResult.UNSUPPORTED_ZONE
        } else {
            ValidationResult.VALID
        }

    }

    override fun getValidityZones(token: String): String {
        val field = "vz"
        val parser = Jwts.parserBuilder().setSigningKey(Base64.encodeBase64String(key.toByteArray())).build()
        return parser.parseClaimsJws(token).body.getValue(field).toString()
    }

}