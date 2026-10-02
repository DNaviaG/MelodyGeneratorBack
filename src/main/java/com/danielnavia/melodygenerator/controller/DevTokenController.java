package com.danielnavia.melodygenerator.controller;

import com.danielnavia.melodygenerator.service.TokenService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DevTokenController {

    private final TokenService jwtService;

    public DevTokenController(TokenService tokenService) {
        this.jwtService = tokenService;
    }


    //Controller de prueba  para swagger/postman
    @GetMapping("/api/dev/token/{id}/{mail}")
    public String token(@PathVariable Integer id, @PathVariable String mail) {
        return jwtService.generateToken(id, mail);
    }
}
