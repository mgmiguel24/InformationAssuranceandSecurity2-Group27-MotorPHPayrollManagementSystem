package com.motorph.service;

import com.motorph.dao.EmployeeDAO;
import com.motorph.dao.PayrollDAO;
import com.motorph.model.Employee;
import com.motorph.model.Payslip;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Logic tier for payroll. Deductions use a simplified monthly model:
 * SSS 5% of basic (capped at 1,750), PhilHealth 2.5% employee share of basic
 * (basis 10,000 to 100,000), Pag-IBIG 2% capped at 200, and TRAIN-law
 * monthly withholding tax brackets on (basic pay minus contributions).
 */
public class PayrollCalculator {
    private static final BigDecimal MAX_HOURS = new BigDecimal("400");

    private final EmployeeDAO employeeDAO;
    private final PayrollDAO payrollDAO;

    public PayrollCalculator(EmployeeDAO employeeDAO, PayrollDAO payrollDAO) {
        this.employeeDAO = employeeDAO;
        this.payrollDAO = payrollDAO;
    }

    public Payslip compute(String employeeIdRaw, String startRaw, String endRaw, String hoursRaw) {
        int employeeId = parseInt(employeeIdRaw, "Employee ID");
        LocalDate start = parseDate(startRaw, "Period start");
        LocalDate end = parseDate(endRaw, "Period end");
        if (end.isBefore(start)) {
            throw new ValidationException("Period end must be on or after period start.");
        }
        BigDecimal hours;
        try {
            hours = new BigDecimal(hoursRaw.trim());
        } catch (Exception e) {
            throw new ValidationException("Hours worked must be a number.");
        }
        if (hours.signum() < 0 || hours.compareTo(MAX_HOURS) > 0) {
            throw new ValidationException("Hours worked must be between 0 and 400.");
        }

        Employee emp = employeeDAO.findById(employeeId)
                .orElseThrow(() -> new ValidationException("Employee not found."));

        BigDecimal basicPay = money(emp.getHourlyRate().multiply(hours));
        BigDecimal allowances = money(emp.getTotalAllowances());
        BigDecimal gross = basicPay.add(allowances);

        BigDecimal salaryBase = emp.getBasicSalary();
        BigDecimal sss = money(salaryBase.multiply(new BigDecimal("0.05")).min(new BigDecimal("1750")));
        BigDecimal phBase = salaryBase.max(new BigDecimal("10000")).min(new BigDecimal("100000"));
        BigDecimal philhealth = money(phBase.multiply(new BigDecimal("0.025")));
        BigDecimal pagibig = money(salaryBase.multiply(new BigDecimal("0.02")).min(new BigDecimal("200")));

        BigDecimal taxable = basicPay.subtract(sss).subtract(philhealth).subtract(pagibig).max(BigDecimal.ZERO);
        BigDecimal tax = money(withholdingTax(taxable));

        BigDecimal net = gross.subtract(sss).subtract(philhealth).subtract(pagibig).subtract(tax);

        Payslip slip = new Payslip(emp.getEmployeeId(), emp.getFullName(), start, end, hours,
                basicPay, allowances, gross, sss, philhealth, pagibig, tax, money(net));
        payrollDAO.save(slip);
        return slip;
    }

    public List<Payslip> history(String employeeIdRaw) {
        return payrollDAO.findByEmployee(parseInt(employeeIdRaw, "Employee ID"));
    }

    /** TRAIN law monthly withholding tax table (2023 onward). */
    static BigDecimal withholdingTax(BigDecimal t) {
        if (t.compareTo(new BigDecimal("20833")) <= 0) return BigDecimal.ZERO;
        if (t.compareTo(new BigDecimal("33333")) < 0)
            return t.subtract(new BigDecimal("20833")).multiply(new BigDecimal("0.15"));
        if (t.compareTo(new BigDecimal("66667")) < 0)
            return new BigDecimal("1875").add(t.subtract(new BigDecimal("33333")).multiply(new BigDecimal("0.20")));
        if (t.compareTo(new BigDecimal("166667")) < 0)
            return new BigDecimal("8541.80").add(t.subtract(new BigDecimal("66667")).multiply(new BigDecimal("0.25")));
        if (t.compareTo(new BigDecimal("666667")) < 0)
            return new BigDecimal("33541.80").add(t.subtract(new BigDecimal("166667")).multiply(new BigDecimal("0.30")));
        return new BigDecimal("183541.80").add(t.subtract(new BigDecimal("666667")).multiply(new BigDecimal("0.35")));
    }

    private static BigDecimal money(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    private static int parseInt(String raw, String field) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception e) {
            throw new ValidationException(field + " must be a whole number.");
        }
    }

    private static LocalDate parseDate(String raw, String field) {
        try {
            return LocalDate.parse(raw.trim());
        } catch (NullPointerException | DateTimeParseException e) {
            throw new ValidationException(field + " must be a date in YYYY-MM-DD format.");
        }
    }
}
