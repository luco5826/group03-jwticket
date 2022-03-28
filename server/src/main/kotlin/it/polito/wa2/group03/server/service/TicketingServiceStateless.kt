package it.polito.wa2.group03.server.service

import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import org.apache.tomcat.util.codec.binary.Base64
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class TicketingServiceStateless(@Value("\${jwt.key}") private val key: String) : ITicketingService {

    private val parser: JwtParser =
        Jwts.parserBuilder().setSigningKey(Base64.encodeBase64String(key.toByteArray())).build()

    override fun getValidityZones(token: String): String {
        val field = "vz"
        return this.parser.parseClaimsJws(token).body.getValue(field).toString()
    }
}