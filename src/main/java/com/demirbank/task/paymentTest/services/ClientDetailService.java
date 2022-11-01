package com.demirbank.task.paymentTest.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.demirbank.task.paymentTest.entities.Client;
import com.demirbank.task.paymentTest.repositories.ClientJpaRepo;

@Component
public class ClientDetailService implements UserDetailsService {
    @Autowired
    private ClientJpaRepo clientRepo;

    public ClientDetailService(ClientJpaRepo clientJpaRepo){
        this.clientRepo = clientJpaRepo;

    }

    @Override
    public UserDetails loadUserByUsername(String idStr) throws UsernameNotFoundException {
        // TODO Auto-generated method stub
        Long id = Long.parseLong(idStr);
        Optional<Client> cOptional = clientRepo.findById(id);
        final Client client = cOptional.get(); 
        if (client == null) {
            throw new UsernameNotFoundException(idStr);
        } 
        UserDetails user = User.withUsername(idStr).password(client.getPass()).authorities("USER").build();
        return user;
    }
    
}
