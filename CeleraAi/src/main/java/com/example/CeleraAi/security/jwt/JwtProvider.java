package com.example.CeleraAi.security.jwt;


import com.example.CeleraAi.security.errorhandling.JwtTokenException;

import com.example.CeleraAi.users.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;


import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Log
@Service
public class JwtProvider {

    public static final String TOKEN_TYPE = "JWT";
    public static final String TOKEN_HEADER = "Authorization";

    public static final String TOKEN_PREFIX = "Bearer ";




    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.duration}")
    private int jfwLifeInDays;

    private JwtParser jwtParser;

    private SecretKey secretKey;

    @PostConstruct
    public void init(){
        secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        jwtParser = Jwts.parser().verifyWith(secretKey).build();
    }

    public String generateToken(Authentication authentication){
        User user = (User) authentication.getPrincipal();
        return generateToken(user);

    }

    public String generateToken(User user){
        Date tokenExpirationTimeDate =
                Date.from(
                        LocalDateTime
                                .now()
                                .plusDays(jfwLifeInDays)
                                .atZone(ZoneId.systemDefault())
                                .toInstant()
                );

        return Jwts.builder()
                .header()
                .type(TOKEN_TYPE)
                .and()
                .subject(user.getId().toString())
                .issuedAt(tokenExpirationTimeDate)
                .signWith(secretKey)
                .compact();

    }

    public UUID getUserIdFromJwtToken(String token){

        return UUID.fromString(
                jwtParser.parseSignedClaims(token).getPayload().getSubject()
        );

    }

    public boolean validateToken(String token){

        try {
            jwtParser.parse(token);
            return true;
        }catch (SignatureException | MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException ex){
            log.info("Error con el token: " + ex.getMessage());
            throw new JwtTokenException(ex.getMessage());
        }

    }

}
