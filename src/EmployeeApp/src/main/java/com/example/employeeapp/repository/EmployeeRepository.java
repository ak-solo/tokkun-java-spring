package com.example.employeeapp.repository;

import com.example.employeeapp.model.Employee;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class EmployeeRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public EmployeeRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Employee> findAll() {
        return jdbcTemplate.query(
            "SELECT id, name FROM employees ORDER BY id",
            Map.of(),
            new BeanPropertyRowMapper<>(Employee.class)
        );
    }
}
