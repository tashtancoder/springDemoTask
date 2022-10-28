package com.demirbank.task.paymentTest;

import java.io.Serializable;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
@Component
public class TokenManager implements Serializable{
    private static final long serialVersionUID = 7008375124389347049L; 
    public static final long TOKEN_VALIDITY = 10 * 60 * 60; 
    
    @Value("${secret}") 
    private String jwtSecret; 

    public String generateJwtToken(String id) { 
        Map<String, Object> claims = new HashMap<>(); 
        return Jwts.builder().setClaims(claims).setSubject(id) 
           .setIssuedAt(new Date(System.currentTimeMillis())) 
           .setExpiration(new Date(System.currentTimeMillis() + TOKEN_VALIDITY * 1000)) 
           .signWith(SignatureAlgorithm.HS512, "cXdlcnR5cGFzc3dvcmQ=").compact(); 
     } 

    
}
