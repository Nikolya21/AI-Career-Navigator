package com.aicareer.core.service.roadmap;

import com.aicareer.core.model.roadmap.Roadmap;
import com.aicareer.core.model.roadmap.RoadmapZone;
import com.aicareer.core.model.courseModel.Week;
import com.aicareer.core.model.courseModel.Task;
import com.aicareer.repository.roadmap.RoadmapRepository;
import com.aicareer.repository.roadmap.RoadmapZoneRepository;
import com.aicareer.repository.roadmap.WeekRepository;
import com.aicareer.repository.roadmap.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoadmapService {

  private final RoadmapRepository roadmapRepository;
  private final RoadmapZoneRepository zoneRepository;
  private final WeekRepository weekRepository;
  private final TaskRepository taskRepository;

  public Roadmap saveCompleteRoadmap(Roadmap roadmap) {
    // Сохраняем основную roadmap
    Roadmap savedRoadmap = roadmapRepository.save(roadmap);

    // Сохраняем зоны
    if (roadmap.getRoadmapZones() != null) {
      for (RoadmapZone zone : roadmap.getRoadmapZones()) {
        zone.setRoadmapId(savedRoadmap.getId());
        RoadmapZone savedZone = zoneRepository.save(zone);

        // Сохраняем недели
        if (zone.getWeeks() != null) {
          for (Week week : zone.getWeeks()) {
            week.setRoadmapZoneId(savedZone.getId());
            Week savedWeek = weekRepository.save(week);

            // Сохраняем задачи
            if (week.getTasks() != null) {
              for (Task task : week.getTasks()) {
                task.setWeekId(savedWeek.getId());
                taskRepository.save(task);
              }
            }
          }
        }
      }
    }

    return savedRoadmap;
  }

  public Optional<Roadmap> findFullRoadmapById(Long roadmapId) {
    Optional<Roadmap> roadmapOpt = roadmapRepository.findById(roadmapId);

    if (roadmapOpt.isPresent()) {
      Roadmap roadmap = roadmapOpt.get();
      List<RoadmapZone> zones = zoneRepository.findByRoadmapId(roadmapId);
      roadmap.setRoadmapZones(zones);

      for (RoadmapZone zone : zones) {
        List<Week> weeks = weekRepository.findByRoadmapZoneId(zone.getId());
        zone.setWeeks(weeks);

        for (Week week : weeks) {
          List<Task> tasks = taskRepository.findByWeekId(week.getId());
          week.setTasks(tasks);
        }
      }

      return Optional.of(roadmap);
    }

    return Optional.empty();
  }

  public Optional<Roadmap> findRoadmapByUserId(Long userId) {
    return roadmapRepository.findByUserId(userId);
  }

  public boolean deleteRoadmap(Long roadmapId) {
    return roadmapRepository.delete(roadmapId);
  }
}