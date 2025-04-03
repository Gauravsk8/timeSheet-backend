package com.example.timesheet.controller;


import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final Keycloak keycloak;

    public TestController(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    @GetMapping("/keycloak")
    public String testKeycloak() {
        try {
            List<String> realms = keycloak.realms().findAll().stream()
                    .map(RealmRepresentation::getRealm)
                    .collect(Collectors.toList());
            return "Connected to Keycloak. Available realms: " + realms;
        } catch (Exception e) {
            return "Keycloak connection failed: " + e.getMessage();
        }
    }
}