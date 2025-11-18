package com.aicareer.repository.course;

import com.aicareer.core.dto.courseDto.CourseRequest;

public interface PromptGenerator {
  public String generatePrompt(CourseRequest request); //создание промпта

}
