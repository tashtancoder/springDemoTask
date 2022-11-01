package com.demirbank.task.paymentTest.entities;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import com.demirbank.task.paymentTest.HashSha256;
import com.fasterxml.jackson.annotation.JsonIgnore;

/*import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy; */


@Entity
public class Client {
    @Id
    @GeneratedValue
    @Column(name = "CLIENT_ID")
    private Long id;
    
    //@JsonIgnore
    @Column(name = "PASS")
    private String pass;

    @Column(name = "NAME")
    private String name;

    @Column(name = "SURNAME")
    private String surname;

    @Column(name = "AMOUNT", columnDefinition = "Decimal(10,2)")
    private Double amount = 8.0;

    @Column(name = "CURRENCY")
    private String currency = "USD";
    

    @JsonIgnore
    @Column(name = "ISBLOCKED")
    private Boolean isBlocked = false;

    @JsonIgnore
    @Column(name = "LOGINATTEMPT")
    private Integer loginAttempt = 0;
    

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "CLIENT_ID")
    @OrderBy
    private List<Payment> payments;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "CLIENT_ID")
    private List<ActiveToken> tokens;

    public Client(){

    }

    public Client(String pass, String name, String surname){
        this.pass = HashSha256.getHash(pass);
        this.name = name;
        this.surname = surname;
    }
    public Client(String pass, String name, String surname, Double amount, String currency){
        this.pass = HashSha256.getHash(pass);
        this.name = name;
        this.surname = surname;
        this.amount = amount;
        this.currency = currency;
        this.payments = new ArrayList<Payment>();
        this.isBlocked = false;
    }
    public Client(String pass, String name, String surname, Double amount){
        this.pass = HashSha256.getHash(pass);
        this.name = name;
        this.surname = surname;
        this.amount = amount;
        this.currency = "USD";
        this.payments = new ArrayList<Payment>();
        this.isBlocked = false;
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

    public List<ActiveToken> getTokens(){
        return tokens;
    }
    public void addToken(ActiveToken activeToken){
        this.tokens.add(activeToken);
    }

    public void removeToken(ActiveToken token){
        this.tokens.remove(token);
    }

    public String getCurrency(){
        return this.currency;
    }
    public void setCurrency(String currency){
        this.currency = currency;
    }

    public void setIsBlocked(Boolean isBlocked){
        this.isBlocked = isBlocked;
    }
    public Boolean getIsBlocked(){
        return isBlocked;
    }

    public void setLoginAttempt(Integer loginAttempt){
        this.loginAttempt = loginAttempt;
    }

    public Integer getLoginAttempt(){
        return loginAttempt;
    }
   
    public String toString(){
        return "id: " + id + " name: " + name + " surname: " + surname + " amount: " + amount;
    }

}
