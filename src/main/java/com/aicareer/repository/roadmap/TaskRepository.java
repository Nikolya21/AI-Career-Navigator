package com.aicareer.repository.roadmap;

import com.aicareer.core.model.courseModel.Task;
import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    Task save(Task task);
    List<Task> findByWeekId(Long weekId);
    Optional<Task> findById(Long id);
    boolean delete(Long id);
    boolean deleteByWeekId(Long weekId);
}