package it.polito.wa2.group03.server.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import it.polito.wa2.group03.server.model.TicketPayload
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import java.nio.charset.StandardCharsets
import java.security.Key
import java.time.LocalDateTime
import java.time.ZoneOffset

@SpringBootTest
class TicketingServiceStatefulTests(@Value("\${jwt.key}") private val keyString: String) {

    private val key: Key = Keys.hmacShaKeyFor(keyString.toByteArray(StandardCharsets.UTF_8))

    lateinit var ticketingServiceStateful: TicketingServiceStateful

    @BeforeEach
    fun initializeService() {
        ticketingServiceStateful = TicketingServiceStateful(keyString)
    }

    @Test
    fun acceptValidJWT() {
        val token = Jwts
            .builder()
            .setClaims(
                mapOf(
                    "sub" to "subject-ticket",
                    "vz" to "1234",
                    "exp" to LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC)
                )
            )
            .signWith(key)
            .compact()
        val zone = "4"

        Assertions.assertEquals(
            ValidationResult.VALID,
            ticketingServiceStateful.validateTicket(TicketPayload(zone, token))
        )
    }

    @Test
    fun rejectExpiredJWT() {
        val token = Jwts
            .builder()
            .setClaims(
                mapOf(
                    "sub" to "subject-ticket",
                    "vz" to "1234",
                    "exp" to LocalDateTime.now().minusDays(1).toEpochSecond(ZoneOffset.UTC)
                )
            )
            .signWith(key)
            .compact()
        val zone = "4"

        Assertions.assertEquals(
            ValidationResult.EXPIRED,
            ticketingServiceStateful.validateTicket(TicketPayload(zone, token))
        )
    }

    @Test
    fun rejectZoneNotSupportedJWT() {
        val token = Jwts
            .builder()
            .setClaims(
                mapOf(
                    "sub" to "subject-ticket",
                    "vz" to "1234",
                    "exp" to LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC)
                )
            )
            .signWith(key)
            .compact()

        /** zone 10 is NOT valid */
        val zone = "10"

        Assertions.assertEquals(
            ValidationResult.UNSUPPORTED_ZONE,
            ticketingServiceStateful.validateTicket(TicketPayload(zone, token))
        )
    }

    @Test
    fun rejectNotValidJWT() {
        val token = Jwts
            .builder()
            .setClaims(
                mapOf(
                    "sub" to "subject-ticket",
                    "vz" to "1234",
                    "exp" to LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC)
                )
            )
            .signWith(key)
            .compact()
        val zone = "4"

        /** mess up the signature by appending wrong characters at the end of the token */
        Assertions.assertEquals(
            ValidationResult.NOT_VALID,
            ticketingServiceStateful.validateTicket(TicketPayload(zone, token + "ERROR"))
        )
    }

    @Test
    fun rejectEmptyJWT() {

        val token = ""
        val zone = ""

        Assertions.assertEquals(
            ValidationResult.NOT_VALID,
            ticketingServiceStateful.validateTicket(TicketPayload(zone, token))
        )
    }

    @Test
    fun emptyZoneJWT() {
        val token = Jwts
            .builder()
            .setClaims(
                mapOf(
                    "sub" to "subject-ticket",
                    "vz" to "123",
                    "exp" to LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC)
                )
            )
            .signWith(key)
            .compact()
        val zone = ""

        Assertions.assertEquals(
            ValidationResult.NOT_VALID,
            ticketingServiceStateful.validateTicket(TicketPayload(zone, token))
        )
    }

    @Test
    fun emptyValidityZonesJWT() {
        val token = Jwts
            .builder()
            .setClaims(
                mapOf(
                    "sub" to "subject-ticket",
                    "vz" to "",
                    "exp" to LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC)
                )
            )
            .signWith(key)
            .compact()
        val zone = "2"

        Assertions.assertEquals(
            ValidationResult.NOT_VALID,
            ticketingServiceStateful.validateTicket(TicketPayload(zone, token))
        )
    }

    @Test
    fun missingSubField() {
        val token = Jwts
            .builder()
            .setClaims(
                mapOf(
                    "vz" to "123",
                    "exp" to LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC)
                )
            )
            .signWith(key)
            .compact()
        val zone = "2"

        Assertions.assertEquals(
            ValidationResult.NOT_VALID,
            ticketingServiceStateful.validateTicket(TicketPayload(zone, token))
        )
    }

    @Test
    fun duplicateSub() {
        val builder = Jwts
            .builder()
            .setClaims(
                mapOf(
                    "sub" to "subject-ticket",
                    "vz" to "123",
                    "exp" to LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC)
                )
            )
            .signWith(key)
        val token1 = builder.compact()
        val token2 = builder.compact()
        val zone = "2"

        Assertions.assertEquals(
            ValidationResult.VALID,
            ticketingServiceStateful.validateTicket(TicketPayload(zone, token1))
        )
        Assertions.assertEquals(
            ValidationResult.DUPLICATE,
            ticketingServiceStateful.validateTicket(TicketPayload(zone, token2))
        )
    }
}