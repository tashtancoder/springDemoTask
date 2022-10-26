package com.demirbank.task.paymentTest.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Payment {

    @Id
    @GeneratedValue
    @Column(name = "PAYMENT_ID")
    private Long id;

    @Column(name = "COST", columnDefinition = "Decimal(10,2) default '1.1'")
    private Double cost;

    @Column(name = "DATE")
    private LocalDate date;
    public Payment(){
        cost = 1.1;
        date = LocalDate.now();

    }

    public Payment(Double cost){
        this.cost = cost;
        date = LocalDate.now();
    }

    public Long getId(){
        return id;
    }
    public void setId(Long id){
        this.id = id;
    }
    public Double getCost(){
        return cost;
    }
    public void setCost(Double cost){
        this.cost = cost;
    }
    public LocalDate getDate(){
        return date;
    }
    public void setDate(LocalDate date){
        this.date = date;
    }
        

}
