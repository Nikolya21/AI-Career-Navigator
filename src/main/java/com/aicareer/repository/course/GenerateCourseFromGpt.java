package com.aicareer.repository.course;

import com.aicareer.core.dto.courseDto.CourseRequest;

public interface GenerateCourseFromGpt {
  String generateCoursePlan(CourseRequest courseRequest); //создание курса
}
