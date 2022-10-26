package com.demirbank.task.paymentTest.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demirbank.task.paymentTest.entities.Payment;

public interface PaymentJpaRepo extends JpaRepository <Payment, Long>{
    
}
