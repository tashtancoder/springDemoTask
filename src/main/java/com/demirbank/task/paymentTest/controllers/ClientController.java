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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demirbank.task.paymentTest.Constants;
import com.demirbank.task.paymentTest.HashSha256;
import com.demirbank.task.paymentTest.TokenJwt;
import com.demirbank.task.paymentTest.TokenManager;
import com.demirbank.task.paymentTest.entities.Client;
import com.demirbank.task.paymentTest.entities.Payment;
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
        final Client client = new Client("pass321", "sergei", "ivanov", 8.0);
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
            token = tokenManager.generateJwtToken("userName");
            //token = getJWTToken(client.getId());
            //token = "accessToken";
            client.setToken(token);
            clientRepository.save(client);
            //clientTemp.setToken(token);
            //clientTemp.setId(id);
        }
        TokenJwt tokenJwt = new TokenJwt(token, id, 5);

        return new ResponseEntity<>(tokenJwt, HttpStatus.OK);
    }

    @PostMapping("/clients")
    public ResponseEntity<?> createClient(@RequestBody Client client, @RequestHeader("token") String token){
        System.out.println(token);
        if (client.getAmount() == null){
            client.setAmount(8.0);
        }
        if(client.getCurrency() == null){
            client.setCurrency("USD");
        }
        //client.setPass(HashSha256.getHash(client.getPass()));
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

    @PostMapping("/clients/{id}/payments")
    public ResponseEntity<?> doPayment(@PathVariable Long id){
        Payment payment = new Payment();
        Optional<Client> clientOptional = clientRepository.findById(id);
        Client client = clientOptional.get();
        if (payment.getCost() <= client.getAmount()) {
            client.addPayment(payment);
            paymentRepo.save(payment);
            return new ResponseEntity<>(null, HttpStatus.ACCEPTED);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_ACCEPTABLE);
        }
        
    }

    @GetMapping("/clients/{id}/payments")
    public ResponseEntity<List<Payment>> getPayments(@PathVariable Long id){
        Optional<Client> clientOptional = clientRepository.findById(id);
        Client client = clientOptional.get();
        return new ResponseEntity<List<Payment>>(client.getPayments(), HttpStatus.OK);
    }

    

    private String getJWTToken(Long id){
        String base64EncodedSecret = "cXdlcnR5cGFzc3dvcmQ=";
        byte[] decodedSecret = Base64.getDecoder().decode(base64EncodedSecret);
        String token = "";
        try {
            Claims claims = Jwts.claims().setSubject("" + id);
            JwtBuilder jwt = Jwts.builder().setId(Constants.jwtId)
            .setClaims(claims)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + Constants.tokenExpiredTime * 60 * 1000))
            .signWith(SignatureAlgorithm.ES512, decodedSecret);
            token = jwt.compact();
        } catch (Exception  e) {
            System.out.println(e.toString());
            token = "username";
        }
        return token;

    }

    private String getJWTTokenOld(Long id){
        String token = "";
        try {
            Claims claims = Jwts.claims().setSubject("userName");
            JwtBuilder jwt = Jwts.builder().setId(Constants.jwtId)
            .setClaims(claims)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + Constants.tokenExpiredTime * 60 * 1000))
            .signWith(SignatureAlgorithm.HS512, Constants.sekretKey.getBytes());
            token = jwt.compact();
        } catch (Exception  e) {
            System.out.println(e.toString());
            token = "username";
        }
        return token;

    }





    


        



}
