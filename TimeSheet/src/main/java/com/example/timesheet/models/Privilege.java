package com.example.timesheet.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
@Table
@Entity
@Getter
@Setter
@NoArgsConstructor
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
public class Privilege extends BaseEntity{

    private  String name;
    private int sortOrder;
    @ManyToMany(mappedBy = "privileges")
    private Set<Role> role=new LinkedHashSet<>();
}
