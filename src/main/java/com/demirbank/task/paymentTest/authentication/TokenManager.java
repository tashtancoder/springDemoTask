package com.demirbank.task.paymentTest.authentication;

import java.io.Serializable;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.vote.ConsensusBased;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import com.demirbank.task.paymentTest.Constants;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
@Component
public class TokenManager implements Serializable{
    private static final long serialVersionUID = 7008375124389347049L; 
    
     public String generateJwtToken(String id) { 
        List<GrantedAuthority> grantedAuthorities = AuthorityUtils
				.commaSeparatedStringToAuthorityList("ROLE_USER");
        String token = Jwts
				.builder()
				.setId(Constants.jwtId)
        .setSubject(id)
				.claim("authorities",
						grantedAuthorities.stream()
								.map(GrantedAuthority::getAuthority)
								.collect(Collectors.toList()))        
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + Constants.tokenExpiredTime * 60 * 1000))
				.signWith(SignatureAlgorithm.HS512,
						Constants.sekretKey.getBytes()).compact();

        return Constants.TOKEN_PREFIX + " " + token;
     } 

    
}
