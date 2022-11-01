package com.demirbank.task.paymentTest.authentication;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.demirbank.task.paymentTest.Constants;
import com.demirbank.task.paymentTest.entities.ActiveToken;
import com.demirbank.task.paymentTest.entities.Client;
import com.demirbank.task.paymentTest.repositories.ActiveTokenJpaRepo;
import com.demirbank.task.paymentTest.repositories.ClientJpaRepo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
/*
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;*/
import io.jsonwebtoken.UnsupportedJwtException;

@SuppressWarnings("SpringJavaAutowiringInspection")
public class AuthorizationFilter extends OncePerRequestFilter {

    @Inject
    ClientJpaRepo clientRepository;

    @Inject
    ActiveTokenJpaRepo activeTokenJpaRepo;
    


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filter)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        try {
           
            if (clientRepository == null){
                ServletContext servletContext = request.getServletContext();
                WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
                clientRepository = webApplicationContext.getBean(ClientJpaRepo.class);
            }
            if (activeTokenJpaRepo == null){
                ServletContext servletContext = request.getServletContext();
                WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
                activeTokenJpaRepo = webApplicationContext.getBean(ActiveTokenJpaRepo.class);
            }
            final String token = request.getHeader("Authorization");
            if (isJwtToken(token)) {
                Claims claims = getClaims(token);
                // find a client from DB by client id extracted from token
                Optional<Client> cOptional = clientRepository.findById(Long.parseLong(claims.getSubject()));
                Client client = cOptional.get();
                if(claims.get("authorities") != null && !client.getIsBlocked() && isActiveToken(token)) {
                    setupAuthentication(claims);
                } else {
                    SecurityContextHolder.clearContext();
                }
            } else {
                SecurityContextHolder.clearContext();
            }
            filter.doFilter(request, response);

        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
            return;
        }
        
    }

    private void setupAuthentication(Claims claims){
        List<String>authorities = (List) claims.get("authorities");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), authorities,
        authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private boolean isJwtToken(String token){
        return token != null && token.startsWith(Constants.TOKEN_PREFIX);
    }

    //check if token is in active list (not logged out)
    private boolean isActiveToken(String token){
        token = token.substring(7);
        final List<ActiveToken> activeTokens = activeTokenJpaRepo.findByToken(token);
        return activeTokens.size() > 0;

    }

    private Claims getClaims(String token){
        token = token.substring(7);
        final JwtParser parser = Jwts.parser().setSigningKey(Constants.sekretKey.getBytes());
        final Claims claims = parser.parseClaimsJws(token)
        .getBody();
        return claims;
        
    }

    
    
}
