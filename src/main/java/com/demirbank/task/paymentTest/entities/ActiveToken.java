package com.demirbank.task.paymentTest.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class ActiveToken {

    @Id
    @GeneratedValue
    @Column(name = "TOKEN_ID")
    private Long id;

    @Column(name = "TOKEN")
    private String token;

    public ActiveToken(){

    }

    public ActiveToken(String token){
        this.token = token;
    }

    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }

    public String getToken(){
        return token;
    }

    public void setToken(String token){
        this.token = token;
    }
}
