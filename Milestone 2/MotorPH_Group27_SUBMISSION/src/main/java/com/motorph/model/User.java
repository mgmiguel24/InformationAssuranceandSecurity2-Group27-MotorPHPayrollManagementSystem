package com.motorph.model;

public class User {
    private final int userId;
    private final String username;
    private final String passwordHash;
    private final String role;
    private final Integer employeeId;

    public User(int userId, String username, String passwordHash, String role, Integer employeeId) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.employeeId = employeeId;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public Integer getEmployeeId() { return employeeId; }
}
