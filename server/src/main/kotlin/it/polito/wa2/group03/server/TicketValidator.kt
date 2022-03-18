package it.polito.wa2.group03.server

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import org.apache.tomcat.util.codec.binary.Base64
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController




@RestController
class TicketValidator {
    val key = "ebFWkwyCkiYWbmZhoDvOOKSQnRayUzpOfQpfBWLWeshroGkQFULEkxwdRMvbjKYb"
    val parser: JwtParser = Jwts.parserBuilder().setSigningKey(Base64.encodeBase64String(key.toByteArray())).build()

    @PostMapping("/validate")
    fun validateTicker(@RequestBody payload: TicketPayload): ResponseEntity<String> {
        try {
            val decodified = parser.parseClaimsJws(payload.token)
            if (!decodified.body.getValue("vz").toString().contains(payload.zone)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Zone not supported")
            }

        }catch (e: ExpiredJwtException) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ticket expired")
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not valid")
        }
        return ResponseEntity.status(HttpStatus.OK).body("Valid")
    }
}