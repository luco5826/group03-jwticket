package it.polito.wa2.group03.server

import io.jsonwebtoken.Jwts
import org.apache.tomcat.util.codec.binary.Base64
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Locale


@RestController
class TicketValidator {
    val key = "ebFWkwyCkiYWbmZhoDvOOKSQnRayUzpOfQpfBWLWeshroGkQFULEkxwdRMvbjKYb"
    val parser = Jwts.parserBuilder().setSigningKey(Base64.encodeBase64String(key.toByteArray())).build()

    @PostMapping("/validate")
    fun validateTicker(@RequestBody payload: TicketPayload): ResponseEntity<String> {
        try {
            var decodified = parser.parseClaimsJws(payload.token)
            if(!decodified.body.getValue("vz").toString().contains(payload.zone)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Zone not supported")
            }

            println(LocalDateTime.ofEpochSecond(decodified.body.getValue("exp").toString().toLong(), 0, ZoneOffset.ofHours(1)))
            if(LocalDateTime.ofEpochSecond(decodified.body.getValue("exp").toString().toLong(), 0, ZoneOffset.ofHours(1)) < LocalDateTime.now()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ticket expired")
            }

        } catch (e: java.lang.Exception) {
            println(e.message)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not valid")
        }
        return ResponseEntity.status(HttpStatus.OK).body("Valid")
    }
}