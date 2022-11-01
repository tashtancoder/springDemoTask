package com.demirbank.task.paymentTest.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenJwt {
    private String token;

    @JsonProperty("client_id")
    private Long id;

    @JsonProperty("expiredTime_in_minutes")
    private int expiredTime;
    public TokenJwt(String token, Long id, int expiredTime){
        this.token = token;
        this.id = id;
        this.expiredTime = expiredTime;
    }

    public void setToken(String token){
        this.token = token;
    }

    public String getToken(){
        return token;
    }

    public void setId(Long id){
        this.id = id;
    } 
    public Long getId(){
        return id;
    }
    public void setExpiredTime(int expiredTime){
        this.expiredTime = expiredTime;
    }
    public int getExpiredTime(){
        return expiredTime;
    }
    
}
