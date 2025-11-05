package com.aicareer;
import chat.giga.client.GigaChatClient;
import com.aicareer.core.DTO.CourseRequest;
import com.aicareer.core.DTO.ResponseByWeek;
import com.aicareer.core.config.GigaChatConfig;
import com.aicareer.core.service.*;
import com.aicareer.core.Validator.LlmResponseValidator;
import com.aicareer.core.service.course.*;
import com.aicareer.module.course.AssemblePlan;
import com.aicareer.module.course.CourseResponse;
import com.aicareer.module.course.DistributionByWeek;
import com.aicareer.module.course.GenerateCourseFromGpt;

public class Main {
  public static void main(String[] args) {
    try {
      GigaChatConfig config = new GigaChatConfig(
        "ваш-client-id",
        "ваш-client-secret",
        "GIGACHAT_API_PUB"
      );
      GigaChatClient gigaClient = new GigaChatClient(config);
      ServicePrompt promptService = new ServicePrompt();
      GenerateCourseFromGpt generator = new ServiceGenerateCourse(promptService, gigaClient);
      CourseResponse parser = new ServiceWeek();
      DistributionByWeek distributor = new WeekDistributionService();

      AssemblePlan assembler = new LearningPlanAssembler(generator, parser, distributor);
      String requirements = """
                Требования к курсу: Full-stack разработчик от Middle до Senior.
                Модуль 1: Продвинутый Frontend на React.
                Модуль 2: Интеграция фронтенда и бэкенда.
                Длительность: 8 недель.
                Занятость: 10 часов в неделю.
                """;

      CourseRequest request = new CourseRequest(requirements);
      ResponseByWeek response = assembler.assemblePlan(request);

      System.out.println("План успешно сгенерирован и распарсен!");
      System.out.println("Всего недель: " + response.getWeeks().size());

      for (var week : response.getWeeks()) {
        System.out.println("\n== Неделя " + week.getNumber() + " ==");
        System.out.println("Цель: " + week.getGoal());
        for (int i = 0; i < week.getTasks().size(); i++) {
          var task = week.getTasks().get(i);
          System.out.println("Задача " + (i + 1) + ": " + task.getDescription());
          System.out.println("Ресурсы: " + String.join(", ", task.getUrls()));
        }
      }
      String rawResponse = generator.generateCoursePlan(request);
      if (LlmResponseValidator.validate(rawResponse)) {
        System.out.println("Ответ прошёл валидацию!");
      } else {
        System.out.println("Валидация провалена!");
      }

    } catch (Exception e) {
      System.err.println("Ошибка при генерации плана:");
      e.printStackTrace();
    }
  }
}