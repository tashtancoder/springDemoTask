package com.demirbank.task.paymentTest.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.demirbank.task.paymentTest.entities.ActiveToken;

public interface ActiveTokenJpaRepo extends JpaRepository<ActiveToken, Long> {
    public List<ActiveToken> findByToken(String token);
    
}
