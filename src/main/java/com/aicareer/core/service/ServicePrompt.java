package com.aicareer.core.service;

import com.aicareer.core.DTO.CourseRequest;
import com.aicareer.core.Validator.SyntaxValidator;
import com.aicareer.module.course.PromptGenerator;

public class ServicePrompt implements PromptGenerator {

  @Override
  public String generatePrompt(CourseRequest request){
    if (!SyntaxValidator.validate(request)) {
      throw new IllegalArgumentException("Validation failed. Cannot generate prompt.");
    }
    return "I want you to pretend to be an IT expert. "
      + "I'll provide you with all the necessary information about the job opening and information about student" + request + ", and your goal is to create a detailed weekly "
      + "training plan for the individual. "
      + "You should draw on your knowledge of career development and training. Using clear, simple, and understandable language in your answers will be helpful for people of all skill "
      + "levels. It's helpful to present tasks step by step and using bullet points. Try to avoid too much technical detail, but use it when necessary. "
      + "I want you to format your response as JSON with the keys: \"week_1\", \"week_2\", ..., \"week_10\".";
  }
}