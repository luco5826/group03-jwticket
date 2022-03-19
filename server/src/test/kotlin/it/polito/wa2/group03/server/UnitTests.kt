package it.polito.wa2.group03.server

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

@SpringBootTest
class UnitTests() {

    @Autowired
    lateinit var ticketValidator: TicketValidator

    val notSupported = ResponseEntity.status(HttpStatus.FORBIDDEN).body("Zone not supported")
    val expired = ResponseEntity.status(HttpStatus.FORBIDDEN).body("Ticket expired")
    val notValid = ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not valid")
    val valid = ResponseEntity.status(HttpStatus.OK).body("Valid")

    @Test
    fun acceptValidJWT() {

        /**
         * HEADER
         * { "alg": "HS256", "typ": "JWT" }
         * PAYLOAD
         * { "vz": "1234", "exp": 1679487463 }
         * date is 22-03-2023, so valid
         */
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ2eiI6IjEyMzQiLCJleHAiOjE2Nzk0ODc0NjN9.uv0YqGjf5u48ogeX__kdRmlyyPZJvZSctu25PFpeSPA"
        val zone = "4"

        val ticket = TicketPayload(zone, token)
        Assertions.assertEquals(valid, ticketValidator.validateTicket(ticket))

    }

    @Test
    fun rejectExpiredJWT() {

        /**
         * HEADER
         * { "alg": "HS256", "typ": "JWT" }
         * PAYLOAD
         * { "vz": "1234", "exp": 1616415463 }
         * date is 22-03-2021, so NOT valid
         */
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ2eiI6IjEyMzQiLCJleHAiOjE2MTY0MTU0NjN9.15M66HbDvruoNNd2cpuuen21gb1nr6EVUHur18Odp-c"
        /** zone 4 is valid */
        val zone = "4"

        val ticket = TicketPayload(zone, token)
        Assertions.assertEquals(expired, ticketValidator.validateTicket(ticket))

    }

    @Test
    fun rejectZoneNotSupportedJWT() {

        /**
         * HEADER
         * { "alg": "HS256", "typ": "JWT" }
         * PAYLOAD
         * { "vz": "1234", "exp": 1679487463 }
         * date is 22-03-2023, so valid
         */
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ2eiI6IjEyMzQiLCJleHAiOjE2Nzk0ODc0NjN9.uv0YqGjf5u48ogeX__kdRmlyyPZJvZSctu25PFpeSPA"
        /** zone 10 is NOT valid */
        val zone = "10"

        val ticket = TicketPayload(zone, token)
        Assertions.assertEquals(notSupported, ticketValidator.validateTicket(ticket))

    }

    @Test
    fun rejectNotValidJWT() {

        /**
         * HEADER
         * { "alg": "HS256", "typ": "JWT" }
         * PAYLOAD
         * { "vz": "1234", "exp": 1679487463 }
         * date is 22-03-2023, so valid
         */
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ2eiI6IjEyMzQiLCJleHAiOjE2Nzk0ODc0NjN9.uv0YqGjf5u48ogeX__kdRmlyyPZJvZSctu25PFpeSPA"
        /** zone 4 is valid */
        val zone = "4"

        /** mess up the signature by appending at the end of the token */
        val ticket = TicketPayload(zone, token + "ERROR")
        Assertions.assertEquals(notValid, ticketValidator.validateTicket(ticket))

    }

    @Test
    fun rejectEmptyJWT() {

        val ticket = TicketPayload("", "")
        Assertions.assertEquals(notValid, ticketValidator.validateTicket(ticket))

    }

    @Test
    fun emptyZonesJWT() {

        /**
         * HEADER
         * { "alg": "HS256", "typ": "JWT" }
         * PAYLOAD
         * { "vz": "", "exp": 1679487463 }
         * date is 22-03-2023, so valid
         */
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ2eiI6IiIsImV4cCI6MTY3OTQ4NzQ2M30.cAHuq9frWsMFpScwCLVxYoAz9un9m0Q64mhuwtIvzHw"
        val zone = ""

        val ticket = TicketPayload(zone, token)
        Assertions.assertEquals(valid, ticketValidator.validateTicket(ticket))

        /**
         * NOTE:
         * this test should not be passed, we should consider not valid on empty vz or empty zone
         */

    }

}