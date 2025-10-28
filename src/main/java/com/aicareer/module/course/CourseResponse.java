package com.aicareer.module.course;

import com.aicareer.core.model.Week;

import java.util.List;

public interface CourseResponse {
  public List<Week> parseCourseResponse(String jsonResponse); //дать ответ по неделям
}