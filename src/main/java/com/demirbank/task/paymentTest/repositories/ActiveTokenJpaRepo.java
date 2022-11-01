package com.demirbank.task.paymentTest.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demirbank.task.paymentTest.entities.ActiveToken;

public interface ActiveTokenJpaRepo extends JpaRepository<ActiveToken, Long> {
    public List<ActiveToken> findByToken(String token);
    
}
