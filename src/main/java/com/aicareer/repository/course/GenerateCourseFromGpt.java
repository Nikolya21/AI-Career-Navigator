package com.aicareer.repository.course;

import com.aicareer.core.DTO.courseDto.CourseRequest;

public interface GenerateCourseFromGpt {
  String generateCoursePlan(CourseRequest courseRequest); //создание курса
}
