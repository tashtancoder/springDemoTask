package com.demirbank.task.paymentTest.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import com.demirbank.task.paymentTest.repositories.ClientJpaRepo;

@Component
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    @Autowired
    ClientJpaRepo clientRepo;
    
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        String clientName = event.getAuthentication().getName();
        System.out.println(clientName);
    }
    
}
