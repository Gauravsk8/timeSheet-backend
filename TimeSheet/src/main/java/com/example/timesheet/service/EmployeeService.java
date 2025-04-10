package com.example.timesheet.service;

import com.example.timesheet.Repository.EmployeeRepository;
import com.example.timesheet.Repository.RoleRepository;
import com.example.timesheet.constants.errorCode;
import com.example.timesheet.constants.errorMessage;
import com.example.timesheet.exceptions.*;
import com.example.timesheet.models.Employee;
import com.example.timesheet.models.Role;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;

import static com.example.timesheet.constants.errorMessage.*;


@Service
@RequiredArgsConstructor
public class EmployeeService {
    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final KeycloakService keycloakService;

    @Transactional
    public String createEmployee(Employee employee, String roleName) {
        try {
            // Check if employee already exists
            if (employeeRepository.existsByEmailAndDeletedIsFalse(employee.getEmail())) {
                String message = String.format(EMPLOYEE_ALREADY_EXISTS, employee.getEmail());
                throw new TimeSheetException(errorCode.CONFLICT_ERROR, message);
            }

            // Find role in database
            Role role = roleRepository.findByNameIgnoreCase(roleName)
                    .orElseThrow(() -> new TimeSheetException(errorCode.NOT_FOUND_ERROR, String.format(errorMessage.ROLE_NOT_FOUND, roleName)));

            // Create user in Keycloak (password will be handled by Keycloak)
            String keycloakUserId = keycloakService.createUserWithRole(employee, role.getName());

            // Save to database
            employee.setTenantId("one");
            employee.setKeycloakId(keycloakUserId);
            employee.setRoles(Collections.singleton(role));
            employee.setEnabled(true);
            // Don't store the plain password in your database
            //employee.setPassword(null); // or set to some placeholder

            Employee savedEmployee = employeeRepository.save(employee);

            if (savedEmployee.getId() == null) {
                throw new TimeSheetException(errorCode.INTERNAL_SERVER_ERROR, EMPLOYEE_SAVE_FAILED);
            }

            return String.format(EMPLOYEE_CREATION_SUCCESS, savedEmployee.getId());

        } catch (TimeSheetException e) {
        throw e;
    } catch (Exception e) {
            throw new TimeSheetException(errorCode.INTERNAL_SERVER_ERROR,
                    EMPLOYEE_CREATION_FAILED_LOG + ": " + e.getMessage(), e);    }

}
}