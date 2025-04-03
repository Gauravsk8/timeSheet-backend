package com.example.timesheet.service;

import com.example.timesheet.models.Employee;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeycloakService {
    private static final Logger log = LoggerFactory.getLogger(KeycloakService.class);
    private final Keycloak keycloakAdmin;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    public String createUserWithRole(Employee employee, String roleName) {
        Response response = null;
        try {
            // 1. Verify admin connection
            verifyAdminConnection();

            RealmResource realmResource = keycloakAdmin.realm(realm);

            // 2. Check for existing user
            checkExistingUser(employee.getEmail(), realmResource);

            // 3. Create user representation with updated credential format
            UserRepresentation user = createUserRepresentation(employee);

            // 4. Create user in Keycloak with enhanced error handling
            UsersResource usersResource = realmResource.users();
            response = usersResource.create(user);
            handleCreateUserResponse(response);

            // 5. Get created user ID
            String userId = extractUserIdFromResponse(response);

            // 6. Assign role
            assignClientRole(userId, roleName, realmResource);

            return userId;
        } catch (Exception e) {
            log.error("Error in createUserWithRole", e);
            throw new RuntimeException("Keycloak operation failed: " + e.getMessage(), e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private void verifyAdminConnection() {
        try {
            keycloakAdmin.realms().findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to access Keycloak as admin. Check admin credentials.", e);
        }
    }

    private void checkExistingUser(String email, RealmResource realmResource) {
        List<UserRepresentation> existingUsers = realmResource.users()
                .search(email, true); // exact match
        if (!existingUsers.isEmpty()) {
            throw new RuntimeException("User with email " + email + " already exists in Keycloak");
        }
    }

    private UserRepresentation createUserRepresentation(Employee employee) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(employee.getEmail());
        user.setFirstName(employee.getFirstName());
        user.setLastName(employee.getLastName());
        user.setEmail(employee.getEmail());
        user.setEnabled(true);
        user.setEmailVerified(false);

        // Add any required attributes
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("source", List.of("timesheet-app"));
        user.setAttributes(attributes);

        // Use the newer credential format to avoid deprecation warnings
        user.singleAttribute(CredentialRepresentation.PASSWORD, employee.getPassword());

        return user;
    }

    private void handleCreateUserResponse(Response response) {
        if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
            try {
                String errorBody = response.readEntity(String.class);
                log.error("Keycloak create user failed. Status: {}, Headers: {}, Body: {}",
                        response.getStatus(),
                        response.getHeaders(),
                        errorBody);

                // Try to parse JSON error for more details
                try {
                    JsonNode errorNode = new ObjectMapper().readTree(errorBody);
                    String errorDetail = errorNode.path("error_description").asText();
                    throw new RuntimeException("Keycloak error: " + errorDetail);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create user in Keycloak. Status: " +
                            response.getStatus() + ", Response: " + errorBody);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to read error response from Keycloak", e);
            }
        }
    }

    private String extractUserIdFromResponse(Response response) {
        String location = response.getLocation().toString();
        return location.substring(location.lastIndexOf('/') + 1);
    }

    private void assignClientRole(String userId, String roleName, RealmResource realmResource) {
        try {
            // Get client
            ClientRepresentation client = realmResource.clients()
                    .findByClientId(clientId)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Client not found in Keycloak: " + clientId));

            // Get role
            RoleRepresentation role = realmResource.clients()
                    .get(client.getId())
                    .roles()
                    .get(roleName)
                    .toRepresentation();

            // Assign role
            realmResource.users()
                    .get(userId)
                    .roles()
                    .clientLevel(client.getId())
                    .add(Collections.singletonList(role));
        } catch (Exception e) {
            log.error("Error assigning role to user", e);
            throw new RuntimeException("Failed to assign role: " + e.getMessage(), e);
        }
    }

    // Additional helper methods can be added here
    public boolean isUserExists(String email) {
        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            List<UserRepresentation> users = realmResource.users().search(email, true);
            return !users.isEmpty();
        } catch (Exception e) {
            log.error("Error checking if user exists", e);
            return false;
        }
    }

    public void updateUserPassword(String userId, String newPassword) {
        try {
            RealmResource realmResource = keycloakAdmin.realm(realm);
            UserResource userResource = realmResource.users().get(userId);

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);
            credential.setTemporary(false);

            userResource.resetPassword(credential);
        } catch (Exception e) {
            log.error("Error updating user password", e);
            throw new RuntimeException("Failed to update user password", e);
        }
    }
}