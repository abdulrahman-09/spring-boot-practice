package com.am9.Oauth_lab.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SecureController {

    @GetMapping("/secure")
    public String secure(){
        return "secure.html";
    }
}
