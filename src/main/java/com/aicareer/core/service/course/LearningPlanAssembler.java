package com.aicareer.core.service.course;

import com.aicareer.core.dto.courseDto.CourseRequest;
import com.aicareer.core.dto.courseDto.ResponseByWeek;
import com.aicareer.core.model.courseModel.Week;
import com.aicareer.repository.course.AssemblePlan;
import com.aicareer.repository.course.CourseResponse;
import com.aicareer.repository.course.DistributionByWeek;
import com.aicareer.repository.course.GenerateCourseFromGpt;

import java.util.List;

public class LearningPlanAssembler implements AssemblePlan {

  private final GenerateCourseFromGpt courseGenerator;
  private final CourseResponse courseResponse;
  private final DistributionByWeek distributionByWeek;

  public LearningPlanAssembler(GenerateCourseFromGpt courseGenerator, CourseResponse courseResponse, DistributionByWeek distributionByWeek) {
    this.courseGenerator = courseGenerator;
    this.courseResponse = courseResponse;
    this.distributionByWeek = distributionByWeek;
  }

  @Override
  public ResponseByWeek assemblePlan(CourseRequest request) {
    System.out.println("🎯 Начало сборки учебного плана...");

    try {
      String rawLlmResponse = courseGenerator.generateCoursePlan(request);

      List<Week> parsedWeeks = courseResponse.parseCourseResponse(rawLlmResponse);

      List<Week> distributedWeeks = distributionByWeek.distributionByWeek(parsedWeeks);
      System.out.println("✅ Распределение по неделям завершено");

      return new ResponseByWeek(distributedWeeks);

    } catch (Exception e) {
      System.err.println("❌ Ошибка при сборке учебного плана: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Не удалось собрать учебный план: " + e.getMessage(), e);
    }
  }
}