package com.motorph.dao;

import com.motorph.config.DatabaseConfig;
import com.motorph.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * CONTROL #1: every query uses PreparedStatement with bound parameters.
 * No user input is ever concatenated into SQL text.
 * Returns plain Java objects (Optional<User>), never HTTP responses.
 */
public class UserDAO {

    private static final String FIND_BY_USERNAME =
            "SELECT user_id, username, password, role, employee_id FROM users WHERE username = ?";
    private static final String UPDATE_PASSWORD =
            "UPDATE users SET password = ? WHERE user_id = ?";

    private final DatabaseConfig db = DatabaseConfig.getInstance();

    public Optional<User> findByUsername(String username) {
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(FIND_BY_USERNAME)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                int empId = rs.getInt("employee_id");
                Integer employeeId = rs.wasNull() ? null : empId;
                return Optional.of(new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        employeeId));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to look up user", e);
        }
    }

    public void updatePassword(int userId, String newHash) {
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_PASSWORD)) {
            pstmt.setString(1, newHash);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update password", e);
        }
    }
}
