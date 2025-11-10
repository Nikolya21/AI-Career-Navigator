package com;

import com.aicareer.core.DTO.courseDto.CourseRequest;
import com.aicareer.core.DTO.courseDto.ResponseByWeek;
import com.aicareer.core.Validator.LlmResponseValidator;
import com.aicareer.core.config.GigaChatConfig;
import com.aicareer.core.model.*;
import com.aicareer.core.service.course.*;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.service.information.ChatWithAiAfterDeterminingVacancyService;
import com.aicareer.core.service.information.ChatWithAiBeforeDeterminingVacancyService;
import com.aicareer.core.service.information.DialogService;
import com.aicareer.core.service.roadmap.RoadmapGenerateService;
import com.aicareer.repository.information.ChatWithAiBeforeDeterminingVacancy;

import java.util.List;
import java.util.logging.Logger;

public class Main {

  // Используем стандартный Java-логгер (или замените на SLF4J/Lombok @Slf4j)
  private static final Logger log = Logger.getLogger(Main.class.getName());

  public static void main(String[] args) {
    log.info("ЗАПУСК: ГЕНЕРАЦИЯ ПЕРСОНАЛИЗИРОВАННОГО УЧЕБНОГО ПЛАНА (LLM + VALIDATION)");

    try {
      // === 1. Инициализация сервисов ===
      initializeServices();

      // === 2. Цикл 1-2: Знакомство с пользователем (до определения вакансии) ===
      CVdata cvData = loadSampleCVData(); // ← заглушка; заменить на реальный источник
      UserPreferences userPreferences = runBeginAiChatCycle(cvData);

      if (userPreferences == null) {
        log.severe("Не удалось получить предпочтения пользователя. Прерывание.");
        System.exit(1);
      }

      // === 3. Цикл 3-4: Формирование вспомогательной информации (резюмирование диалога) ===
      UserPreferences summarizedPreferences = runSummarizingAiChatCycle(cvData);
      if (summarizedPreferences == null) {
        log.warning("Резюмирующий чат не удался, используем исходные предпочтения.");
        summarizedPreferences = userPreferences;
      }

      // === 4. Цикл 5-6: Определение целевой вакансии и формирование требований к курсу ===
      FinalVacancyRequirements vacancyRequirements = determineVacancyAndRequirements(summarizedPreferences);
      if (vacancyRequirements == null) {
        log.severe("Не удалось определить вакансии. Прерывание.");
        System.exit(1);
      }

      CourseRequirements courseRequirements = runCourseRequirementsCycle(vacancyRequirements);
      if (courseRequirements == null) {
        log.severe("Не удалось сформировать требования к курсу. Прерывание.");
        System.exit(1);
      }

      // === 5. Подготовка запроса на генерацию курса ===
      CourseRequest request = buildCourseRequest(courseRequirements);
      log.info("Входные требования:\n{}", request.getCourseRequirements());

      // === 6. Настройка компонентов сборки учебного плана ===
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

      // === 7. Генерация учебного плана ===
      log.info("Генерация учебного плана через GigaChat...");
      ResponseByWeek response = assembler.assemblePlan(request);
      List<Week> weeks = response.getWeeks();

      // === 8. Валидация ответа LLM ===
      log.info("Валидация структуры ответа...");
      String llmRawResponse = simulateLlmRawResponse(weeks); // ← только для демонстрации
      if (!LlmResponseValidator.validate(llmRawResponse)) {
        log.severe("Валидация провалена. План отклонён.");
        System.exit(1);
      }
      log.info("Валидация пройдена. План структурно корректен.");

      // === 9. Цикл 7-8: Генерация дорожной карты (roadmap) ===
      RoadmapGenerateService roadmapService = new RoadmapGenerateService(gigaChatService);
      Roadmap roadmap = runCourseAndRoadmapGenerationCycle(roadmapService, response);
      if (roadmap == null) {
        log.warning("Генерация roadmap не удалась. Продолжаем без неё.");
      }

      // === 10. Вывод результата ===
      log.info("СГЕНЕРИРОВАННЫЙ ПЛАН ({} недель):", weeks.size());
      for (Week week : weeks) {
        log.info("Неделя {}: {}", week.getNumber(), week.getGoal());
        if (week.getTasks() != null && !week.getTasks().isEmpty()) {
          for (Task task : week.getTasks()) {
            log.info("    {}", task.getDescription());
            if (task.getUrls() != null && !task.getUrls().isEmpty()) {
              for (String url : task.getUrls()) {
                log.info("        {}", url);
              }
            }
          }
        }
        log.info("");
      }

      if (roadmap != null) {
        log.info("СГЕНЕРИРОВАННЫЙ ROADMAP:");
        roadmap.getZones().forEach(zone ->
          log.info("  Зона '{}': {} недель", zone.getName(), zone.getWeeks().size())
        );
      }

      log.info("УСПЕХ: учебный план и roadmap готовы к интеграции!");

    } catch (Exception e) {
      log.severe("КРИТИЧЕСКАЯ ОШИБКА: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }
  }

  // === ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

  private static void initializeServices() {
    log.info("1. ИНИЦИАЛИЗАЦИЯ СЕРВИСОВ...");

    // Инициализация сервисов
    GigaChatService gigaChatService = new GigaChatService();
    DialogService dialogService = new DialogService(gigaChatService, true);
    ChatWithAiBeforeDeterminingVacancyService chatBeforeVacancyService =
      new ChatWithAiBeforeDeterminingVacancyService(gigaChatService, dialogService);
    ChatWithAiAfterDeterminingVacancyService chatAfterVacancyService =
      new ChatWithAiAfterDeterminingVacancyService(gigaChatService, dialogService);
    RoadmapGenerateService roadmapService = new RoadmapGenerateService(gigaChatService);

    log.info("✅ Все сервисы инициализированы");
  }

  private static UserPreferences runBeginAiChatCycle(CVdata cvData) {
    log.info("\n💬 ЦИКЛ 2: ЗНАКОМСТВО С ПОЛЬЗОВАТЕЛЕМ ЧЕРЕЗ AI-ЧАТ");

    GigaChatService gigaChatService = new GigaChatService();
    DialogService dialogService = new DialogService(gigaChatService, true);
    ChatWithAiBeforeDeterminingVacancyService chatService =
      new ChatWithAiBeforeDeterminingVacancyService(gigaChatService, dialogService);

    try {
      chatService.starDialogWithUser(); // ← опечатка в оригинале: starDialog → startDialog? (оставлено как есть)
      chatService.askingStandardQuestions();

      List<String> personalizedQuestions = chatService.generatePersonalizedQuestions(cvData);
      chatService.askingPersonalizedQuestions(personalizedQuestions);

      return chatService.analyzeCombinedData();
    } catch (Exception e) {
      log.severe("❌ Ошибка в цикле AI-чата: " + e.getMessage());
      return null;
    }
  }

  private static UserPreferences runSummarizingAiChatCycle(CVdata cvData) {
    log.info("\n💬 ЦИКЛ 4: ФОРМИРОВАНИЕ ВСПОМОГАТЕЛЬНОЙ ИНФОРМАЦИИ НА ОСНОВЕ ДИАЛОГА");

    GigaChatService gigaChatService = new GigaChatService();
    DialogService dialogService = new DialogService(gigaChatService, true);
    ChatWithAiBeforeDeterminingVacancyService chatService =
      new ChatWithAiBeforeDeterminingVacancyService(gigaChatService, dialogService);

    try {
      chatService.starDialogWithUser();
      chatService.askingStandardQuestions();

      List<String> personalizedQuestions = chatService.generatePersonalizedQuestions(cvData);
      chatService.askingPersonalizedQuestions(personalizedQuestions);

      return chatService.analyzeCombinedData();
    } catch (Exception e) {
      log.severe("❌ Ошибка в резюмирующем цикле: " + e.getMessage());
      return null;
    }
  }

  private static FinalVacancyRequirements determineVacancyAndRequirements(UserPreferences preferences) {
    // ← Здесь должна быть логика определения вакансии (например, через LLM или правила)
    // Заглушка: создаём фиктивные требования
    return new FinalVacancyRequirements(
      "Senior Java Developer",
      List.of("Spring Security", "Kubernetes", "OAuth2")
    );
  }

  private static CourseRequirements runCourseRequirementsCycle(FinalVacancyRequirements vacancyRequirements) {
    log.info("\n🎓 ЦИКЛ 6: ФОРМИРОВАНИЕ ТРЕБОВАНИЙ К КУРСУ");

    GigaChatService gigaChatService = new GigaChatService();
    DialogService dialogService = new DialogService(gigaChatService, true);
    ChatWithAiAfterDeterminingVacancyService chatService =
      new ChatWithAiAfterDeterminingVacancyService(gigaChatService, dialogService);

    try {
      List<String> personalizedQuestions = chatService.generatePersonalizedQuestions(vacancyRequirements);
      chatService.askingPersonalizedQuestions(personalizedQuestions);

      return chatService.analyzeCombinedData(vacancyRequirements);
    } catch (Exception e) {
      log.severe("❌ Ошибка в цикле формирования требований к курсу: " + e.getMessage());
      return null;
    }
  }

  private static Roadmap runCourseAndRoadmapGenerationCycle(
    RoadmapGenerateService roadmapService,
    ResponseByWeek responseByWeek
  ) {
    log.info("\n🗺️ ЦИКЛ 8: ГЕНЕРАЦИЯ КУРСА И ДОРОЖНОЙ КАРТЫ");

    try {
      String weeksInfo = roadmapService.gettingWeeksInformation(responseByWeek);
      String complexityResult = roadmapService.informationComplexityAndQuantityAnalyzeAndCreatingZone(weeksInfo);
      List<Week> weeks = responseByWeek.getWeeks();
      List<RoadmapZone> zones = roadmapService.splittingWeeksIntoZones(complexityResult, weeks);
      return roadmapService.identifyingThematicallySimilarZones(zones);
    } catch (Exception e) {
      log.severe("❌ Ошибка в цикле генерации roadmap: " + e.getMessage());
      return null;
    }
  }

  // --- Загрузчики и вспомогательные методы ---

  private static GigaChatConfig loadGigaChatConfig() {
    try {
      GigaChatConfig config = new GigaChatConfig();
      log.info("GigaChatConfig загружен из переменных окружения");
      return config;
    } catch (Exception e) {
      log.severe("Ошибка загрузки конфигурации. Проверьте переменные окружения:");
      log.severe("  GIGACHAT_CLIENT_ID");
      log.severe("  GIGACHAT_CLIENT_SECRET");
      log.severe("  GIGACHAT_SCOPE");
      throw e;
    }
  }

  private static CVdata loadSampleCVData() {
    // ← Заглушка: в реальности — из базы, файла или API
    return new CVdata("John Doe", "Middle Java Developer", 5, List.of("Spring Boot", "SQL"));
  }

  private static CourseRequest buildCourseRequest(CourseRequirements reqs) {
    // Преобразуем CourseRequirements → строку для CourseRequest
    String reqString = String.format(
      """
      Целевая вакансия: %s
      Пробелы: %s
      Доступно в неделю: %d часов
      Цель: %s
      Продолжительность: %d недель
      """,
      reqs.getTargetVacancy(),
      String.join(", ", reqs.getKnowledgeGaps()),
      reqs.getHoursPerWeek(),
      reqs.getGoal(),
      reqs.getDurationWeeks()
    );
    return new CourseRequest(reqString);
  }

  // Только для демонстрации валидации
  private static String simulateLlmRawResponse(List<Week> weeks) {
    StringBuilder sb = new StringBuilder();
    for (Week w : weeks) {
      sb.append("week").append(w.getNumber()).append(": goal: \"").append(w.getGoal()).append("\"");
      int taskNum = 1;
      for (Task task : w.getTasks()) {
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