package com.demirbank.task.paymentTest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class WebSecurityAdapter {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        /*http.formLogin().loginPage("/login").permitAll()
        .and().authorizeRequests()
        .anyRequest().authenticated();*/
        //http.authorizeRequests().anyRequest().authenticated();
        /* .and().formLogin()
        .loginPage("/login").permitAll();*/
        /*http
            .authorizeHttpRequests((authz) -> authz
                .anyRequest().authenticated()
            )
            .httpBasic(withDefaults());*/
        http.authorizeRequests().anyRequest().authenticated()
        .and().formLogin().loginPage("/login").permitAll()
        .and().logout().permitAll()
        .and().httpBasic();
        return http.build();
    }
    /*
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return (web) -> web.ignoring().requestMatchers(HttpMethod.POST, "/login");
    }*/

   
}
