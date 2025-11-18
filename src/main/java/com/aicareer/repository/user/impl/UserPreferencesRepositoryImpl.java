package com.aicareer.repository.user.impl;

import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.repository.user.UserPreferencesRepository;


import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

public class UserPreferencesRepositoryImpl implements UserPreferencesRepository {

    private final DataSource dataSource;

    public UserPreferencesRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public UserPreferences save(UserPreferences userPreferences) {
        if (userPreferences.getId() == null) {
            return insert(userPreferences);
        } else {
            return update(userPreferences);
        }
    }

    private UserPreferences insert(UserPreferences userPreferences) {
        String sql = "INSERT INTO aicareer.user_preferences (user_id, info_about_person) VALUES (?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, userPreferences.getUserId());
            stmt.setString(2, userPreferences.getInfoAboutPerson());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user preferences failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    userPreferences.setId(generatedKeys.getLong(1));
                }
            }

            return userPreferences;

        } catch (SQLException e) {
            throw new RuntimeException("Error inserting user preferences", e);
        }
    }

    private UserPreferences update(UserPreferences userPreferences) {
        String sql = "UPDATE aicareer.user_preferences SET user_id = ?, info_about_person = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userPreferences.getUserId());
            stmt.setString(2, userPreferences.getInfoAboutPerson());
            stmt.setLong(3, userPreferences.getId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Updating user preferences failed, no rows affected.");
            }

            return userPreferences;

        } catch (SQLException e) {
            throw new RuntimeException("Error updating user preferences", e);
        }
    }

    @Override
    public Optional<UserPreferences> findById(Long id) {
        String sql = "SELECT * FROM aicareer.user_preferences WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUserPreferences(rs));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding user preferences by id", e);
        }
    }

    @Override
    public Optional<UserPreferences> findByUserId(Long userId) {
        String sql = "SELECT * FROM aicareer.user_preferences WHERE user_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUserPreferences(rs));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding user preferences by user id", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM aicareer.user_preferences WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user preferences", e);
        }
    }

    @Override
    public boolean deleteByUserId(Long userId) {
        String sql = "DELETE FROM aicareer.user_preferences WHERE user_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user preferences by user id", e);
        }
    }

    @Override
    public boolean existsByUserId(Long userId) {
        String sql = "SELECT COUNT(*) FROM aicareer.user_preferences WHERE user_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

            return false;

        } catch (SQLException e) {
            throw new RuntimeException("Error checking if user preferences exist", e);
        }
    }

    private UserPreferences mapResultSetToUserPreferences(ResultSet rs) throws SQLException {
        return UserPreferences.builder()
                .id(rs.getLong("id"))
                .userId(rs.getLong("user_id"))
                .infoAboutPerson(rs.getString("info_about_person"))
                .build();
    }
}
