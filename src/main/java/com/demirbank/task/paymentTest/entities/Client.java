package com.demirbank.task.paymentTest.entities;
import java.util.ArrayList;
import java.util.List;

import com.demirbank.task.paymentTest.HashSha256;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;


@Entity
public class Client {
    @Id
    @GeneratedValue
    @Column(name = "CLIENT_ID")
    private Long id;
    
    @JsonIgnore
    @Column(name = "PASS")
    private String pass;

    @Column(name = "NAME")
    private String name;

    @Column(name = "SURNAME")
    private String surname;

    @Column(name = "AMOUNT", columnDefinition = "Decimal(10,2) default '8'")
    private Double amount;

    @Column(name = "CURRENCY", columnDefinition = "varchar(10) default 'USD'")
    private String currency;
    
    @Column(name = "TOKEN")
    private String token;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "CLIENT_ID")
    @OrderBy
    private List<Payment> payments;
    public Client(){

    }
    public Client(String pass, String name, String surname, Double amount, String currency){
        this.pass = HashSha256.getHash(pass);
        this.name = name;
        this.surname = surname;
        this.amount = amount;
        this.currency = currency;
        this.payments = new ArrayList<Payment>();
        this.token = "";
    }
    public Client(String pass, String name, String surname, Double amount){
        this.pass = HashSha256.getHash(pass);
        this.name = name;
        this.surname = surname;
        this.amount = amount;
        this.currency = "USD";
        this.payments = new ArrayList<Payment>();
        this.token = "";
    }


    public Long getId(){
        return this.id;
    }
    public void setId(Long id){
        this.id = id;
    }

    public String getName(){
        return this.name;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getSurname(){
        return this.surname;
    }
    public void setSurname(String surname){
        this.surname = surname;
    }

    public void setPass(String pass){
        this.pass = HashSha256.getHash(pass);
    }

    public String getPass(){
        return this.pass;
    }

    public Double getAmount(){
        return this.amount;
    }

    public void setAmount(Double amount){
        this.amount = amount;
    }
    public List<Payment> getPayments(){
        return this.payments;
    }
    public void addPayment(Payment payment){
        this.payments.add(payment);
        this.amount = this.amount - payment.getCost();
    }

    public String getCurrency(){
        return this.currency;
    }
    public void setCurrency(String currency){
        this.currency = currency;
    }

    public void setToken(String token){
        this.token = token;
    }

    public String getToken(){
        return token;
    }
   
    public String toString(){
        return "id: " + id + " name: " + name + " surname: " + surname + " amount: " + amount;
    }

}
