package com.demirbank.task.paymentTest.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demirbank.task.paymentTest.entities.Client;

public interface ClientJpaRepo extends JpaRepository<Client, Long> {
    
}
