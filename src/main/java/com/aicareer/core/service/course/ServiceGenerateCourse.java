package com.aicareer.core.service.course;

import com.aicareer.core.dto.courseDto.CourseRequest;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.repository.course.GenerateCourseFromGpt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceGenerateCourse implements GenerateCourseFromGpt {

  private final ServicePrompt servicePrompt;
  private final GigaChatService gigaChatClient;

  @Override
  public String generateCoursePlan(CourseRequest request) {
    log.info("🔧 Генерация курса для: " + request);

    try {
      String prompt = servicePrompt.generatePrompt(request);
      log.info("📝 Промт сгенерирован, длина: " + prompt.length());

      String response = gigaChatClient.sendMessage(prompt);
      log.info("✅ Ответ от GigaChat получен, длина: " + response.length());

      return response;
    } catch (Exception e) {
      log.error("❌ Ошибка при генерации курса: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Не удалось сгенерировать курс: " + e.getMessage(), e);
    }
  }
}