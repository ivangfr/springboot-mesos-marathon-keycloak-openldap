package com.mycompany.simpleservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api")
public class ApplicationController {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/public")
    public String getPublicString() {
        return "It is public.\n";
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/private")
    public String getPrivateString(Principal principal) {
        return String.format("%s, it is private.\n", principal.getName());
    }

}