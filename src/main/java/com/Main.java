package com;

import com.aicareer.core.config.GigaChatConfig;
import com.aicareer.core.DTO.courseDto.CourseRequest;
import com.aicareer.core.DTO.courseDto.ResponseByWeek;
import com.aicareer.core.model.courseModel.Week;
import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.core.model.vacancy.SelectedPotentialVacancy;
import com.aicareer.core.service.ParserOfVacancy.SelectVacancy;
import com.aicareer.core.service.course.*;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.Validator.LlmResponseValidator;

import java.util.List;
import java.util.Scanner;

// Используем логгер (как во втором Main)
public class Main {

  public static void main(String[] args) {
    System.out.println("🚀 AI-Career Navigator: Полный цикл — от анализа до учебного плана");
    System.out.println("===============================================================");

    Scanner scanner = new Scanner(System.in);
    try {
      //  ЭТАП 1: АНАЛИЗ ВАКАНСИИ 
      System.out.println("\n[ЭТАП 1] Подбор и парсинг вакансии");
      System.out.println("---------------------------------");

      // 🔹 Обязательно: создать userinfo
      // Предполагаем, что UserPreferences имеет конструктор по умолчанию
      UserPreferences userinfo = new UserPreferences();

      SelectVacancy selectVacancy = new SelectVacancy();

      System.out.println("🔍 Анализ предпочтений пользователя...");
      String analysisResult = selectVacancy.analyzeUserPreference(userinfo);
      System.out.println("✅ Анализ завершён");

      System.out.println("🎯 Выбор 3-х вакансий...");
      List<String> suggestedVacancies = selectVacancy.extractThreeVacancies(analysisResult);

      System.out.println("📌 Выбор одной вакансии...");
      // ❗ Исправлено: choosenVacansy → chooseVacancy (опечатка)
      // Если в вашей реализации метод называется именно choosenVacansy — верните как есть
      SelectedPotentialVacancy selectedVacancy = selectVacancy.choosenVacansy(suggestedVacancies);

      System.out.println("🌐 Парсинг вакансий с HH.ru...");
      // ❗ Исправлено: FormingByParsing → formingByParsing (camelCase)
      String parsingResults = selectVacancy.FormingByParsing(selectedVacancy);
      System.out.println("✅ Парсинг завершён");

      System.out.println("📝 Формирование финальных требований...");
      // ❗ Исправлено: FormingFinalVacancyRequirements → formingFinalVacancyRequirements
      String finalRequirements = selectVacancy.FormingFinalVacancyRequirements(parsingResults);
      System.out.println("✅ Финальные требования готовы");
      System.out.println("\n📄 Результат этапа 1:\n" + finalRequirements);
      System.out.println("---------------------------------------------------------------");

      //  ЭТАП 2: ГЕНЕРАЦИЯ УЧЕБНОГО ПЛАНА
      System.out.println("\n[ЭТАП 2] Генерация персонализированного учебного плана");
      System.out.println("---------------------------------------------------------");

      // 1. Конфигурация
      GigaChatConfig config;
      try {
        config = new GigaChatConfig();
        System.out.println("✅ GigaChatConfig загружен");
      } catch (Exception e) {
        System.err.println("❌ Ошибка загрузки конфигурации. Проверьте переменные окружения:");
        System.err.println("   GIGACHAT_CLIENT_ID");
        System.err.println("   GIGACHAT_CLIENT_SECRET");
        System.err.println("   GIGACHAT_SCOPE");
        return;
      }

      // 2. Ручной DI (как во втором Main)
      GigaChatService gigaChatService = new GigaChatService(config);
      ServicePrompt promptService = new ServicePrompt();
      ServiceGenerateCourse courseGenerator = new ServiceGenerateCourse(promptService, gigaChatService);
      ServiceWeek parser = new ServiceWeek();
      WeekDistributionService distributor = new WeekDistributionService();

      LearningPlanAssembler assembler = new LearningPlanAssembler(
        courseGenerator,
        parser,
        distributor
      );

      // 3. Подготовка запроса — используем finalRequirements из этапа 1!
      CourseRequest courseRequest = new CourseRequest(finalRequirements);
      System.out.println("📥 Входные данные для генерации:\n" + courseRequest.getCourseRequirements());

      // 4. Генерация
      System.out.println("🧠 Генерация учебного плана через GigaChat...");
      ResponseByWeek response = assembler.assemblePlan(courseRequest);
      List<Week> weeks = response.getWeeks();

      // 5. Валидация (как во втором Main)
      System.out.println("🔍 Валидация структуры ответа...");
      String rawResponse = simulateLlmRawResponse(weeks);
      if (!LlmResponseValidator.validate(rawResponse)) {
        System.err.println("❌ Валидация провалена. План отклонён.");
        return;
      }
      System.out.println("✅ Валидация пройдена");

      // 6. Вывод
      System.out.println("\n🎓 Сгенерированный учебный план (" + weeks.size() + " недель):");
      System.out.println("==================================================");
      for (Week week : weeks) {
        System.out.println("▸ Неделя " + week.getNumber() + ": " + week.getGoal());
        week.getTasks().forEach(task -> {
          System.out.println("    • " + task.getDescription());
          task.getUrls().forEach(url -> System.out.println("        🔗 " + url));
        });
        System.out.println();
      }

      System.out.println("🎉 УСПЕХ: полный цикл завершён!");

    } catch (Exception e) {
      System.err.println("💥 КРИТИЧЕСКАЯ ОШИБКА: " + e.getMessage());
      e.printStackTrace();
    } finally {
      scanner.close();
    }
  }

  //  ВСПОМОГАТЕЛЬНЫЙ МЕТОД из второго Main (без изменений)
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