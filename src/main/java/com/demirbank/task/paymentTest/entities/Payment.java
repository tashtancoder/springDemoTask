package com.demirbank.task.paymentTest.entities;

import java.time.LocalDate;
/*
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id; */
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Payment {

    @Id
    @GeneratedValue
    @Column(name = "PAYMENT_ID")
    private Long id;

    @Column(name = "COST", columnDefinition = "Decimal(10,2) default '1.1'")
    private Double cost;

    @Column(name = "DATE")
    private Date date;
    public Payment(){
        cost = 1.1;
        date = new Date();

    }

    public Payment(Double cost){
        this.cost = cost;
        date = new Date();
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
    public Date getDate(){
        return date;
    }
    public void setDate(Date date){
        this.date = date;
    }
        

}
