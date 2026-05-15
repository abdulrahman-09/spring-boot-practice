package com.am9.spring_security_lab.controller;

import com.am9.spring_security_lab.constant.AppConstants;
import com.am9.spring_security_lab.dto.LoginRequestDto;
import com.am9.spring_security_lab.dto.LoginResponseDto;
import com.am9.spring_security_lab.entity.Customer;
import com.am9.spring_security_lab.repository.CustomerRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@RestController
public class AuthenticationController {
    //just experiment no judge (:
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final Environment env;
    private final SecretKey secretKey;

    public AuthenticationController(CustomerRepository customerRepository, Environment env, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.env = env;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;

        String key = env.getProperty(AppConstants.JWT_SECRET_KEY,
                AppConstants.DEFAULT_JWT_SECRET_KEY);
        this.secretKey = Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }



    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Customer customer){
        try {
            String hashPwd = passwordEncoder.encode(customer.getPassword());
            customer.setPassword(hashPwd);
            Customer savedCustomer = customerRepository.save(customer);

            if(savedCustomer.getId()>0) {
                return ResponseEntity.status(HttpStatus.CREATED).
                        body("Given user details are successfully registered");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                        body("User registration failed");
            }
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).
                    body("An exception occurred: " + ex.getMessage());
        }

    }

    @PostMapping("/userlogin")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto){
        Authentication authentication = UsernamePasswordAuthenticationToken.unauthenticated(loginRequestDto.username(), loginRequestDto.password());
        Authentication authenticationResult = authenticationManager.authenticate(authentication);
        if (authenticationResult != null && authenticationResult.isAuthenticated()){
            Long expTime = AppConstants.DEFAULT_JWT_EXP_TIME;
            Date now = new Date();
            String jwt = Jwts.builder().issuer("SecLab")
                    .subject("JWT Token")
                    .claim("username", authentication.getName())
                    .claim("authorities", authentication.getAuthorities()
                            .stream().map(GrantedAuthority::getAuthority)
                            .collect(Collectors.joining(",")))
                    .issuedAt(now)
                    .expiration(new Date(now.getTime() + expTime))
                    .signWith(secretKey)
                    .compact();

            return ResponseEntity.status(HttpStatus.OK).header(AppConstants.JWT_HEADER,jwt)
                    .body(new LoginResponseDto(HttpStatus.OK.getReasonPhrase(), jwt));

        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new LoginResponseDto(HttpStatus.UNAUTHORIZED.getReasonPhrase(), ""));
    }
}
