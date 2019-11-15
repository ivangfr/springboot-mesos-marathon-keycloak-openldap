package com.mycompany.simpleservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api")
public class ApplicationController {

    @GetMapping("/public")
    public String getPublicString() {
        return "It is public.\n";
    }

    @GetMapping("/private")
    public String getPrivateString(Principal principal) {
        return principal.getName() + ", it is private.\n";
    }

}