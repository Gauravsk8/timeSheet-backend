package com.example.timesheet.models;

import com.example.timesheet.Repository.EmployeeRepository;
import com.example.timesheet.Repository.Projection.EmployeeProjection;
import com.example.timesheet.enums.EmployeePrivilege;
import com.example.timesheet.framework.utils.SpringContext;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import java.sql.Date;
import java.util.*;

@Table
@Entity
@Getter
@Setter
@NoArgsConstructor
@Audited
public class Employee extends BaseEntity implements UserDetails {
    @ManyToMany(mappedBy = "employees")
    protected Set<Role> roles = new LinkedHashSet<>();


    @Column(nullable = false, unique = true, length = 50)
    private String employeeId;

    @Column(columnDefinition = "text")
    private String firstName;

    @Column(columnDefinition = "text")
    private String lastName;

    private String email;

    private String phone;

    @JsonIgnore
    private String password;

    private Boolean enabled;


    @ManyToMany(mappedBy = "assignedEmployees")
    private Set<Project> projects = new LinkedHashSet<>();


    @Column(name = "keycloak_id", unique = true)
    private String keycloakId;


    //One-to-Many relationship with Project (one employee can manage multiple projects)
    @OneToMany(mappedBy = "manager")
    private Set<Project> managedProjects = new LinkedHashSet<>();

    private Integer failedAttempt = 0;

    private Date lastPasswordChanged;

    private boolean mobileVerified = false;

    private boolean emailVerified = false;

    private boolean firstTimeLogin = false;

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return "Employee{employeeId='" + employeeId + "', name='" + Objects.toString(firstName, "N/A") + " " + Objects.toString(lastName, "N/A") + "', email='" + Objects.toString(email, "N/A") + "'}";
    }


    @Transient
    public boolean hasRole(String role) {
        return this.roles.stream().anyMatch(o -> o.getName().equalsIgnoreCase(role));
    }

    @Transient
    public boolean getCurrentUser() {
        EmployeeRepository employeeRepository = SpringContext.getBean(EmployeeRepository.class);
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return false;
        }


        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();
        EmployeeProjection loggedInUser = employeeRepository.findByEmailAndDeletedIsFalse(username, EmployeeProjection.class);

        return loggedInUser != null && loggedInUser.getEmail().equals(this.getEmail());

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Role role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        }
        return authorities;
    }

    @Transient
    public String getName() {
        return firstName + " " + lastName;
    }

    @Transient
    public boolean hasPrivilege(EmployeePrivilege requestedPrivilege) {
        for (Role role : this.roles) {
            for (Privilege privilege : role.getPrivileges()) {
                if (privilege.getName().equals(requestedPrivilege.name())) {
                    return true;
                }
            }
        }
        return false;
    }
}


