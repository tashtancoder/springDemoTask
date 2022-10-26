package com.demirbank.task.paymentTest.controllers;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.demirbank.task.paymentTest.entities.Client;
import com.demirbank.task.paymentTest.entities.Payment;
import com.demirbank.task.paymentTest.repositories.ClientJpaRepo;
import com.demirbank.task.paymentTest.repositories.PaymentJpaRepo;

@RestController
public class ClientController {

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

    @PostMapping("/clients")
    public ResponseEntity<?> createClient(@RequestBody Client client){
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

    


        



}
