package com.demirbank.task.paymentTest.controllers;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

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
import com.demirbank.task.paymentTest.entities.Client;
import com.demirbank.task.paymentTest.entities.Payment;
import com.demirbank.task.paymentTest.entities.TokenJwt;
import com.demirbank.task.paymentTest.repositories.ClientJpaRepo;
import com.demirbank.task.paymentTest.repositories.PaymentJpaRepo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@RestController
public class ClientController {

    @Autowired
    private TokenManager tokenManager;

    @Inject
    private ClientJpaRepo clientRepository;

    @Inject
    private PaymentJpaRepo paymentRepo;

    public ClientController(ClientJpaRepo clientRepository, PaymentJpaRepo paymentRepo){
        this.clientRepository = clientRepository;
        this.paymentRepo = paymentRepo;
        final Client client = new Client("pass321", "sergei", "ivanov");
        clientRepository.save(client);
    }
    
    @GetMapping("/clients")
    public ResponseEntity<List<Client>> getClients(){
        return new ResponseEntity<List<Client>>(clientRepository.findAll(), HttpStatus.OK);
    }

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
                token = token.substring(8);
                client.setToken(token);
                client.setLoginAttempt(0);
                clientRepository.save(client); 
                // show token, client_id and token expired time in minutes
                TokenJwt tokenJwt = new TokenJwt(token, id, Constants.tokenExpiredTime); 
                return new ResponseEntity<>(tokenJwt, HttpStatus.OK);
            }
            
        } else {
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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String token){
        token = token.substring(7);
        Client client = clientRepository.findByToken(token);
        client.setToken(null);
        clientRepository.save(client);
        return new ResponseEntity<String>("client's session successfully invalidated", HttpStatus.valueOf(204));

    }

    @PostMapping("/clients")
    public ResponseEntity<?> createClient(@RequestBody Client client){
        if (client.getAmount() == null){
            client.setAmount(8.0);
        }
        if(client.getCurrency() == null){
            client.setCurrency("USD");
        }
        clientRepository.save(client);
        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }

    @GetMapping("/clients/{id}")
    public ResponseEntity<Client> getClient(@PathVariable Long id){
        Optional<Client> clientOptional = clientRepository.findById(id);
        Client client = clientOptional.get();
        return new ResponseEntity<>(client, HttpStatus.OK);
    }
    

    @DeleteMapping("/clients/{id}")
    public ResponseEntity<?> deleteClient(@PathVariable Long id){
        clientRepository.deleteById(id);
        return new ResponseEntity<>(null, HttpStatus.MOVED_PERMANENTLY);    
    }

    @PostMapping("/clients/{id}/payments") // post payment by client Id
    public ResponseEntity<?> createPayment(@PathVariable Long id){
        Optional<Client> clientOptional = clientRepository.findById(id);
        Client client = clientOptional.get();
        return doPayment(client);
        
    }

    @PostMapping("/payments") // post payment by client token
    public ResponseEntity<?> createPayment(@RequestHeader("Authorization") String token){
        token = token.substring(7);
        Client client = clientRepository.findByToken(token);
        return doPayment(client);
    }

    @GetMapping("/clients/{id}/payments")
    public ResponseEntity<List<Payment>> getPayments(@PathVariable Long id){
        Optional<Client> clientOptional = clientRepository.findById(id);
        Client client = clientOptional.get();
        return new ResponseEntity<List<Payment>>(client.getPayments(), HttpStatus.OK);
    }

    @GetMapping("/payments")
    public ResponseEntity<List<Payment>> getPayments(@RequestHeader("Authorization") String token){
        token = token.substring(7);
        Client client = clientRepository.findByToken(token);
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

}
