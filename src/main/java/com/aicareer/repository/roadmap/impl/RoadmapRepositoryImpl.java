package com.aicareer.repository.roadmap.impl;

import com.aicareer.core.model.roadmap.Roadmap;
import com.aicareer.repository.roadmap.RoadmapRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoadmapRepositoryImpl implements RoadmapRepository {

    private final DataSource dataSource;

    public RoadmapRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Roadmap save(Roadmap roadmap) {
        String sql = "INSERT INTO aicareer.roadmaps (user_id, created_at, updated_at) VALUES (?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            roadmap.updateTimestamps();

            stmt.setLong(1, roadmap.getUserId());
            stmt.setTimestamp(2, Timestamp.from(roadmap.getCreatedAt()));
            stmt.setTimestamp(3, Timestamp.from(roadmap.getUpdatedAt()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating roadmap failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    roadmap.setId(generatedKeys.getLong(1));
                }
            }

            return roadmap;

        } catch (SQLException e) {
            throw new RuntimeException("Error saving roadmap", e);
        }
    }

    @Override
    public Optional<Roadmap> findById(Long id) {
        String sql = "SELECT * FROM aicareer.roadmaps WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToRoadmap(rs));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding roadmap by id", e);
        }
    }

    @Override
    public Optional<Roadmap> findByUserId(Long userId) {
        String sql = "SELECT * FROM aicareer.roadmaps WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToRoadmap(rs));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding roadmap by user id", e);
        }
    }

    @Override
    public List<Roadmap> findAllByUserId(Long userId) {
        String sql = "SELECT * FROM aicareer.roadmaps WHERE user_id = ? ORDER BY created_at DESC";
        List<Roadmap> roadmaps = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                roadmaps.add(mapResultSetToRoadmap(rs));
            }

            return roadmaps;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding all aicareer.roadmaps by user id", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM aicareer.roadmaps WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting roadmap", e);
        }
    }

    @Override
    public boolean deleteByUserId(Long userId) {
        String sql = "DELETE FROM aicareer.roadmaps WHERE user_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting roadmaps by user id", e);
        }
    }

    private Roadmap mapResultSetToRoadmap(ResultSet rs) throws SQLException {
        return Roadmap.builder()
                .id(rs.getLong("id"))
                .userId(rs.getLong("user_id"))
                .createdAt(rs.getTimestamp("created_at").toInstant())
                .updatedAt(rs.getTimestamp("updated_at").toInstant())
                .build();
    }
}