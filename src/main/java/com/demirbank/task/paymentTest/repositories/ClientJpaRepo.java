package com.demirbank.task.paymentTest.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.demirbank.task.paymentTest.entities.Client;

@Component
public interface ClientJpaRepo extends JpaRepository<Client, Long> {
    public Client findByIdAndPass(Long id, String pass);
}
