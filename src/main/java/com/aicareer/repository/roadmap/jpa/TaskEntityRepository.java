package com.aicareer.repository.roadmap.jpa;

import com.aicareer.core.model.roadmap.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskEntityRepository extends JpaRepository<TaskEntity, Long> {
  List<TaskEntity> findByWeekId(Long weekId);
}