package com.demirbank.task.paymentTest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String getHelloMsg(){
        return "Welcome to Demo Spring App";
    }
}
