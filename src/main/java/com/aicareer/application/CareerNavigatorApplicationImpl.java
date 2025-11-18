package com.aicareer.application;

import com.aicareer.core.dto.user.LoginRequestDto;
import com.aicareer.core.dto.user.UserRegistrationDto;
import com.aicareer.core.dto.courseDto.ResponseByWeek;
import com.aicareer.core.exception.*;
import com.aicareer.core.model.courseModel.Task;
import com.aicareer.core.model.courseModel.Week;
import com.aicareer.core.model.roadmap.RoadmapZone;
import com.aicareer.core.model.user.CVData;
import com.aicareer.core.model.user.User;
import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.core.model.vacancy.FinalVacancyRequirements;
import com.aicareer.core.model.courseModel.CourseRequirements;
import com.aicareer.core.model.roadmap.Roadmap;
import com.aicareer.core.model.vacancy.SelectedPotentialVacancy;
import com.aicareer.core.service.course.LearningPlanAssembler;
import com.aicareer.core.service.parserOfVacancy.SelectVacancy;
import com.aicareer.core.service.information.ChatWithAiAfterDeterminingVacancyService;
import com.aicareer.core.service.information.ChatWithAiBeforeDeterminingVacancyService;
import com.aicareer.core.service.roadmap.RoadmapGenerateService;
import com.aicareer.core.service.roadmap.RoadmapService;
import com.aicareer.core.service.user.UserService;
import com.aicareer.core.service.user.model.AuthenticationResult;
import com.aicareer.core.service.user.model.RegistrationResult;
import com.aicareer.repository.user.CVDataRepository;
import com.aicareer.repository.user.UserPreferencesRepository;
import com.aicareer.repository.user.UserSkillsRepository;

import java.util.List;
import java.util.stream.Collectors;

public class CareerNavigatorApplicationImpl implements CareerNavigatorApplication {

  private final UserService userService;
  private final ChatWithAiBeforeDeterminingVacancyService chatBeforeVacancyService;
  private final SelectVacancy selectVacancy;
  private final ChatWithAiAfterDeterminingVacancyService chatAfterVacancyService;
  private final RoadmapGenerateService roadmapGenerateService;
  private final RoadmapService roadmapService;
  private final UserPreferencesRepository userPreferencesRepository;
  private final CVDataRepository cvDataRepository;
  private final LearningPlanAssembler learningPlanAssembler;
  private final UserSkillsRepository userSkillsRepository;

  public CareerNavigatorApplicationImpl(
      UserService userService,
      ChatWithAiBeforeDeterminingVacancyService chatBeforeVacancyService,
      SelectVacancy selectVacancy,
      ChatWithAiAfterDeterminingVacancyService chatAfterVacancyService,
      RoadmapGenerateService roadmapGenerateService,
      RoadmapService roadmapService, // ← ДОБАВИЛ
      UserPreferencesRepository userPreferencesRepository,
      CVDataRepository cvDataRepository, // ← ДОБАВИТЬ
      UserSkillsRepository userSkillsRepository, // ← ДОБАВИТЬ
      LearningPlanAssembler learningPlanAssembler
  ) {
    this.userService = userService;
    this.chatBeforeVacancyService = chatBeforeVacancyService;
    this.selectVacancy = selectVacancy;
    this.chatAfterVacancyService = chatAfterVacancyService;
    this.roadmapGenerateService = roadmapGenerateService;
    this.roadmapService = roadmapService; // ← ДОБАВИЛ
    this.userPreferencesRepository = userPreferencesRepository;
    this.cvDataRepository = cvDataRepository;
    this.userSkillsRepository = userSkillsRepository;
    this.learningPlanAssembler = learningPlanAssembler;
  }

  @Override
  public User register(String email, String password, String name)
      throws AuthenticationException {
    // Валидация
    if (email == null || email.trim().isEmpty()) {
      throw new AuthenticationException(
          AuthenticationException.Type.INVALID_EMAIL_FORMAT,
          "Email не может быть пустым"
      );
    }
    if (password == null || password.length() < 6) {
      throw new AuthenticationException(
          AuthenticationException.Type.WEAK_PASSWORD,
          "Пароль должен содержать минимум 6 символов"
      );
    }

    try {
      // Создаём DTO
      UserRegistrationDto dto = new UserRegistrationDto();
      dto.setEmail(email);
      dto.setPassword(password);
      dto.setName(name);

      // Вызываем UserService
      RegistrationResult result = userService.registerUser(dto);
      User user = result.getUser(); //todo cvData adding

      if (result.isSuccess()) {
        return result.getUser();
      } else {
        throw new AuthenticationException(
            AuthenticationException.Type.USER_ALREADY_EXISTS,
            "Регистрация не удалась: " + String.join("; ", result.getErrors())
        );
      }

    } catch (Exception e) {
      throw new AuthenticationException(
          AuthenticationException.Type.ACCOUNT_LOCKED,
          "Системная ошибка при регистрации: " + e.getMessage(),
          e
      );
    }
  }

  @Override
  public User authenticate(String email, String password)
      throws AuthenticationException {
    // Валидация для входа
    if (email == null || email.trim().isEmpty()) {
      throw new AuthenticationException(
          AuthenticationException.Type.INVALID_EMAIL_FORMAT,
          "Email не может быть пустым"
      );
    }
    if (password == null || password.length() < 6) {
      throw new AuthenticationException(
          AuthenticationException.Type.WEAK_PASSWORD,
          "Пароль должен содержать минимум 6 символов"
      );
    }

    try {
      // Создаём DTO для входа
      LoginRequestDto loginDto = new LoginRequestDto();
      loginDto.setEmail(email);
      loginDto.setPassword(password);

      // Вызываем UserService для аутентификации
      AuthenticationResult result = userService.authenticateUser(loginDto);

      if (result.isSuccess()) {
        return result.getUser();
      } else {
        throw new AuthenticationException(
            AuthenticationException.Type.USER_ALREADY_EXISTS,
            "Вход не удался: " + String.join("; ", result.getErrors())
        );
      }

    } catch (Exception e) {
      throw new AuthenticationException(
          AuthenticationException.Type.ACCOUNT_LOCKED,
          "Системная ошибка при входе: " + e.getMessage(),
          e
      );
    }
  }

  @Override
  public UserPreferences gatherUserPreferences(User user, String cvText) throws ChatException {
    if (user == null) {
      throw new IllegalArgumentException("User must not be null");
    }
    if (cvText == null || cvText.trim().isEmpty()) {
      throw new ChatException(
          ChatException.Type.INVALID_RESPONSE_FORMAT,
          "CV не может быть пустым"
      );
    }

    try {
      // ✅ ВАЖНО: Запускаем диалог с пользователем!
      chatBeforeVacancyService.starDialogWithUser();
      chatBeforeVacancyService.askingStandardQuestions();
      System.out.println("first");

      // 1. Сохраняем CVData
      CVData cvData = CVData.builder()
          .userId(user.getId())
          .information(cvText)
          .build();
      try {
        cvDataRepository.save(cvData);
        System.out.println("✅ CV data saved successfully");
      } catch (RuntimeException e) {
        System.err.println("❌ Error saving CV data: " + e.getMessage());
        e.printStackTrace(); // Добавляем stack trace для диагностики
        throw new ChatException(
            ChatException.Type.MODEL_ERROR,
            "Ошибка при сохранении данных CV: " + e.getMessage(),
            e
        );
      }
      System.out.println("second");

      // 2. Генерируем и сохраняем UserPreferences через ИИ
      UserPreferences userPreferences = chatBeforeVacancyService.analyzeCombinedData();
      System.out.println("third");

      if (userPreferences == null) {
        throw new ChatException(
            ChatException.Type.INVALID_RESPONSE_FORMAT,
            "AI не вернул данные о предпочтениях пользователя"
        );
      }

      userPreferences.setUserId(user.getId());
      System.out.println("fourth");

      UserPreferences savedPreferences = userPreferencesRepository.save(userPreferences);
      System.out.println("fifth");

      return savedPreferences;

    } catch (ChatException e) {
      // Пробрасываем уже созданные ChatException
      throw e;
    } catch (Exception e) {
      throw new ChatException(
          ChatException.Type.MODEL_ERROR,
          "Ошибка при анализе данных пользователя через AI: " + e.getMessage(),
          e
      );
    }
  }

  @Override
  public FinalVacancyRequirements selectVacancy(UserPreferences preferences)
      throws VacancySelectionException {
    if (preferences == null) {
      throw new VacancySelectionException(
          VacancySelectionException.Type.INVALID_PREFERENCES,
          "UserPreferences не могут быть null"
      );
    }

    try {
      String analysisResult = selectVacancy.analyzeUserPreference(preferences);
      if (analysisResult == null || analysisResult.trim().isEmpty()) {
        throw new VacancySelectionException(
            VacancySelectionException.Type.NO_VACANCIES_FOUND,
            "AI не вернул анализ предпочтений"
        );
      }
      try {
        System.out.println("🔍 Начало процесса подбора вакансий...");

        // 1. Извлечение трех вакансий
        List<String> threeVacancies = selectVacancy.extractThreeVacancies(analysisResult);
        System.out.println("✅ Извлечено вакансий: " + threeVacancies.size());

        // 2. Выбор вакансии (пока заглушка)
        SelectedPotentialVacancy selectedPotentialVacancy = selectVacancy.choosenVacansy(
            threeVacancies);
        System.out.println("✅ Выбрана вакансия: " + selectedPotentialVacancy.getNameOfVacancy());

        // 3. Парсинг вакансии
        String parsingResult = selectVacancy.formingByParsing(selectedPotentialVacancy);
        System.out.println("✅ Парсинг завершен, длина результатa: " + parsingResult.length());
        //System.out.println(parsingResult);

        // 4. Формирование финальных требований
        FinalVacancyRequirements finalVacancyRequirements = selectVacancy.formingFinalVacancyRequirements(
            parsingResult);
        System.out.println("✅ Финальные требования сформированы");

        return finalVacancyRequirements;
      } catch (NullPointerException e) {
        System.err.println("❌ Ошибка NullPointerException в процессе подбора вакансий:");
        System.err.println("   Возможные причины:");
        System.err.println("   - analysisResult = null");
        System.err.println("   - selectVacancy = null");
        System.err.println("   - selectedPotentialVacancy = null");
        e.printStackTrace();
        throw new RuntimeException("Ошибка инициализации данных для подбора вакансий", e);

      } catch (IllegalArgumentException e) {
        System.err.println("❌ Ошибка IllegalArgumentException в процессе подбора вакансий:");
        System.err.println("   Неверные параметры методов");
        e.printStackTrace();
        throw new RuntimeException("Некорректные параметры для обработки вакансий", e);

      } catch (IllegalStateException e) {
        System.err.println("❌ Ошибка IllegalStateException в процессе подбора вакансий:");
        System.err.println("   Некорректное состояние объекта selectVacancy");
        e.printStackTrace();
        throw new RuntimeException("Некорректное состояние системы для обработки вакансий", e);

      } catch (Exception e) {
        System.err.println("❌ Неожиданная ошибка в процессе подбора вакансий:");
        System.err.println("🔍 Детали ошибки:");
        System.err.println("   - Класс ошибки: " + e.getClass().getName());
        System.err.println("   - Сообщение: " + e.getMessage());
        System.err.println("📋 Контекст выполнения:");
        System.err.println(
            "   - Analysis Result length: " + (analysisResult != null ? analysisResult.length()
                : "null"));
        System.err.println(
            "   - SelectVacancy: " + (selectVacancy != null ? "initialized" : "null"));

        e.printStackTrace();

        throw new RuntimeException(
            "Критическая ошибка при формировании требований вакансии: " + e.getMessage(), e);
      }

    } catch (Exception e) {
      throw new VacancySelectionException(
          VacancySelectionException.Type.PARSING_FAILED,
          "Ошибка при подборе вакансии",
          e
      );
    }
  }

  @Override
  public CourseRequirements defineCourseRequirements(FinalVacancyRequirements vacancyRequirements)
      throws CourseDefinitionException {
    if (vacancyRequirements == null
        || vacancyRequirements.getVacancyAllCompactRequirements() == null) {
      throw new CourseDefinitionException(
          CourseDefinitionException.Type.INSUFFICIENT_DATA,
          "Требования вакансии не заданы"
      );
    }

    try {
      chatAfterVacancyService.askingPersonalizedQuestions(
          chatAfterVacancyService.generatePersonalizedQuestions(vacancyRequirements)
      );
      return chatAfterVacancyService.analyzeCombinedData(vacancyRequirements);
    } catch (Exception e) {
      throw new CourseDefinitionException(
          CourseDefinitionException.Type.COURSE_GENERATION_FAILED,
          "Не удалось сформировать требования к курсу",
          e
      );
    }
  }

  @Override
  public Roadmap generateRoadmap(CourseRequirements courseRequirements, User user)
      throws RoadmapGenerationException {
    if (courseRequirements == null) {
      throw new RoadmapGenerationException(
          RoadmapGenerationException.Type.MISSING_COURSE_DATA,
          "CourseRequirements не могут быть null"
      );
    }


      ResponseByWeek response = createTestResponseByWeek();
      System.out.println("✅ Тестовый ResponseByWeek создан");

    String weeksInfo;
    try {
      // Вручную вызываем методы RoadmapGenerateService
      weeksInfo = roadmapGenerateService.gettingWeeksInformation(response);
      System.out.println(
          "✅ Информация о неделях получена, длина: " + (weeksInfo != null ? weeksInfo.length()
              : "null"));

    } catch (Exception e) {
      System.err.println("❌ Ошибка в gettingWeeksInformation:");
      System.err.println("   Response: " + (response != null ? response.toString() : "null"));
      e.printStackTrace();
      throw new RuntimeException("Ошибка получения информации о неделях: " + e.getMessage(), e);
    }

    String zonesAnalysis;
    try {
      zonesAnalysis = roadmapGenerateService.informationComplexityAndQuantityAnalyzeAndCreatingZone(
          weeksInfo);
      System.out.println(
          "✅ Анализ сложности и создание зон завершен, длина результата: " + (zonesAnalysis != null
              ? zonesAnalysis.length() : "null"));

    } catch (Exception e) {
      System.err.println("❌ Ошибка в informationComplexityAndQuantityAnalyzeAndCreatingZone:");
      System.err.println("   Weeks Info: " + (weeksInfo != null ?
          weeksInfo.substring(0, Math.min(100, weeksInfo.length())) + "..." : "null"));
      e.printStackTrace();
      throw new RuntimeException("Ошибка анализа сложности и создания зон: " + e.getMessage(), e);
    }

    List<RoadmapZone> zones;
    try {
      zones = roadmapGenerateService.splittingWeeksIntoZones(zonesAnalysis, response.getWeeks());
      System.out.println(
          "✅ Недели разделены на зоны, количество зон: " + (zones != null ? zones.size() : "null"));

    } catch (Exception e) {
      System.err.println("❌ Ошибка в splittingWeeksIntoZones:");
      System.err.println("   Zones Analysis: " + (zonesAnalysis != null ?
          zonesAnalysis.substring(0, Math.min(100, zonesAnalysis.length())) + "..." : "null"));
      System.err.println("   Weeks count: " + (response != null && response.getWeeks() != null
          ? response.getWeeks().size() : "null"));
      e.printStackTrace();
      throw new RuntimeException("Ошибка разделения недель на зоны: " + e.getMessage(), e);
    }

    Roadmap generatedRoadmap;
    try {
      // Генерируем roadmap
      generatedRoadmap = roadmapGenerateService.identifyingThematicallySimilarZones(zones);
      System.out.println(
          "✅ Roadmap сгенерирован, ID: " + (generatedRoadmap != null ? generatedRoadmap.getId()
              : "null"));
    } catch (Exception e) {
      System.err.println("❌ Ошибка в identifyingThematicallySimilarZones:");
      System.err.println("   Zones count: " + (zones != null ? zones.size() : "null"));
      System.err.println("   Zones: " + (zones != null ? zones.stream().map(RoadmapZone::getName)
          .collect(Collectors.toList()) : "null"));
      e.printStackTrace();
      throw new RuntimeException("Ошибка идентификации тематически схожих зон: " + e.getMessage(),
          e);
    }

    Roadmap savedRoadmap;
    try {
      // ✅ СОХРАНЯЕМ в БД через RoadmapService
      // Нужно установить userId (можно передавать через параметры или контекст)
      generatedRoadmap.setUserId(user.getId());
      savedRoadmap = roadmapService.saveCompleteRoadmap(generatedRoadmap);
      System.out.println(
          "✅ Roadmap сохранен в БД, ID: " + (savedRoadmap != null ? savedRoadmap.getId() : "null"));
      System.out.println("🎉 Процесс генерации и сохранения roadmap успешно завершен!");
      return savedRoadmap;

    } catch (Exception e) {
      System.err.println("❌ Ошибка при сохранении roadmap в БД:");
      System.err.println(
          "   Generated Roadmap: " + (generatedRoadmap != null ? generatedRoadmap.toString()
              : "null"));
      System.err.println(
          "   Roadmap ID: " + (generatedRoadmap != null ? generatedRoadmap.getId() : "null"));
      e.printStackTrace();
      throw new RuntimeException("Ошибка сохранения roadmap в базу данных: " + e.getMessage(), e);
    }
  }


  /**
   * НОВЫЙ МЕТОД: Получить сохраненную roadmap пользователя
   */
  public Roadmap getSavedRoadmap(Long userId) throws RoadmapGenerationException {
    try {
      return roadmapService.findRoadmapByUserId(userId)
              .orElseThrow(() -> new RoadmapGenerationException(
                      RoadmapGenerationException.Type.MISSING_COURSE_DATA,
                      "Roadmap не найдена для пользователя: " + userId
              ));
    } catch (Exception e) {
      throw new RoadmapGenerationException(
              RoadmapGenerationException.Type.INFRASTRUCTURE_ERROR,
              "Ошибка при получении roadmap",
              e
      );
    }
  }

  private ResponseByWeek createTestResponseByWeek() {
    // === Week 1 ===
    Task task1 = new Task();
    task1.setDescription("Изучить базовый синтаксис Java");
    task1.setUrls(List.of(
            "https://docs.oracle.com/javase/tutorial/",
            "https://learnjavaonline.org/"
    ));

    Task task2 = new Task();
    task2.setDescription("Установить IntelliJ IDEA и настроить проект");
    task2.setUrls(List.of(
            "https://www.jetbrains.com/idea/download/",
            "https://www.jetbrains.com/help/idea/creating-and-running-your-first-java-application.html"
    ));

    Week week1 = new Week();
    week1.setNumber(1);
    week1.setGoal("Освоить основы Java и настроить окружение");
    week1.setTasks(List.of(task1, task2));

    // === Week 2 ===
    Task task3 = new Task();
    task3.setDescription("Изучить основы Spring Boot: создать REST-контроллер");
    task3.setUrls(List.of(
            "https://spring.io/guides/gs/spring-boot/",
            "https://www.baeldung.com/spring-boot-rest"
    ));

    Task task4 = new Task();
    task4.setDescription("Работа с аннотациями @RestController, @GetMapping");
    task4.setUrls(List.of(
            "https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/RestController.html"
    ));

    Week week2 = new Week();
    week2.setNumber(2);
    week2.setGoal("Создать первый Spring Boot REST API");
    week2.setTasks(List.of(task3, task4));

    // === Week 3 ===
    Task task5 = new Task();
    task5.setDescription("Подключить базу данных (H2/PostgreSQL) через Spring Data JPA");
    task5.setUrls(List.of(
            "https://spring.io/guides/gs/accessing-data-jpa/",
            "https://www.baeldung.com/spring-boot-jpa"
    ));

    Week week3 = new Week();
    week3.setNumber(3);
    week3.setGoal("Работа с базой данных через JPA");
    week3.setTasks(List.of(task5));

    // === Собираем ResponseByWeek ===
    ResponseByWeek response = new ResponseByWeek();
    response.setWeeks(List.of(week1, week2, week3));
    return response;
  }
}