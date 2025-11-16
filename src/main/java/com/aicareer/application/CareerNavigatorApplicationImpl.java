// com.aicareer.application.CareerNavigatorApplicationImpl.java
package com.aicareer.application;

import com.aicareer.core.DTO.UserRegistrationDto;
import com.aicareer.core.DTO.courseDto.ResponseByWeek;
import com.aicareer.core.exception.*;
import com.aicareer.core.model.courseModel.Task;
import com.aicareer.core.model.courseModel.Week;
import com.aicareer.core.model.roadmap.RoadmapZone;
import com.aicareer.core.model.user.User;
import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.core.model.vacancy.FinalVacancyRequirements;
import com.aicareer.core.model.courseModel.CourseRequirements;
import com.aicareer.core.model.roadmap.Roadmap;
import com.aicareer.core.service.ParserOfVacancy.SelectVacancy;
import com.aicareer.core.service.information.ChatWithAiAfterDeterminingVacancyService;
import com.aicareer.core.service.information.ChatWithAiBeforeDeterminingVacancyService;
import com.aicareer.core.service.roadmap.RoadmapGenerateService;
import com.aicareer.core.service.user.RegistrationResult;
import com.aicareer.core.service.user.UserService;

import java.util.List;

public class CareerNavigatorApplicationImpl implements CareerNavigatorApplication {

  private final UserService userService;
  private final ChatWithAiBeforeDeterminingVacancyService chatBeforeVacancyService;
  private final SelectVacancy selectVacancy;
  private final ChatWithAiAfterDeterminingVacancyService chatAfterVacancyService;
  private final RoadmapGenerateService roadmapGenerateService;

  public CareerNavigatorApplicationImpl(
    UserService userService,
    ChatWithAiBeforeDeterminingVacancyService chatBeforeVacancyService,
    SelectVacancy selectVacancy,
    ChatWithAiAfterDeterminingVacancyService chatAfterVacancyService,
    RoadmapGenerateService roadmapGenerateService
  ) {
    this.userService = userService;
    this.chatBeforeVacancyService = chatBeforeVacancyService;
    this.selectVacancy = selectVacancy;
    this.chatAfterVacancyService = chatAfterVacancyService;
    this.roadmapGenerateService = roadmapGenerateService;
  }

  @Override
  public User authenticateOrRegister(String email, String password, String name)
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
      chatBeforeVacancyService.starDialogWithUser();
      chatBeforeVacancyService.askingStandardQuestions();
      return chatBeforeVacancyService.analyzeCombinedData();
    } catch (RuntimeException e) {
      throw new ChatException(
        ChatException.Type.MODEL_ERROR,
        "Ошибка при анализе данных пользователя через AI",
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

      // Возвращаем вашу строку — как и задумано
      return new FinalVacancyRequirements(
        "Java 11+, Spring Boot, опыт работы с REST API, 2+ года опыта, английский B1+"
      );
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
    if (vacancyRequirements == null || vacancyRequirements.getVacancyAllCompactRequirements() == null) {
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
  public Roadmap generateRoadmap(CourseRequirements courseRequirements)
    throws RoadmapGenerationException {
    if (courseRequirements == null) {
      throw new RoadmapGenerationException(
        RoadmapGenerationException.Type.MISSING_COURSE_DATA,
        "CourseRequirements не могут быть null"
      );
    }

    try {
      // Заглушка: создаём ResponseByWeek
      ResponseByWeek response = createTestResponseByWeek();

      // Вручную вызываем методы RoadmapGenerateService
      String weeksInfo = roadmapGenerateService.gettingWeeksInformation(response);
      String zonesAnalysis = roadmapGenerateService.informationComplexityAndQuantityAnalyzeAndCreatingZone(weeksInfo);
      List<RoadmapZone> zones = roadmapGenerateService.splittingWeeksIntoZones(zonesAnalysis, response.getWeeks());
      return roadmapGenerateService.identifyingThematicallySimilarZones(zones);

    } catch (Exception e) {
      throw new RoadmapGenerationException(
        RoadmapGenerationException.Type.INFRASTRUCTURE_ERROR,
        "Ошибка генерации дорожной карты",
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