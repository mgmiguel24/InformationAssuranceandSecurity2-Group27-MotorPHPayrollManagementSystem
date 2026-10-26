package com.motorph.service;

import com.motorph.dao.EmployeeDAO;
import com.motorph.model.Employee;

import java.util.List;

/** Logic tier: validates input before it reaches the DAO. */
public class EmployeeManager {
    private final EmployeeDAO employeeDAO;

    public EmployeeManager(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    public List<Employee> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return employeeDAO.findAll();
        }
        if (keyword.length() > 100) {
            throw new ValidationException("Search keyword is too long (max 100 characters).");
        }
        return employeeDAO.search(keyword.trim());
    }
}
