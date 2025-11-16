package com.aicareer.repository.roadmap.impl;

import com.aicareer.core.model.courseModel.Task;
import com.aicareer.repository.roadmap.TaskRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskRepositoryImpl implements TaskRepository {

    private final DataSource dataSource;

    public TaskRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Task save(Task task) {
        String sql = "INSERT INTO tasks (week_id, description, urls, created_at) VALUES (?, ?, ?::jsonb, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            task.updateTimestamps();

            stmt.setLong(1, task.getWeekId());
            stmt.setString(2, task.getDescription());

            // Конвертируем List<String> в JSON
            String urlsJson = convertUrlsToJson(task.getUrls());
            stmt.setString(3, urlsJson);

            stmt.setTimestamp(4, Timestamp.from(task.getCreatedAt()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating task failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    task.setId(generatedKeys.getLong(1));
                }
            }

            return task;

        } catch (SQLException e) {
            throw new RuntimeException("Error saving task", e);
        }
    }

    @Override
    public List<Task> findByWeekId(Long weekId) {
        String sql = "SELECT * FROM tasks WHERE week_id = ? ORDER BY created_at";
        List<Task> tasks = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, weekId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tasks.add(mapResultSetToTask(rs));
            }

            return tasks;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding tasks by week id", e);
        }
    }

    @Override
    public Optional<Task> findById(Long id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToTask(rs));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error finding task by id", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting task", e);
        }
    }

    @Override
    public boolean deleteByWeekId(Long weekId) {
        String sql = "DELETE FROM tasks WHERE week_id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, weekId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting tasks by week id", e);
        }
    }

    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        return Task.builder()
                .id(rs.getLong("id"))
                .weekId(rs.getLong("week_id"))
                .description(rs.getString("description"))
                .urls(convertJsonToUrls(rs.getString("urls")))
                .createdAt(rs.getTimestamp("created_at").toInstant())
                .build();
    }

    private String convertUrlsToJson(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return "[]";
        }
        // Простая конвертация в JSON массив
        return "[\"" + String.join("\",\"", urls) + "\"]";
    }

    private List<String> convertJsonToUrls(String urlsJson) {
        List<String> urls = new ArrayList<>();
        if (urlsJson != null && !urlsJson.equals("[]")) {
            // Убираем квадратные скобки и кавычки, разделяем по запятым
            String cleaned = urlsJson.replaceAll("[\\[\\]\"]", "");
            if (!cleaned.isEmpty()) {
                String[] urlArray = cleaned.split(",");
                for (String url : urlArray) {
                    if (!url.trim().isEmpty()) {
                        urls.add(url.trim());
                    }
                }
            }
        }
        return urls;
    }
}