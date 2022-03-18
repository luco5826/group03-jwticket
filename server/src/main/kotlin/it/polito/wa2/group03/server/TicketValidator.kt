package it.polito.wa2.group03.server

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
    val parser = Jwts.parserBuilder().setSigningKey(Base64.encodeBase64String(key.toByteArray())).build()

    @PostMapping("/validate")
    fun validateTicker(@RequestBody payload: TicketPayload): ResponseEntity<String> {
        try {
            var decodified = parser.parse(payload.token)
        } catch (e: java.lang.Exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not valid")
        }
        return ResponseEntity.status(HttpStatus.OK).body("Valid")
    }
}