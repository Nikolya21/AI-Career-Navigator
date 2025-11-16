package com.aicareer.repository.roadmap.impl;

import com.aicareer.core.model.courseModel.Week;
import com.aicareer.repository.roadmap.WeekRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WeekRepositoryImpl implements WeekRepository {

    private final DataSource dataSource;

    public WeekRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Week save(Week week) {
        String sql = "INSERT INTO weeks (roadmap_zone_id, week_number, goal, created_at) VALUES (?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            week.updateTimestamps();

            stmt.setLong(1, week.getRoadmapZoneId());
            stmt.setInt(2, week.getNumber());
            stmt.setString(3, week.getGoal());
            stmt.setTimestamp(4, Timestamp.from(week.getCreatedAt()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating week failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    week.setId(generatedKeys.getLong(1));
                }
            }

            return week;

        } catch (SQLException e) {
            throw new RuntimeException("Error saving week", e);
        }
    }

    @Override
    public List<Week> findByRoadmapZoneId(Long roadmapZoneId) {
        String sql = "SELECT * FROM weeks WHERE roadmap_zone_id = ? ORDER BY week_number";
        List<Week> weeks = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, roadmapZoneId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                weeks.add(mapResultSetToWeek(rs));
            }

            return weeks;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding weeks by roadmap zone id", e);
        }
    }

    @Override
    public Optional<Week> findById(Long id) {
        String sql = "SELECT * FROM weeks WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToWeek(rs));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding week by id", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM weeks WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting week", e);
        }
    }

    @Override
    public boolean deleteByRoadmapZoneId(Long roadmapZoneId) {
        String sql = "DELETE FROM weeks WHERE roadmap_zone_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, roadmapZoneId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting weeks by roadmap zone id", e);
        }
    }

    private Week mapResultSetToWeek(ResultSet rs) throws SQLException {
        return Week.builder()
                .id(rs.getLong("id"))
                .roadmapZoneId(rs.getLong("roadmap_zone_id"))
                .number(rs.getInt("week_number"))
                .goal(rs.getString("goal"))
                .createdAt(rs.getTimestamp("created_at").toInstant())
                .build();
    }
}