package com.aicareer.core.service.user;

import com.aicareer.core.model.user.CVData;
import com.aicareer.repository.user.CVDataRepository;
import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.Optional;

public class CVDataRepositoryImpl implements CVDataRepository {

  private final DataSource dataSource;

  public CVDataRepositoryImpl(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public CVData save(CVData cvData) {
    String sql = cvData.getId() == null ?
        "INSERT INTO cv_data (user_id, file_path, information, uploaded_at) VALUES (?, ?, ?, ?)" :
        "UPDATE cv_data SET user_id = ?, file_path = ?, information = ? WHERE id = ?";

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      if (cvData.getId() == null) {
        stmt.setLong(1, cvData.getUserId());
        stmt.setString(2, cvData.getFile() != null ? cvData.getFile().getAbsolutePath() : null);
        stmt.setString(3, cvData.getInformation());
        stmt.setTimestamp(4, Timestamp.valueOf(String.valueOf(Instant.now())));
      } else {
        stmt.setLong(1, cvData.getUserId());
        stmt.setString(2, cvData.getFile() != null ? cvData.getFile().getAbsolutePath() : null);
        stmt.setString(3, cvData.getInformation());
        stmt.setLong(4, cvData.getId());
      }

      int affectedRows = stmt.executeUpdate();

      if (affectedRows == 0) {
        throw new SQLException("Creating CV data failed, no rows affected.");
      }

      if (cvData.getId() == null) {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            cvData.setId(generatedKeys.getLong(1));
          }
        }
      }

      return cvData;

    } catch (SQLException e) {
      throw new RuntimeException("Error saving CV data", e);
    }
  }

  @Override
  public Optional<CVData> findByUserId(Long userId) {
    String sql = "SELECT * FROM cv_data WHERE user_id = ?";

    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setLong(1, userId);
      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        return Optional.of(mapRowToCVData(rs));
      }

      return Optional.empty();

    } catch (SQLException e) {
      throw new RuntimeException("Error finding CV data by user id", e);
    }
  }

  private CVData mapRowToCVData(ResultSet rs) throws SQLException {
    return CVData.builder()
        .id(rs.getLong("id"))
        .userId(rs.getLong("user_id"))
        .file(rs.getString("file_path") != null ? new java.io.File(rs.getString("file_path")) : null)
        .information(rs.getString("information"))
        .uploadedAt(rs.getTimestamp("uploaded_at").toInstant())
        .build();
  }
}