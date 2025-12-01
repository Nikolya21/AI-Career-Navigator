package com.aicareer.application;

import com.aicareer.core.dto.courseDto.CourseRequest;
import com.aicareer.core.dto.user.LoginRequestDto;
import com.aicareer.core.dto.user.UserRegistrationDto;
import com.aicareer.core.dto.courseDto.ResponseByWeek;
import com.aicareer.core.exception.*;
import com.aicareer.core.model.courseModel.Task;
import com.aicareer.core.model.courseModel.Week;
import com.aicareer.core.model.roadmap.RoadmapZone;
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

import java.io.File;
import java.util.List;
import java.util.Scanner;

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
  public Long register(String email, String password, String name)
      throws AuthenticationException {

    try {
      // Создаём DTO
      UserRegistrationDto dto = new UserRegistrationDto();
      dto.setEmail(email);
      dto.setPassword(password);
      dto.setName(name);

      // сохраняем user без доп данных
      RegistrationResult result = userService.registerUser(dto);
      User currentUser = result.getUser();
      Long userId = currentUser.getId();

      if (!result.isSuccess()) {
        throw new AuthenticationException(
            AuthenticationException.Type.USER_ALREADY_EXISTS,
            "Регистрация не удалась: " + String.join("; ", result.getErrors())
        );
      }

      // сохранение резюме (пока из локального файла)
      File cvFile;
      while (true) {
        System.out.println("\nВыберите вариант резюме:\n1 - PDF\n2 - DOCX\nВаш выбор: ");
        Scanner scanner = new Scanner(System.in);
        String choice = scanner.nextLine();
        if (choice.equals("1")) {
          cvFile = new File("TestCV.pdf");
          break;
        } else if (choice.equals("2")) {
          cvFile = new File("TestCV.docx");
          break;
        } else {
          System.out.println("❌ Неверный выбор. Попробуйте снова.");
        }
      }
      userService.uploadCV(cvFile, userId);

      UserPreferences userPreferences = handleUserPreferences(currentUser);
      if (userPreferences == null) return userId;

      FinalVacancyRequirements vacancyRequirements = handleVacancySelection(userPreferences);
      if (vacancyRequirements == null) return userId;

      CourseRequirements courseRequirements = handleCourseDefinition(vacancyRequirements);
      if (courseRequirements == null) return userId;

      System.out.println("\n📚 Передаём требования в генератор курса...");
      CourseRequest courseRequest = new CourseRequest(courseRequirements);
      ResponseByWeek responseByWeek = getLearningPlanAssembler().assemblePlan(courseRequest);
      System.out.println("✅ Курс сгенерирован: " + responseByWeek.getWeeks().size() + " недель");

      Roadmap roadmap = handleRoadmapGeneration(responseByWeek, currentUser);
      if (roadmap == null) return userId;
      userService.updateRoadmap(roadmap.getId(), userId);


      System.out.println("\n✅ УСПЕХ: полный цикл завершён!");

      return userId;
    } catch (Exception e) {
      throw new AuthenticationException(
          AuthenticationException.Type.ACCOUNT_LOCKED,
          "Системная ошибка при регистрации: " + e.getMessage(),
          e
      );
    }
  }

  public LearningPlanAssembler getLearningPlanAssembler() {
    return learningPlanAssembler;
  }
  @Override
  public Long authenticate(String email, String password)
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
        return result.getUser().getId();
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

      // Генерируем и сохраняем UserPreferences через ИИ
      UserPreferences userPreferences = chatBeforeVacancyService.analyzeCombinedData();

      if (userPreferences == null) {
        throw new ChatException(
            ChatException.Type.INVALID_RESPONSE_FORMAT,
            "AI не вернул данные о предпочтениях пользователя"
        );
      }

      userPreferences.setUserId(user.getId());

      UserPreferences savedPreferences = userPreferencesRepository.save(userPreferences);

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
        List<String> threeVacancies = selectVacancy.extractThreeVacancies(analysisResult, 0);
        if (threeVacancies.isEmpty()){
          threeVacancies = selectVacancy.extractThreeVacancies(analysisResult, 0);
        }
        System.out.println("✅ Извлечено вакансий: " + threeVacancies.size());

        // 2. Выбор вакансии (пока заглушка)
        SelectedPotentialVacancy selectedPotentialVacancy = selectVacancy.choosenVacansy(
            threeVacancies);
        System.out.println("✅ Выбрана вакансия: " + selectedPotentialVacancy.getNameOfVacancy());
        // сохранение вакансии
        userService.updateVacancy(selectedPotentialVacancy.getNameOfVacancy(),
            preferences.getUserId());

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
  public User getUserProfile(Long userId) {
    return userService.getUserProfile(userId);
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
  public Roadmap generateRoadmap(ResponseByWeek responseByWeek, User user)
      throws RoadmapGenerationException {
    if (responseByWeek == null) {
      throw new RoadmapGenerationException(
          RoadmapGenerationException.Type.MISSING_COURSE_DATA,
          "ResponseByWeek не могут быть null"
      );
    }

    try {
      // Вручную вызываем методы RoadmapGenerateService
      String weeksInfo = null;
      try {
        weeksInfo = roadmapGenerateService.gettingWeeksInformation(responseByWeek);
        System.out.println("✅ weeksInfo успешно получен: " + (weeksInfo != null ? weeksInfo.substring(0, Math.min(weeksInfo.length(), 100)) + "..." : "null"));
      } catch (Exception e) {
        System.out.println("❌ Ошибка в gettingWeeksInformation: " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException("Ошибка при получении информации о неделях", e);
      }

      String zonesAnalysis = null;
      try {
        zonesAnalysis = roadmapGenerateService.informationComplexityAndQuantityAnalyzeAndCreatingZone(weeksInfo);
        System.out.println("✅ zonesAnalysis успешно получен: " + (zonesAnalysis != null ? zonesAnalysis.substring(0, Math.min(zonesAnalysis.length(), 100)) + "..." : "null"));
      } catch (Exception e) {
        System.out.println("❌ Ошибка в informationComplexityAndQuantityAnalyzeAndCreatingZone: " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException("Ошибка при анализе сложности и создании зон", e);
      }

      List<RoadmapZone> zones = null;
      try {
        zones = roadmapGenerateService.splittingWeeksIntoZones(zonesAnalysis, responseByWeek.getWeeks());
        System.out.println("✅ zones успешно созданы, количество: " + (zones != null ? zones.size() : 0));
        if (zones != null) {
          for (int i = 0; i < zones.size(); i++) {
            System.out.println("Зона " + i + ": " + zones.get(i));
          }
        }
      } catch (Exception e) {
        System.out.println("❌ Ошибка в splittingWeeksIntoZones: " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException("Ошибка при разделении недель по зонам", e);
      }

      // Генерируем roadmap
      Roadmap generatedRoadmap = null;
      try {
        generatedRoadmap = roadmapGenerateService.identifyingThematicallySimilarZones(zones);
        generatedRoadmap.setUserId(user.getId());
        System.out.println("✅ Roadmap успешно сгенерирован: " + (generatedRoadmap != null ? generatedRoadmap.toString() : "null"));
      } catch (Exception e) {
        System.out.println("❌ Ошибка в identifyingThematicallySimilarZones: " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException("Ошибка при идентификации тематически схожих зон", e);
      }

      // ✅ СОХРАНЯЕМ в БД через RoadmapService
      Roadmap savedRoadmap = null;
      try {
        // Нужно установить userId (можно передавать через параметры или контекст)
        // generatedRoadmap.setUserId(userId);
        savedRoadmap = roadmapService.saveCompleteRoadmap(generatedRoadmap);
        System.out.println("✅ Roadmap успешно сохранен в БД с ID: " + (savedRoadmap != null ? savedRoadmap.getId() : "null"));

        // Возвращаем результат если все успешно
        return savedRoadmap;

      } catch (Exception e) {
        System.out.println("❌ Ошибка при сохранении roadmap в БД: " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException("Ошибка при сохранении roadmap в базу данных", e);
      }

    } catch (RuntimeException e) {
      // Перехватываем уже обернутые исключения
      System.out.println("💥 Критическая ошибка в процессе создания roadmap: " + e.getMessage());
      e.printStackTrace();
      throw e;
    } catch (Exception e) {
      // На всякий случай перехватываем все остальные исключения
      System.out.println("💥 Неожиданная ошибка в процессе создания roadmap: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Неожиданная ошибка при создании roadmap", e);
    } finally {
      // Блок finally выполнится в любом случае - успех или ошибка
      System.out.println("🔚 Завершение процесса создания roadmap");
      // Здесь можно добавить очистку ресурсов, если нужно
    }
  }

  /**
   * НОВЫЙ МЕТОД: Получить сохраненную roadmap пользователя
   */
  public Roadmap getSavedRoadmap(Long userId) throws RoadmapGenerationException {
    try {
      return roadmapService.findFullRoadmapById((roadmapService.findRoadmapByUserId(userId)).get().getId())
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

  private UserPreferences handleUserPreferences(User user) {
    System.out.println("\n💬 Цикл: Знакомство с пользователем (AI-чат)");
    String cvText = cvDataRepository.findByUserId(user.getId()).orElseThrow().getInformation();
    try {
      return gatherUserPreferences(user, cvText);
    } catch (Exception e) {
      System.err.println("❌ Ошибка в AI-знакомстве: " + e.getMessage());
      return null;
    }
  }

  private FinalVacancyRequirements handleVacancySelection(UserPreferences preferences) {
    System.out.println("\n🎯 Цикл: Подбор и анализ вакансии");
    try {
      return selectVacancy(preferences);
    } catch (Exception e) {
      System.err.println("❌ Ошибка при подборе вакансии: " + e.getMessage());
      return null;
    }
  }

  private CourseRequirements handleCourseDefinition(FinalVacancyRequirements vacancyRequirements) {
    System.out.println("\n🎓 Цикл: Формирование требований к курсу");
    try {
      return defineCourseRequirements(vacancyRequirements);
    } catch (Exception e) {
      System.err.println("❌ Ошибка при формировании CourseRequirements: " + e.getMessage());
      return null;
    }
  }

  private Roadmap handleRoadmapGeneration(ResponseByWeek responseByWeek, User user) {
    System.out.println("\n🗺️ Цикл: Генерация учебного плана и дорожной карты");
    try {
      return generateRoadmap(responseByWeek, user);
    } catch (Exception e) {
      System.err.println("❌ Ошибка при генерации Roadmap: " + e.getMessage());
      return null;
    }
  }
}