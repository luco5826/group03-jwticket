package it.polito.wa2.group03.server.service

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import it.polito.wa2.group03.server.model.TicketPayload
import org.apache.tomcat.util.codec.binary.Base64
import org.springframework.stereotype.Service

@Service
class TicketingService {

    // TODO: Extract key into environment variable
    private val key = "ebFWkwyCkiYWbmZhoDvOOKSQnRayUzpOfQpfBWLWeshroGkQFULEkxwdRMvbjKYb"
    private val parser: JwtParser =
        Jwts.parserBuilder().setSigningKey(Base64.encodeBase64String(key.toByteArray())).build()

    fun validateTicket(ticket: TicketPayload): ValidationResult {
        if (ticket.zone.isBlank())
            return ValidationResult.NOT_VALID

        try {
            val validityZones = parser.parseClaimsJws(ticket.token).body.getValue("vz").toString()

            if (validityZones.isBlank()) {
                return ValidationResult.NOT_VALID
            }

            if (!validityZones.contains(ticket.zone)) {
                return ValidationResult.UNSUPPORTED_ZONE
            }
        } catch (e: ExpiredJwtException) {
            return ValidationResult.EXPIRED
        } catch (e: Exception) {
            return ValidationResult.NOT_VALID
        }
        return ValidationResult.VALID
    }

}