package com.motorph.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Payslip(
        int employeeId,
        String employeeName,
        LocalDate periodStart,
        LocalDate periodEnd,
        BigDecimal hoursWorked,
        BigDecimal basicPay,
        BigDecimal allowances,
        BigDecimal grossPay,
        BigDecimal sss,
        BigDecimal philhealth,
        BigDecimal pagibig,
        BigDecimal withholdingTax,
        BigDecimal netPay) {
}
