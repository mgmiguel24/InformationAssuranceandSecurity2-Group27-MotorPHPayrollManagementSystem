package com.motorph.model;

import java.math.BigDecimal;

public class Employee extends Person {
    private final int employeeId;
    private final String position;
    private final String department;
    private final BigDecimal basicSalary;
    private final BigDecimal hourlyRate;
    private final BigDecimal riceSubsidy;
    private final BigDecimal phoneAllowance;
    private final BigDecimal clothingAllowance;

    public Employee(int employeeId, String firstName, String lastName, String position, String department,
                    BigDecimal basicSalary, BigDecimal hourlyRate, BigDecimal riceSubsidy,
                    BigDecimal phoneAllowance, BigDecimal clothingAllowance) {
        super(firstName, lastName);
        this.employeeId = employeeId;
        this.position = position;
        this.department = department;
        this.basicSalary = basicSalary;
        this.hourlyRate = hourlyRate;
        this.riceSubsidy = riceSubsidy;
        this.phoneAllowance = phoneAllowance;
        this.clothingAllowance = clothingAllowance;
    }

    @Override
    public String describe() {
        return getFullName() + " (" + position + ", " + department + ")";
    }

    public int getEmployeeId() { return employeeId; }
    public String getPosition() { return position; }
    public String getDepartment() { return department; }
    public BigDecimal getBasicSalary() { return basicSalary; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public BigDecimal getRiceSubsidy() { return riceSubsidy; }
    public BigDecimal getPhoneAllowance() { return phoneAllowance; }
    public BigDecimal getClothingAllowance() { return clothingAllowance; }
    public BigDecimal getTotalAllowances() { return riceSubsidy.add(phoneAllowance).add(clothingAllowance); }
}
