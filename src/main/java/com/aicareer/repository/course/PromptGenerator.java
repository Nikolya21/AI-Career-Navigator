package com.aicareer.repository.course;

import com.aicareer.core.DTO.courseDto.CourseRequest;

public interface PromptGenerator {
  public String generatePrompt(CourseRequest request); //создание промпта

}
