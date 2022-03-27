package it.polito.wa2.group03.server.controller

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import it.polito.wa2.group03.server.model.TicketPayload
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.annotation.DirtiesContext.ClassMode
import java.nio.charset.StandardCharsets
import java.security.Key
import java.time.LocalDateTime
import java.time.ZoneOffset

// This attribute recreates the WebServer before each test, this is a simple-yet-ugly
// workaround to re-initialize the already checked ticket list before each test
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TicketsControllerTests(@Value("\${jwt.key}") private val keyString: String) {

    @LocalServerPort
    var port: Int = 0

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    private val key: Key = Keys.hmacShaKeyFor(keyString.toByteArray(StandardCharsets.UTF_8))

    @Test
    fun `valid ticket`() {
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
        val zone = "2"

        val baseUrl = "http://localhost:$port"
        val request = HttpEntity(TicketPayload(zone, token))
        val response = restTemplate.postForEntity<String>(
            "$baseUrl/validate",
            request
        )
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun `empty zone`() {
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
        val zone = ""

        val baseUrl = "http://localhost:$port"
        val request = HttpEntity(TicketPayload(zone, token))
        val response = restTemplate.postForEntity<String>(
            "$baseUrl/validate",
            request
        )
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    @Test
    fun `unsupported zone`() {
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
        val zone = "5"

        val baseUrl = "http://localhost:$port"
        val request = HttpEntity(TicketPayload(zone, token))
        val response = restTemplate.postForEntity<String>(
            "$baseUrl/validate",
            request
        )
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    @Test
    fun `wrong jwt signature`() {
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
        val zone = "2"

        val baseUrl = "http://localhost:$port"
        val request = HttpEntity(TicketPayload(zone, token + "ERROR"))
        val response = restTemplate.postForEntity<String>(
            "$baseUrl/validate",
            request
        )
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    @Test
    fun `empty validity zones`() {
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

        val baseUrl = "http://localhost:$port"
        val request = HttpEntity(TicketPayload(zone, token))
        val response = restTemplate.postForEntity<String>(
            "$baseUrl/validate",
            request
        )
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    @Test
    fun `expired jwt`() {
        val token = Jwts
            .builder()
            .setClaims(
                mapOf(
                    "sub" to "subject-ticket",
                    "vz" to "123",
                    "exp" to LocalDateTime.now().minusDays(1).toEpochSecond(ZoneOffset.UTC)
                )
            )
            .signWith(key)
            .compact()
        val zone = "2"

        val baseUrl = "http://localhost:$port"
        val request = HttpEntity(TicketPayload(zone, token))
        val response = restTemplate.postForEntity<String>(
            "$baseUrl/validate",
            request
        )
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    @Test
    fun `missing sub field`() {
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

        val baseUrl = "http://localhost:$port"
        val request = HttpEntity(TicketPayload(zone, token))
        val response = restTemplate.postForEntity<String>(
            "$baseUrl/validate",
            request
        )
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    @Test
    fun `duplicate sub field`() {
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
        val zone = "2"
        val baseUrl = "http://localhost:$port"

        val token1 = builder.compact()
        val token2 = builder.compact()

        val response1 = restTemplate.postForEntity<String>(
            "$baseUrl/validate",
            HttpEntity(TicketPayload(zone, token1))
        )
        val response2 = restTemplate.postForEntity<String>(
            "$baseUrl/validate",
            HttpEntity(TicketPayload(zone, token2))
        )
        Assertions.assertEquals(HttpStatus.OK, response1.statusCode)
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response2.statusCode)
    }
}
