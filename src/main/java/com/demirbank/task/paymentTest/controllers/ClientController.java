package com.demirbank.task.paymentTest.controllers;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties.Jwt;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demirbank.task.paymentTest.Constants;
import com.demirbank.task.paymentTest.HashSha256;
import com.demirbank.task.paymentTest.authentication.TokenManager;
import com.demirbank.task.paymentTest.entities.ActiveToken;
import com.demirbank.task.paymentTest.entities.Client;
import com.demirbank.task.paymentTest.entities.Payment;
import com.demirbank.task.paymentTest.entities.TokenJwt;
import com.demirbank.task.paymentTest.repositories.ActiveTokenJpaRepo;
import com.demirbank.task.paymentTest.repositories.ClientJpaRepo;
import com.demirbank.task.paymentTest.repositories.PaymentJpaRepo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;


// APIs 
@RestController
public class ClientController {

    @Autowired
    private TokenManager tokenManager;

    @Inject
    private ClientJpaRepo clientRepository;

    @Inject
    private PaymentJpaRepo paymentRepo;
    
    @Inject
    private ActiveTokenJpaRepo tokenRepository;

    public ClientController(ClientJpaRepo clientRepository, PaymentJpaRepo paymentRepo){
        this.clientRepository = clientRepository;
        this.paymentRepo = paymentRepo;
        // generate default client with pass [pass321p] id 1 and 8.0USD
        final Client client = new Client("pass321", "sergei", "ivanov");
        clientRepository.save(client); // save default value to DB
    }
    

    // Get all clients with details
    @GetMapping("/clients") 
    public ResponseEntity<List<Client>> getClients(){
        return new ResponseEntity<List<Client>>(clientRepository.findAll(), HttpStatus.OK);
    }

    // Login service with client id as "id" and password as "pass"
    @PostMapping("/login") 
    public ResponseEntity<?> login(@RequestParam Long id, @RequestParam String pass){
        Client client = clientRepository.findByIdAndPass(id, HashSha256.getHash(pass));
        String token = "";
        if (client != null){
            if (client.getIsBlocked()) { // if the Client is blocked then only show message with LOCKED status
                //throw new LockedException("The Client is blocked");
                return new ResponseEntity<>("The Client is blocked", HttpStatus.LOCKED);
            } else {
                token = tokenManager.generateJwtToken("" + client.getId()); // generate JWT token with client id
                final ActiveToken activeToken = new ActiveToken(token);
                client.addToken(activeToken); // add new token to active token list (white list)
                tokenRepository.save(activeToken); // save the token to DB
                client.setLoginAttempt(0); // reset counter of incorrect login attempt to 0
                clientRepository.save(client); // update the client
                removeExpiredTokens(client); // Remove expired tokens from the list
                // show token, client_id and token expired time in minutes
                TokenJwt tokenJwt = new TokenJwt(token, id, Constants.tokenExpiredTime); 
                return new ResponseEntity<>(tokenJwt, HttpStatus.OK);
            }
            
        } else { // check for brute force to clients passwords with clients id's 
            Optional<Client> cOptional = clientRepository.findById(id);
            if (!cOptional.isEmpty()) {
                client = cOptional.get();
                client.setLoginAttempt(client.getLoginAttempt() + 1); // count incorrect login attempts
                if (client.getLoginAttempt() > Constants.MAX_LOGIN_ATTEMPT) { // block the client if number of login attempt exceeds MAX_LOGIN_ATTEMPT
                    client.setIsBlocked(true);
                }
                clientRepository.save(client); // save the client
            }
            //throw new UsernameNotFoundException("Invalid Client_id and/or password");
            return new ResponseEntity<>("Invalid Client_id and/or password", HttpStatus.NOT_FOUND);

        }
        
    }


    // Logout the client, invalidate the session by removing the token from active token list (white list).
    // Notice that endpoint is 'loggout' not 'logout'.
    @PostMapping("/loggout") 
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token){
        token = token.substring(7);
        final List<ActiveToken> activeTokens = tokenRepository.findByToken(token);
        //ActiveToken activeToken = activeTokens.get(0);
        tokenRepository.deleteAll(activeTokens);
        return new ResponseEntity<>("client's session successfully invalidated", HttpStatus.OK);

    }

    // Create new client
    @PostMapping("/client")
    public ResponseEntity<?> createClient(@RequestBody Client client){
        if (client.getAmount() == null){
            client.setAmount(8.0); // if amount is not defined then initialize with default value 8.0
        }
        if(client.getCurrency() == null){
            client.setCurrency("USD"); // if currency is not defined then initialize with default value "USD"
        }
        clientRepository.save(client);
        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }

    // Get the specific client's details by client id
    @GetMapping("/clients/{id}") 
    public ResponseEntity<Client> getClient(@PathVariable Long id){
        Optional<Client> clientOptional = clientRepository.findById(id);
        Client client = clientOptional.get();
        return new ResponseEntity<>(client, HttpStatus.OK);
    }
    
    // Get the client details by token
    @GetMapping("/client") 
    public ResponseEntity<Client> getClient(@RequestHeader("Authorization") String token){
        token = token.substring(7);
        Client client = findClientByToken(token);
        return new ResponseEntity<>(client, HttpStatus.OK);
    }

    // Delete specific client by its client id
    @DeleteMapping("/clients/{id}") 
    public ResponseEntity<?> deleteClient(@PathVariable Long id){
        clientRepository.deleteById(id);
        return new ResponseEntity<>("the client is successfully deleted", HttpStatus.MOVED_PERMANENTLY);    
    }

    // Delete the client by token
    @DeleteMapping("/client") 
    public ResponseEntity<?> deleteClient(@RequestHeader("Authorization") String token){
        token = token.substring(7);
        Client client = findClientByToken(token);
        clientRepository.deleteById(client.getId());
        return new ResponseEntity<>(null, HttpStatus.MOVED_PERMANENTLY);    
    }

    // Create a payment for a spesific client by client id
    @PostMapping("/clients/{id}/payments") 
    public ResponseEntity<?> createPayment(@PathVariable Long id){
        Optional<Client> clientOptional = clientRepository.findById(id);
        Client client = clientOptional.get();
        if (client == null) {
            return new ResponseEntity<>("Client not found", HttpStatus.NOT_FOUND);
        } else {
            return doPayment(client);
        }
    }

    // Create a payment by the client token
    @PostMapping("/payment") 
    public ResponseEntity<?> createPayment(@RequestHeader("Authorization") String token){
        token = token.substring(7);
        Client client = findClientByToken(token);
        return doPayment(client);
    }

    // Get payment list of specific client by client id
    @GetMapping("/clients/{id}/payments")
    public ResponseEntity<List<Payment>> getPayments(@PathVariable Long id){
        Optional<Client> clientOptional = clientRepository.findById(id);
        Client client = clientOptional.get();
        return new ResponseEntity<List<Payment>>(client.getPayments(), HttpStatus.OK);
    }

    // Get payment list of the client by client token
    @GetMapping("/payments")
    public ResponseEntity<List<Payment>> getPayments(@RequestHeader("Authorization") String token){
        token = token.substring(7);
        Client client = findClientByToken(token);
        return new ResponseEntity<List<Payment>>(client.getPayments(), HttpStatus.OK);
    }


    private ResponseEntity<?> doPayment(Client client){
        Payment payment = new Payment();
        if (payment.getCost() <= client.getAmount()) {
            client.addPayment(payment);
            paymentRepo.save(payment);
            return new ResponseEntity<>("Payment accepted", HttpStatus.ACCEPTED);
        } else {
            return new ResponseEntity<>("Insufficient amount", HttpStatus.NOT_ACCEPTABLE);
        }
    }

    private Client findClientByToken(String token){
        final JwtParser parser = Jwts.parser().setSigningKey(Constants.sekretKey.getBytes());
        final Claims claims = parser.parseClaimsJws(token).getBody();
        String idInString = claims.getSubject();
        Long id = Long.parseLong(idInString);
        Optional<Client> cOptional = clientRepository.findById(id);
        if (!cOptional.isEmpty()){
            return cOptional.get();
        } else {
            return null;
        }

    }

    private void removeExpiredTokens(Client client){
        List<ActiveToken> expiredTokens = new ArrayList<ActiveToken>();
        for (ActiveToken activeToken : client.getTokens()) {
            try {
                final JwtParser parser = Jwts.parser().setSigningKey(Constants.sekretKey.getBytes());
                final Jws<Claims> claims = parser.parseClaimsJws(activeToken.getToken());
            } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException e){
                expiredTokens.add(activeToken);
            }
        }
        for(ActiveToken expiredToken: expiredTokens){
            client.getTokens().removeIf(token -> token.getId() == expiredToken.getId());
        }
        clientRepository.save(client);
        tokenRepository.deleteAll(expiredTokens);
    }

}
