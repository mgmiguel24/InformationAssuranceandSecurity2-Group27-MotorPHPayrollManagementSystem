package com.motorph.dao;

import com.motorph.config.DatabaseConfig;
import com.motorph.model.Payslip;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** CONTROL #1: parameterized INSERT and SELECT for payroll records. */
public class PayrollDAO {

    private static final String INSERT =
            "INSERT INTO payroll_records (employee_id, period_start, period_end, hours_worked, gross_pay, "
          + "sss, philhealth, pagibig, withholding_tax, net_pay) VALUES (?,?,?,?,?,?,?,?,?,?)";

    private static final String FIND_BY_EMPLOYEE =
            "SELECT p.*, CONCAT(e.first_name,' ',e.last_name) AS name, e.hourly_rate, "
          + "(e.rice_subsidy + e.phone_allowance + e.clothing_allowance) AS allowances "
          + "FROM payroll_records p JOIN employees e ON e.employee_id = p.employee_id "
          + "WHERE p.employee_id = ? ORDER BY p.period_start DESC";

    private final DatabaseConfig db = DatabaseConfig.getInstance();

    public void save(Payslip p) {
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT)) {
            pstmt.setInt(1, p.employeeId());
            pstmt.setDate(2, Date.valueOf(p.periodStart()));
            pstmt.setDate(3, Date.valueOf(p.periodEnd()));
            pstmt.setBigDecimal(4, p.hoursWorked());
            pstmt.setBigDecimal(5, p.grossPay());
            pstmt.setBigDecimal(6, p.sss());
            pstmt.setBigDecimal(7, p.philhealth());
            pstmt.setBigDecimal(8, p.pagibig());
            pstmt.setBigDecimal(9, p.withholdingTax());
            pstmt.setBigDecimal(10, p.netPay());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save payroll", e);
        }
    }

    public List<Payslip> findByEmployee(int employeeId) {
        List<Payslip> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(FIND_BY_EMPLOYEE)) {
            pstmt.setInt(1, employeeId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    BigDecimal hours = rs.getBigDecimal("hours_worked");
                    BigDecimal allowances = rs.getBigDecimal("allowances");
                    BigDecimal gross = rs.getBigDecimal("gross_pay");
                    list.add(new Payslip(
                            rs.getInt("employee_id"),
                            rs.getString("name"),
                            rs.getDate("period_start").toLocalDate(),
                            rs.getDate("period_end").toLocalDate(),
                            hours,
                            gross.subtract(allowances),
                            allowances,
                            gross,
                            rs.getBigDecimal("sss"),
                            rs.getBigDecimal("philhealth"),
                            rs.getBigDecimal("pagibig"),
                            rs.getBigDecimal("withholding_tax"),
                            rs.getBigDecimal("net_pay")));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to load payroll history", e);
        }
        return list;
    }
}
