package com.example.timesheet.Repository;

import com.example.timesheet.models.Role;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RoleRepository extends BaseRepository<Role, Long>, PagingAndSortingRepository<Role, Long>, QuerydslPredicateExecutor<Role> {
}
