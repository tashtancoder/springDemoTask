package com.demirbank.task.paymentTest.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demirbank.task.paymentTest.entities.Client;

public interface ClientJpaRepo extends JpaRepository<Client, Long> {
    public Client findByIdAndPass(Long id, String pass);
    public Client findByToken(String token);
}
