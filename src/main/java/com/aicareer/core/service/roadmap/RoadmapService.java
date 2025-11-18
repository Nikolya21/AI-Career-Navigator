package com.aicareer.core.service.roadmap;

import com.aicareer.core.model.roadmap.Roadmap;
import com.aicareer.core.model.roadmap.RoadmapZone;
import com.aicareer.core.model.courseModel.Week;
import com.aicareer.core.model.courseModel.Task;
import com.aicareer.repository.roadmap.RoadmapRepository;
import com.aicareer.repository.roadmap.RoadmapZoneRepository;
import com.aicareer.repository.roadmap.WeekRepository;
import com.aicareer.repository.roadmap.TaskRepository;
import com.aicareer.repository.roadmap.impl.RoadmapRepositoryImpl;
import com.aicareer.repository.roadmap.impl.RoadmapZoneRepositoryImpl;
import com.aicareer.repository.roadmap.impl.TaskRepositoryImpl;
import com.aicareer.repository.roadmap.impl.WeekRepositoryImpl;


import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapZoneRepository zoneRepository;
    private final WeekRepository weekRepository;
    private final TaskRepository taskRepository;

    public RoadmapService(DataSource dataSource) {
        this.roadmapRepository = new RoadmapRepositoryImpl(dataSource);
        this.zoneRepository = new RoadmapZoneRepositoryImpl(dataSource);
        this.weekRepository = new WeekRepositoryImpl(dataSource);
        this.taskRepository = new TaskRepositoryImpl(dataSource);
    }

    /**
     * Сохранить всю иерархию Roadmap (с зонами, неделями и задачами)
     */
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

    /**
     * Получить полную roadmap со всей иерархией
     */
    public Optional<Roadmap> findFullRoadmapById(Long roadmapId) {
        Optional<Roadmap> roadmapOpt = roadmapRepository.findById(roadmapId);

        if (roadmapOpt.isPresent()) {
            Roadmap roadmap = roadmapOpt.get();

            // Загружаем зоны
            List<RoadmapZone> zones = zoneRepository.findByRoadmapId(roadmapId);
            roadmap.setRoadmapZones(zones);

            // Для каждой зоны загружаем недели и задачи
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

    /**
     * Получить roadmap пользователя
     */
    public Optional<Roadmap> findRoadmapByUserId(Long userId) {
        return roadmapRepository.findByUserId(userId);
    }

    /**
     * Удалить roadmap и все связанные данные
     */
    public boolean deleteRoadmap(Long roadmapId) {
        // Каскадное удаление через репозитории
        // (или настроить CASCADE DELETE в БД)
        return roadmapRepository.delete(roadmapId);
    }
}
