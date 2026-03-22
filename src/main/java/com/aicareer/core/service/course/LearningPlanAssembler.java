package com.aicareer.core.service.course;

import com.aicareer.core.dto.courseDto.CourseRequest;
import com.aicareer.core.dto.courseDto.ResponseByWeek;
import com.aicareer.core.model.courseModel.Week;
import com.aicareer.repository.course.AssemblePlan;
import com.aicareer.repository.course.CourseResponse;
import com.aicareer.repository.course.DistributionByWeek;
import com.aicareer.repository.course.GenerateCourseFromGpt;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LearningPlanAssembler implements AssemblePlan {

  private final GenerateCourseFromGpt courseGenerator;
  private final CourseResponse courseResponse;
  private final DistributionByWeek distributionByWeek;

  @PostConstruct
  public void init() {
    log.info("🚀 LearningPlanAssembler инициализирован");
  }

  @Override
  public ResponseByWeek assemblePlan(CourseRequest request) {
    log.info("🎯 Начало сборки учебного плана...");

    try {
      String rawLlmResponse = courseGenerator.generateCoursePlan(request);

      List<Week> parsedWeeks = courseResponse.parseCourseResponse(rawLlmResponse);

      List<Week> distributedWeeks = distributionByWeek.distributionByWeek(parsedWeeks);
      log.info("✅ Распределение по неделям завершено");

      return new ResponseByWeek(distributedWeeks);

    } catch (Exception e) {
      log.error("❌ Ошибка при сборке учебного плана: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Не удалось собрать учебный план: " + e.getMessage(), e);
    }
  }
}