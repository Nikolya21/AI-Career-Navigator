package com;

import com.aicareer.core.config.GigaChatConfig;
import com.aicareer.core.DTO.CourseRequest;
import com.aicareer.core.DTO.ResponseByWeek;
import com.aicareer.core.model.Week;
import com.aicareer.core.service.course.*;
import com.aicareer.core.Validator.LlmResponseValidator;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.repository.course.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class Main {

  public static void main(String[] args) {
    log.info("ЗАПУСК: ГЕНЕРАЦИЯ ПЕРСОНАЛИЗИРОВАННОГО УЧЕБНОГО ПЛАНА (LLM + VALIDATION)");

    try {
      //  1. Конфигурация
      GigaChatConfig config = loadGigaChatConfig();

      // 2. Создание компонентов (ручной DI)
      GigaChatService gigaChatService = new GigaChatService();

      ServicePrompt promptService = new ServicePrompt();
      ServiceGenerateCourse courseGenerator = new ServiceGenerateCourse(promptService, gigaChatService);

      ServiceWeek parser = new ServiceWeek();
      WeekDistributionService distributor = new WeekDistributionService();

      LearningPlanAssembler assembler = new LearningPlanAssembler(
        courseGenerator,
        parser,
        distributor
      );

      //  3. Подготовка входных данных
      CourseRequest request = buildSampleRequest();
      log.info("  Входные требования:\n{}", request.getCourseRequirements());

      //  4. Генерация и сборка плана
      log.info("Генерация учебного плана через GigaChat...");
      ResponseByWeek response = assembler.assemblePlan(request);

      //  5. Валидация ответа
      log.info(" Валидация структуры ответа...");
      List<Week> weeks = response.getWeeks();
      String llmRawResponse = simulateLlmRawResponse(weeks); // ← демонстрации; в реале парсер получает raw-строку

      if (!LlmResponseValidator.validate(llmRawResponse)) {
        log.error("Валидация провалена. План отклонён.");
        System.exit(1);
      }
      log.info("Валидация пройдена. План структурно корректен.");

      //  6. Вывод результата
      log.info("СГЕНЕРИРОВАННЫЙ ПЛАН ({} недель):", weeks.size());
      weeks.forEach(week -> {
        log.info("Неделя {}: {}", week.getNumber(), week.getGoal());
        if (week.getTasks() != null && !week.getTasks().isEmpty()) {
          week.getTasks().forEach(task -> {
            log.info("    {}", task.getDescription());
            if (task.getUrls() != null && !task.getUrls().isEmpty()) {
              task.getUrls().forEach(url -> log.info("        {}", url));
            }
          });
        }
        log.info("");
      });

      log.info("УСПЕХ: учебный план готов к интеграции в Roadmap!");

    } catch (Exception e) {
      log.error("КРИТИЧЕСКАЯ ОШИБКА", e);
      System.exit(1);
    }
  }

  // вспомогательные методы

  private static GigaChatConfig loadGigaChatConfig() {
    try {
      GigaChatConfig config = new GigaChatConfig();
      log.info("GigaChatConfig загружен из переменных окружения");
      return config;
    } catch (Exception e) {
      log.error("    Ошибка загрузки конфигурации. Проверьте переменные окружения:");
      log.error("    GIGACHAT_CLIENT_ID");
      log.error("    GIGACHAT_CLIENT_SECRET");
      log.error("    GIGACHAT_SCOPE");
      throw e;
    }
  }

  private static CourseRequest buildSampleRequest() {
    //  В реальном приложении — это приходит из UI / API / файла
    String requirements = """
            Целевая вакансия: Senior Java Developer
            Текущий уровень: Middle, 5 лет опыта
            Пробелы: Spring Security, микросервисы, Kubernetes
            Доступно в неделю: 6 часов
            Страхи: не понимаю OAuth2, боюсь production-деплоя
            Цель: за 8 недель закрыть пробелы и пройти собеседование в Сбер/Тинькофф
            Продолжительность: 8 недель
            """;
    return new CourseRequest(requirements);
  }
  private static String simulateLlmRawResponse(List<Week> weeks) {
    StringBuilder sb = new StringBuilder();
    for (Week w : weeks) {
      sb.append("week").append(w.getNumber()).append(": ");
      sb.append("goal: \"").append(w.getGoal()).append("\"");
      int taskNum = 1;
      for (var task : w.getTasks()) {
        sb.append(". task").append(taskNum).append(": \"").append(task.getDescription()).append("\"");
        if (task.getUrls() != null && !task.getUrls().isEmpty()) {
          String urls = String.join(", ", task.getUrls());
          sb.append(". urls: \"").append(urls).append("\"");
        }
        taskNum++;
      }
      sb.append("\n");
    }
    return sb.toString().trim();
  }
}