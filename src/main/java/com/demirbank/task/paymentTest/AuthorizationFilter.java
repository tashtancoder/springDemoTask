package com.demirbank.task.paymentTest;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthorizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filter)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        final String token = request.getHeader("token");
        if (isJwtToken(token)) {
            Claims claims = getClaims(token);
            if(claims.get("authorities") != null) setupAuthentication(claims);
            else SecurityContextHolder.clearContext();
        } else {
            SecurityContextHolder.clearContext();
        }
        filter.doFilter(request, response);
    }

    private void setupAuthentication(Claims claims){
        List<String>authorities = (List) claims.get("authorities");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), 
        authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private boolean isJwtToken(String token){
        return token != null && token.startsWith("Bearer");
    }

    private Claims getClaims(String token){
        return Jwts.parser().setSigningKey(Constants.sekretKey.getBytes())
        .parseClaimsJws(token).getBody();
    }
    
}
