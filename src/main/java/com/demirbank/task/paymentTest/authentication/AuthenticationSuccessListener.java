package com.demirbank.task.paymentTest.authentication;

import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent>{

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        // TODO Auto-generated method stub
        String name = event.getAuthentication().getName();
        
    }
    
}
