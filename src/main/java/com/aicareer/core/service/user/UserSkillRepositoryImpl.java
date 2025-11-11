package com.aicareer.core.service.user;

import com.aicareer.core.model.user.UserSkills;
import com.aicareer.repository.user.UserSkillRepository;
import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.*;

public class UserSkillRepositoryImpl implements UserSkillRepository {

  private final DataSource dataSource;

  public UserSkillRepositoryImpl(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public UserSkills save(UserSkills skills) {
    Connection conn = null;
    try {
      conn = dataSource.getConnection();
      conn.setAutoCommit(false);

      String skillsSql = skills.getId() == null ?
          "INSERT INTO user_skills (user_id, full_compliance_percentage, calculated_at) VALUES (?, ?, ?)" :
          "UPDATE user_skills SET user_id = ?, full_compliance_percentage = ?, calculated_at = ? WHERE id = ?";

      PreparedStatement skillsStmt = conn.prepareStatement(skillsSql, Statement.RETURN_GENERATED_KEYS);

      if (skills.getId() == null) {
        skillsStmt.setLong(1, skills.getUserId());
        skillsStmt.setDouble(2, skills.getFullCompliancePercentage());
        skillsStmt.setTimestamp(3, Timestamp.valueOf(String.valueOf(Instant.now())));
      } else {
        skillsStmt.setLong(1, skills.getUserId());
        skillsStmt.setDouble(2, skills.getFullCompliancePercentage());
        skillsStmt.setTimestamp(3, Timestamp.valueOf(String.valueOf(Instant.now())));
        skillsStmt.setLong(4, skills.getId());
      }

      int affectedRows = skillsStmt.executeUpdate();

      if (affectedRows == 0) {
        throw new SQLException("Creating user skills failed, no rows affected.");
      }

      if (skills.getId() == null) {
        try (ResultSet generatedKeys = skillsStmt.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            skills.setId(generatedKeys.getLong(1));
          }
        }
      }

      saveSkillGaps(conn, skills);
      conn.commit();
      return skills;

    } catch (SQLException e) {
      if (conn != null) {
        try {
          conn.rollback();
        } catch (SQLException ex) {
          ex.printStackTrace();
        }
      }
      throw new RuntimeException("Error saving user skills", e);
    } finally {
      if (conn != null) {
        try {
          conn.setAutoCommit(true);
          conn.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  public Optional<UserSkills> findByUserId(Long userId) {
    String sql = "SELECT * FROM user_skills WHERE user_id = ?";

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, userId);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        UserSkills skills = mapRowToUserSkills(rs);
        skills.setSkillGaps(getSkillGapsByUserSkillsId(conn, skills.getId()));
        return Optional.of(skills);
      }

      return Optional.empty();

    } catch (SQLException e) {
      throw new RuntimeException("Error finding user skills by user id", e);
    }
  }

  private void saveSkillGaps(Connection conn, UserSkills skills) throws SQLException {
    String deleteSql = "DELETE FROM skill_gaps WHERE user_skills_id = ?";
    try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
      stmt.setLong(1, skills.getId());
      stmt.executeUpdate();
    }

    if (skills.getSkillGaps() != null && !skills.getSkillGaps().isEmpty()) {
      String insertSql = "INSERT INTO skill_gaps (user_skills_id, skill_name, gap_percentage) VALUES (?, ?, ?)";
      try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
        for (Map.Entry<String, Double> entry : skills.getSkillGaps().entrySet()) {
          stmt.setLong(1, skills.getId());
          stmt.setString(2, entry.getKey());
          stmt.setDouble(3, entry.getValue());
          stmt.addBatch();
        }
        stmt.executeBatch();
      }
    }
  }

  private Map<String, Double> getSkillGapsByUserSkillsId(Connection conn, Long userSkillsId) throws SQLException {
    String sql = "SELECT skill_name, gap_percentage FROM skill_gaps WHERE user_skills_id = ?";
    Map<String, Double> skillGaps = new HashMap<>();

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, userSkillsId);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        skillGaps.put(rs.getString("skill_name"), rs.getDouble("gap_percentage"));
      }
    }

    return skillGaps;
  }

  private UserSkills mapRowToUserSkills(ResultSet rs) throws SQLException {
    return UserSkills.builder()
        .id(rs.getLong("id"))
        .userId(rs.getLong("user_id"))
        .fullCompliancePercentage(rs.getDouble("full_compliance_percentage"))
        .calculatedAt(rs.getTimestamp("calculated_at").toInstant())
        .build();
  }
}