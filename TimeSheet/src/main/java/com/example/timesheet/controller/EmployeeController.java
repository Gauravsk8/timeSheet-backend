package com.example.timesheet.controller;

import com.example.timesheet.config.KeycloakAuthorizationEnforcer;
import com.example.timesheet.models.Employee;
import com.example.timesheet.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final KeycloakAuthorizationEnforcer enforcer;

    @PostMapping("/create")
    public ResponseEntity<?> createEmployee(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody Employee employee,
            @RequestParam String role) {
        try {
            boolean allowed = enforcer.isAuthorized(token, "employee",  "testscope");
            if (!allowed) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied by Keycloak policy");
            }

            String result = employeeService.createEmployee(employee, role);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to create employee: " + e.getMessage());
        }
    }
}
