package com.aicareer.repository.user.impl;

import com.aicareer.core.model.user.User;
import com.aicareer.core.model.user.UserSkills;
import com.aicareer.repository.user.UserRepository;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {

  private final DataSource dataSource;

  public UserRepositoryImpl(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public User save(User user) {
    String sql = user.getId() == null ?
        "INSERT INTO users (name, email, password_hash, vacancy_now, roadmap_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)" :
        "UPDATE users SET name = ?, email = ?, password_hash = ?, vacancy_now = ?, roadmap_id = ?, updated_at = ? WHERE id = ?";

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      Instant now = Instant.now();

      if (user.getId() == null) {
        stmt.setString(1, user.getName());
        stmt.setString(2, user.getEmail());
        stmt.setString(3, user.getPasswordHash());
        stmt.setString(4, user.getVacancyNow());
        stmt.setObject(5, user.getRoadmapId());
        stmt.setTimestamp(6, Timestamp.valueOf(String.valueOf(now)));
        stmt.setTimestamp(7, Timestamp.valueOf(String.valueOf(now)));
      } else {
        stmt.setString(1, user.getName());
        stmt.setString(2, user.getEmail());
        stmt.setString(3, user.getPasswordHash());
        stmt.setString(4, user.getVacancyNow());
        stmt.setObject(5, user.getRoadmapId());
        stmt.setTimestamp(6, Timestamp.valueOf(String.valueOf(now)));
        stmt.setLong(7, user.getId());
      }

      int affectedRows = stmt.executeUpdate();

      if (affectedRows == 0) {
        throw new SQLException("Creating user failed, no rows affected.");
      }

      if (user.getId() == null) {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            user.setId(generatedKeys.getLong(1));
          }
        }
      }

      if (user.getCreatedAt() == null) {
        user.setCreatedAt(now);
      }
      user.setUpdatedAt(now);

      return user;

    } catch (SQLException e) {
      throw new RuntimeException("Error saving user", e);
    }
  }

  @Override
  public Optional<User> findById(Long id) {
    String sql = "SELECT * FROM users WHERE id = ?";

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, id);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return Optional.of(mapRowToUser(rs));
      }

      return Optional.empty();

    } catch (SQLException e) {
      throw new RuntimeException("Error finding user by id", e);
    }
  }

  @Override
  public Optional<User> findByEmail(String email) {
    String sql = "SELECT * FROM users WHERE email = ?";

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, email);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return Optional.of(mapRowToUser(rs));
      }

      return Optional.empty();

    } catch (SQLException e) {
      throw new RuntimeException("Error finding user by email", e);
    }
  }

  @Override
  public boolean existsByEmail(String email) {
    String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, email);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return rs.getInt(1) > 0;
      }

      return false;

    } catch (SQLException e) {
      throw new RuntimeException("Error checking if email exists", e);
    }
  }

  @Override
  public boolean delete(Long id) {
    String sql = "DELETE FROM users WHERE id = ?";

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, id);
      return stmt.executeUpdate() > 0;

    } catch (SQLException e) {
      throw new RuntimeException("Error deleting user", e);
    }
  }

  @Override
  public List<User> findAll() {
    String sql = "SELECT * FROM users ORDER BY created_at DESC";
    List<User> users = new ArrayList<>();

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {

      while (rs.next()) {
        users.add(mapRowToUser(rs));
      }

      return users;

    } catch (SQLException e) {
      throw new RuntimeException("Error finding all users", e);
    }
  }

  private User mapRowToUser(ResultSet rs) throws SQLException {
    return User.builder()
        .id(rs.getLong("id"))
        .name(rs.getString("name"))
        .email(rs.getString("email"))
        .passwordHash(rs.getString("password_hash"))
        .vacancyNow(rs.getString("vacancy_now"))
        .roadmapId(rs.getLong("roadmap_id"))
        .createdAt(rs.getTimestamp("created_at").toInstant())
        .updatedAt(rs.getTimestamp("updated_at").toInstant())
        .build();
  }
}