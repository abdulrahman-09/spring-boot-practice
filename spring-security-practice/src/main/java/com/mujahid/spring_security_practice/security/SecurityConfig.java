package com.mujahid.spring_security_practice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.net.http.HttpRequest;
import java.util.function.Consumer;

@Configuration
public class SecurityConfig {

    @Bean
    public InMemoryUserDetailsManager userDetailsManager(){
        UserDetails am9 = User.builder()
                .username("am9")
                .password("{noop}123")
                .roles("EMPLOYEE","ADMIN","MANAGER")
                .build();

        UserDetails jow = User.builder()
                .username("jow")
                .password("{noop}123")
                .roles("EMPLOYEE","MANAGER")
                .build();

        UserDetails m = User.builder()
                .username("m")
                .password("{noop}123")
                .roles("EMPLOYEE")
                .build();

        return new InMemoryUserDetailsManager(am9, jow, m);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeHttpRequests(configurer -> configurer
                        .requestMatchers("/").hasRole("EMPLOYEE")
                        .requestMatchers("/leaders/**").hasRole("MANAGER")
                        .requestMatchers("/systems/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(
                        form -> form
                                .loginPage("/showMyLoginPage")
                                .loginProcessingUrl("/authenticateTheUser")
                                .permitAll()
                )
                .logout(logout -> logout.permitAll());
        return httpSecurity.build();
    }
}
