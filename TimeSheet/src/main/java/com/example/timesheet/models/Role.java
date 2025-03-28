package com.example.timesheet.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Table
@Entity
@Getter
@Setter
@NoArgsConstructor
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
public class Role extends  BaseEntity {

    private String name;

    @ManyToMany(fetch = FetchType.EAGER,cascade = {CascadeType.PERSIST})
    @JoinTable(
            name = "role_privileges",
            joinColumns = {@JoinColumn(name = "role_id")},
            inverseJoinColumns = {@JoinColumn(name = "privilege_id")}
    )
    @OrderBy("sortOrder")
    private Set<Privilege> privileges=new LinkedHashSet<>();
}
