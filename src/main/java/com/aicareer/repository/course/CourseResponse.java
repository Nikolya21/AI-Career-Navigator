package com.aicareer.repository.course;

import com.aicareer.core.model.Week;

import java.util.List;

public interface CourseResponse {
  public List<Week> parseCourseResponse(String llmResponse); //дать ответ по неделям
}