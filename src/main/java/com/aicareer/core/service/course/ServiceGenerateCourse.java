package com.aicareer.core.service.course;

import com.aicareer.core.dto.courseDto.CourseRequest;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.repository.course.GenerateCourseFromGpt;

public class ServiceGenerateCourse implements GenerateCourseFromGpt {

  private final ServicePrompt servicePrompt;
  private final GigaChatService gigaChatClient;

  public ServiceGenerateCourse(ServicePrompt servicePrompt, GigaChatService gigaChatClient) {
    this.servicePrompt = servicePrompt;
    this.gigaChatClient = gigaChatClient;
  }

  @Override
  public String generateCoursePlan(CourseRequest request) {
    String prompt = servicePrompt.generatePrompt(request);
    return gigaChatClient.sendMessage(prompt);
  }
}