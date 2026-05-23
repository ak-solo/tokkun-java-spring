package com.example.employeeapp.controller;

import com.example.employeeapp.model.Employee;
import com.example.employeeapp.repository.EmployeeRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/employees")
public class EmployeeController {
    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    public String index(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Integer deptId,
            @RequestParam(defaultValue = "hire_date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {
        List<Employee> employees = employeeRepository.findAll(keyword, deptId, sortBy, sortDir);
        model.addAttribute("employees", employees);
        model.addAttribute("keyword", keyword);
        model.addAttribute("deptId", deptId);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        return "employee/index";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("employee", new Employee());
        return "employee/create";
    }

    @PostMapping
    public String create(@ModelAttribute Employee employee) {
        int id = employeeRepository.save(employee);
        return "redirect:/employees/" + id;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable int id, Model model) {
        Employee employee = employeeRepository.findById(id);
        model.addAttribute("employee", employee);
        return "employee/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        Employee employee = employeeRepository.findById(id);
        model.addAttribute("employee", employee);
        return "employee/edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(
            @PathVariable int id,
            @Valid @ModelAttribute Employee employee,
            BindingResult result) {
        if (result.hasErrors()) {
            return "employee/edit";
        }
        employeeRepository.update(employee);
        return "redirect:/employees/" + id;
    }

    @GetMapping("/{id}/delete")
    public String deleteConfirm(@PathVariable int id, Model model) {
        Employee employee = employeeRepository.findById(id);
        model.addAttribute("employee", employee);
        return "employee/delete";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable int id) {
        employeeRepository.delete(id);
        return "redirect:/employees";
    }
}
