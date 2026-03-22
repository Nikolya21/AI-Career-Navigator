package com.aicareer.core.service.roadmap;

import com.aicareer.core.model.courseModel.Task;
import com.aicareer.core.model.courseModel.Week;
import com.aicareer.core.model.roadmap.Roadmap;
import com.aicareer.core.model.roadmap.RoadmapZone;
import com.aicareer.core.model.roadmap.entity.RoadmapEntity;
import com.aicareer.core.model.roadmap.entity.RoadmapZoneEntity;
import com.aicareer.core.model.roadmap.entity.TaskEntity;
import com.aicareer.core.model.roadmap.entity.WeekEntity;
import com.aicareer.repository.roadmap.jpa.RoadmapEntityRepository;
import com.aicareer.repository.roadmap.jpa.RoadmapZoneEntityRepository;
import com.aicareer.repository.roadmap.jpa.TaskEntityRepository;
import com.aicareer.repository.roadmap.jpa.WeekEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoadmapService {

  private final RoadmapEntityRepository roadmapEntityRepository;
  private final RoadmapZoneEntityRepository zoneEntityRepository;
  private final WeekEntityRepository weekEntityRepository;
  private final TaskEntityRepository taskEntityRepository;

  // ========== Конвертация из старых моделей в сущности ==========

  private RoadmapEntity toEntity(Roadmap roadmap) {
    if (roadmap == null) return null;
    RoadmapEntity entity = RoadmapEntity.builder()
        .id(roadmap.getId())
        .userId(roadmap.getUserId())
        .build();
    // Временные метки установятся автоматически через @PrePersist/@PreUpdate
    return entity;
  }

  private RoadmapZoneEntity toEntity(RoadmapZone zone, Long roadmapId) {
    if (zone == null) return null;
    RoadmapZoneEntity entity = RoadmapZoneEntity.builder()
        .id(zone.getId())
        .roadmapId(roadmapId)
        .name(zone.getName())
        .learningGoal(zone.getLearningGoal())
        .complexityLevel(zone.getComplexityLevel())
        .zoneOrder(zone.getZoneOrder())
        .build();
    return entity;
  }

  private WeekEntity toEntity(Week week, Long roadmapZoneId) {
    if (week == null) return null;
    WeekEntity entity = WeekEntity.builder()
        .id(week.getId())
        .roadmapZoneId(roadmapZoneId)
        .number(week.getNumber())
        .goal(week.getGoal())
        .build();
    return entity;
  }

  private TaskEntity toEntity(Task task, Long weekId) {
    if (task == null) return null;
    TaskEntity entity = TaskEntity.builder()
        .id(task.getId())
        .weekId(weekId)
        .description(task.getDescription())
        .urls(task.getUrls())
        .build();
    return entity;
  }

  // ========== Конвертация из сущностей в старые модели ==========

  private Roadmap toModel(RoadmapEntity entity) {
    if (entity == null) return null;
    Roadmap roadmap = Roadmap.builder()
        .id(entity.getId())
        .userId(entity.getUserId())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
    // Зоны загружаются отдельно
    return roadmap;
  }

  private RoadmapZone toModel(RoadmapZoneEntity entity) {
    if (entity == null) return null;
    return RoadmapZone.builder()
        .id(entity.getId())
        .roadmapId(entity.getRoadmapId())
        .name(entity.getName())
        .learningGoal(entity.getLearningGoal())
        .complexityLevel(entity.getComplexityLevel())
        .zoneOrder(entity.getZoneOrder())
        .createdAt(entity.getCreatedAt())
        .build();
  }

  private Week toModel(WeekEntity entity) {
    if (entity == null) return null;
    return Week.builder()
        .id(entity.getId())
        .roadmapZoneId(entity.getRoadmapZoneId())
        .number(entity.getNumber())
        .goal(entity.getGoal())
        .createdAt(entity.getCreatedAt())
        .build();
  }

  private Task toModel(TaskEntity entity) {
    if (entity == null) return null;
    return Task.builder()
        .id(entity.getId())
        .weekId(entity.getWeekId())
        .description(entity.getDescription())
        .urls(entity.getUrls())
        .createdAt(entity.getCreatedAt())
        .build();
  }

  // ========== Методы сервиса ==========

  public Roadmap saveCompleteRoadmap(Roadmap roadmap) {
    log.info("Сохранение roadmap для пользователя {}", roadmap.getUserId());

    // Сохраняем основную roadmap
    RoadmapEntity roadmapEntity = toEntity(roadmap);
    RoadmapEntity savedRoadmap = roadmapEntityRepository.save(roadmapEntity);

    if (roadmap.getRoadmapZones() != null) {
      for (RoadmapZone zone : roadmap.getRoadmapZones()) {
        RoadmapZoneEntity zoneEntity = toEntity(zone, savedRoadmap.getId());
        RoadmapZoneEntity savedZone = zoneEntityRepository.save(zoneEntity);

        if (zone.getWeeks() != null) {
          for (Week week : zone.getWeeks()) {
            WeekEntity weekEntity = toEntity(week, savedZone.getId());
            WeekEntity savedWeek = weekEntityRepository.save(weekEntity);

            if (week.getTasks() != null) {
              for (Task task : week.getTasks()) {
                TaskEntity taskEntity = toEntity(task, savedWeek.getId());
                taskEntityRepository.save(taskEntity);
              }
            }
          }
        }
      }
    }

    // Возвращаем сохранённый roadmap (в виде старой модели, но с ID)
    roadmap.setId(savedRoadmap.getId());
    roadmap.setCreatedAt(savedRoadmap.getCreatedAt());
    roadmap.setUpdatedAt(savedRoadmap.getUpdatedAt());
    return roadmap;
  }

  public Optional<Roadmap> findFullRoadmapById(Long roadmapId) {
    Optional<RoadmapEntity> roadmapOpt = roadmapEntityRepository.findById(roadmapId);
    if (roadmapOpt.isEmpty()) return Optional.empty();

    RoadmapEntity entity = roadmapOpt.get();
    Roadmap roadmap = toModel(entity);

    List<RoadmapZoneEntity> zoneEntities = zoneEntityRepository.findByRoadmapId(roadmapId);
    List<RoadmapZone> zones = zoneEntities.stream().map(ze -> {
      RoadmapZone zone = toModel(ze);
      List<WeekEntity> weekEntities = weekEntityRepository.findByRoadmapZoneId(ze.getId());
      List<Week> weeks = weekEntities.stream().map(we -> {
        Week week = toModel(we);
        List<TaskEntity> taskEntities = taskEntityRepository.findByWeekId(we.getId());
        List<Task> tasks = taskEntities.stream().map(this::toModel).collect(Collectors.toList());
        week.setTasks(tasks);
        return week;
      }).collect(Collectors.toList());
      zone.setWeeks(weeks);
      return zone;
    }).collect(Collectors.toList());

    roadmap.setRoadmapZones(zones);
    return Optional.of(roadmap);
  }

  public Optional<Roadmap> findRoadmapByUserId(Long userId) {
    Optional<RoadmapEntity> entityOpt = roadmapEntityRepository.findByUserId(userId);
    return entityOpt.map(this::toModel);
  }

  public boolean deleteRoadmap(Long roadmapId) {
    if (!roadmapEntityRepository.existsById(roadmapId)) return false;
    roadmapEntityRepository.deleteById(roadmapId);
    return true;
  }
}