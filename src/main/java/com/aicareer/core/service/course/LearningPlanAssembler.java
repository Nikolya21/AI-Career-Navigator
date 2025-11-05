package com.aicareer.core.service.course;

import com.aicareer.core.DTO.CourseRequest;
import com.aicareer.core.DTO.ResponseByWeek;
import com.aicareer.core.model.Week;
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
    String rawLlmResponse = courseGenerator.generateCoursePlan(request);

    List<Week> parsedWeeks = courseResponse.parseCourseResponse(rawLlmResponse);

    List<Week> distributedWeeks = distributionByWeek.distributionByWeek(parsedWeeks);
    return new ResponseByWeek(distributedWeeks);
  }
}