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
            token = tokenManager.generateJwtToken("" + client.getId());
            token = token.substring(8);
            client.setToken(token);
            clientRepository.save(client);
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
        //Optional<Client> clientOptional = clientRepository.findByToken(token.substring(7));
        //Client client = clientOptional.get();
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
            return new ResponseEntity<>(null, HttpStatus.ACCEPTED);
        } else {
            return new ResponseEntity<>(null, HttpStatus.NOT_ACCEPTABLE);
        }
    }

}
