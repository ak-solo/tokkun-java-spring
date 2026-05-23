package com.example.employeeapp.repository;

import com.example.employeeapp.model.Employee;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
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
            "SELECT id, name, dept_id, salary, hire_date, manager_id FROM employees ORDER BY hire_date DESC",
            Map.of(),
            new BeanPropertyRowMapper<>(Employee.class)
        );
    }

    public int save(Employee employee) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("name",     employee.getName())
            .addValue("deptId",   employee.getDeptId())
            .addValue("salary",   employee.getSalary())
            .addValue("hireDate", employee.getHireDate());
        jdbcTemplate.update(
            "INSERT INTO employees (name, dept_id, salary, hire_date)" +
            " VALUES (:name, :deptId, :salary, :hireDate)",
            params,
            keyHolder,
            new String[]{"id"}
        );
        return keyHolder.getKey().intValue();
    }

    public void update(Employee employee) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("name",     employee.getName())
            .addValue("deptId",   employee.getDeptId())
            .addValue("salary",   employee.getSalary())
            .addValue("hireDate", employee.getHireDate())
            .addValue("id",       employee.getId());
        jdbcTemplate.update(
            "UPDATE employees" +
            " SET name = :name, dept_id = :deptId, salary = :salary, hire_date = :hireDate" +
            " WHERE id = :id",
            params
        );
    }

    public Employee findById(int id) {
        return jdbcTemplate.queryForObject(
            "SELECT e.*, d.name AS dept_name" +
            " FROM employees e" +
            " LEFT JOIN departments d ON e.dept_id = d.id" +
            " WHERE e.id = :id",
            Map.of("id", id),
            new BeanPropertyRowMapper<>(Employee.class)
        );
    }
}
