package com.aicareer.repository.user.impl;

import com.aicareer.core.model.user.UserLearningProfile;
import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.repository.user.UserPreferencesRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@Repository
public class UserPreferencesRepositoryImpl implements UserPreferencesRepository {

  private final DataSource dataSource;
  private final ObjectMapper objectMapper;

  public UserPreferencesRepositoryImpl(DataSource dataSource, ObjectMapper objectMapper) {
    this.dataSource = dataSource;
    this.objectMapper = objectMapper;
  }

  @Override
  public UserPreferences save(UserPreferences userPreferences) {
    if (userPreferences.getId() == null) {
      return insert(userPreferences);
    } else {
      return update(userPreferences);
    }
  }

  private UserPreferences insert(UserPreferences preferences) {
    String sql = "INSERT INTO aicareer.user_preferences (user_id, info_about_person, user_learning_profile) VALUES (?, ?, ?)";

    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      stmt.setLong(1, preferences.getUserId());
      stmt.setString(2, preferences.getInfoAboutPerson());

      // Преобразуем UserLearningProfile в JSON строку
      String profileJson = null;
      if (preferences.getUserLearningProfile() != null) {
        try {
          profileJson = objectMapper.writeValueAsString(preferences.getUserLearningProfile());
        } catch (JsonProcessingException e) {
          throw new RuntimeException("Ошибка сериализации UserLearningProfile", e);
        }
      }
      stmt.setString(3, profileJson);

      int affectedRows = stmt.executeUpdate();

      if (affectedRows == 0) {
        throw new SQLException("Creating user preferences failed, no rows affected.");
      }

      try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          preferences.setId(generatedKeys.getLong(1));
        }
      }

      return preferences;

    } catch (SQLException e) {
      throw new RuntimeException("Error inserting user preferences", e);
    }
  }

  private UserPreferences update(UserPreferences preferences) {
    String sql = "UPDATE aicareer.user_preferences SET user_id = ?, info_about_person = ?, user_learning_profile = ? WHERE id = ?";

    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, preferences.getUserId());
      stmt.setString(2, preferences.getInfoAboutPerson());

      // Преобразуем UserLearningProfile в JSON строку
      String profileJson = null;
      if (preferences.getUserLearningProfile() != null) {
        try {
          profileJson = objectMapper.writeValueAsString(preferences.getUserLearningProfile());
        } catch (JsonProcessingException e) {
          throw new RuntimeException("Ошибка сериализации UserLearningProfile", e);
        }
      }
      stmt.setString(3, profileJson);

      stmt.setLong(4, preferences.getId());

      int affectedRows = stmt.executeUpdate();

      if (affectedRows == 0) {
        throw new SQLException("Updating user preferences failed, no rows affected.");
      }

      return preferences;

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
    UserPreferences preferences = UserPreferences.builder()
      .id(rs.getLong("id"))
      .userId(rs.getLong("user_id"))
      .infoAboutPerson(rs.getString("info_about_person"))
      .build();

    // Читаем JSON из поля user_learning_profile
    String profileJson = rs.getString("user_learning_profile");
    if (profileJson != null) {
      try {
        UserLearningProfile profile = objectMapper.readValue(profileJson, UserLearningProfile.class);
        preferences.setUserLearningProfile(profile);
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Ошибка парсинга user_learning_profile", e);
      }
    }

    return preferences;
  }
}