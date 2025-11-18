package com.aicareer.repository.roadmap.impl;

import com.aicareer.core.model.roadmap.RoadmapZone;
import com.aicareer.repository.roadmap.RoadmapZoneRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoadmapZoneRepositoryImpl implements RoadmapZoneRepository {

    private final DataSource dataSource;

    public RoadmapZoneRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public RoadmapZone save(RoadmapZone zone) {
        String sql = "INSERT INTO aicareer.roadmap_zones (roadmap_id, name, learning_goal, complexity_level, zone_order, created_at) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            zone.updateTimestamps();

            stmt.setLong(1, zone.getRoadmapId());
            stmt.setString(2, zone.getName());
            stmt.setString(3, zone.getLearningGoal());
            stmt.setString(4, zone.getComplexityLevel());
            stmt.setInt(5, zone.getZoneOrder());
            stmt.setTimestamp(6, Timestamp.from(zone.getCreatedAt()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating roadmap zone failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    zone.setId(generatedKeys.getLong(1));
                }
            }

            return zone;

        } catch (SQLException e) {
            throw new RuntimeException("Error saving roadmap zone", e);
        }
    }

    @Override
    public List<RoadmapZone> findByRoadmapId(Long roadmapId) {
        String sql = "SELECT * FROM aicareer.roadmap_zones WHERE roadmap_id = ? ORDER BY zone_order";
        List<RoadmapZone> zones = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, roadmapId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                zones.add(mapResultSetToRoadmapZone(rs));
            }

            return zones;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding roadmap zones by roadmap id", e);
        }
    }

    @Override
    public Optional<RoadmapZone> findById(Long id) {
        String sql = "SELECT * FROM aicareer.roadmap_zones WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToRoadmapZone(rs));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding roadmap zone by id", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM aicareer.roadmap_zones WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting roadmap zone", e);
        }
    }

    @Override
    public boolean deleteByRoadmapId(Long roadmapId) {
        String sql = "DELETE FROM aicareer.roadmap_zones WHERE roadmap_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, roadmapId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting roadmap zones by roadmap id", e);
        }
    }

    private RoadmapZone mapResultSetToRoadmapZone(ResultSet rs) throws SQLException {
        return RoadmapZone.builder()
                .id(rs.getLong("id"))
                .roadmapId(rs.getLong("roadmap_id"))
                .name(rs.getString("name"))
                .learningGoal(rs.getString("learning_goal"))
                .complexityLevel(rs.getString("complexity_level"))
                .zoneOrder(rs.getInt("zone_order"))
                .createdAt(rs.getTimestamp("created_at").toInstant())
                .build();
    }
}
