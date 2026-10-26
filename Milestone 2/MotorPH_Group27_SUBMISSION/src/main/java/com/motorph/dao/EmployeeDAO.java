package com.motorph.dao;

import com.motorph.config.DatabaseConfig;
import com.motorph.model.Employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * CONTROL #1: parameterized queries only.
 *
 * BEFORE (vulnerable, removed):
 *   String sql = "SELECT * FROM employees WHERE last_name LIKE '%" + keyword + "%'";
 *   stmt.executeQuery(sql);
 *   -> typing  ' OR '1'='1' --  would dump every employee.
 *
 * AFTER (this class): the keyword is bound with pstmt.setString(), so the
 * database treats it as plain text, never as SQL commands.
 */
public class EmployeeDAO {

    private static final String BASE_SELECT =
            "SELECT employee_id, first_name, last_name, position, department, basic_salary, "
          + "hourly_rate, rice_subsidy, phone_allowance, clothing_allowance FROM employees";

    private static final String SEARCH =
            BASE_SELECT + " WHERE last_name LIKE ? OR first_name LIKE ? OR department LIKE ? ORDER BY employee_id";
    private static final String FIND_BY_ID = BASE_SELECT + " WHERE employee_id = ?";
    private static final String FIND_ALL = BASE_SELECT + " ORDER BY employee_id";

    private final DatabaseConfig db = DatabaseConfig.getInstance();

    public List<Employee> search(String keyword) {
        String pattern = "%" + escapeLike(keyword) + "%";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SEARCH)) {
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
            return readAll(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to search employees", e);
        }
    }

    public Optional<Employee> findById(int employeeId) {
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(FIND_BY_ID)) {
            pstmt.setInt(1, employeeId);
            List<Employee> list = readAll(pstmt);
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find employee", e);
        }
    }

    public List<Employee> findAll() {
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(FIND_ALL)) {
            return readAll(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list employees", e);
        }
    }

    private List<Employee> readAll(PreparedStatement pstmt) throws SQLException {
        List<Employee> result = new ArrayList<>();
        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                result.add(new Employee(
                        rs.getInt("employee_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("position"),
                        rs.getString("department"),
                        rs.getBigDecimal("basic_salary"),
                        rs.getBigDecimal("hourly_rate"),
                        rs.getBigDecimal("rice_subsidy"),
                        rs.getBigDecimal("phone_allowance"),
                        rs.getBigDecimal("clothing_allowance")));
            }
        }
        return result;
    }

    /** Stops users from turning % and _ into wildcards. */
    private static String escapeLike(String input) {
        return input.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
