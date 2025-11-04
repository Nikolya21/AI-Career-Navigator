package com.aicareer.module.course;

import com.aicareer.core.DTO.CourseRequest;

public interface GenerateCourseFromGpt {
  String generateCoursePlan(CourseRequest courseRequest); //создание курса
}
