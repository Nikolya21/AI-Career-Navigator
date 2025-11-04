// com.aicareer.core.service.course.ServiceGenerateCourse
package com.aicareer.core.service.course;

import com.aicareer.core.DTO.CourseRequest;
import com.aicareer.module.course.GenerateCourseFromGpt;

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