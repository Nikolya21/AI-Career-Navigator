package com.aicareer;

import com.aicareer.core.DTO.user.*;
import com.aicareer.core.Validator.LlmResponseValidator;
import com.aicareer.core.config.GigaChatConfig;
import com.aicareer.core.DTO.courseDto.CourseRequest;
import com.aicareer.core.DTO.courseDto.ResponseByWeek;
import com.aicareer.core.model.courseModel.CourseRequirements;
import com.aicareer.core.model.courseModel.Task;
import com.aicareer.core.model.courseModel.Week;
import com.aicareer.core.model.roadmap.Roadmap;
import com.aicareer.core.model.user.CVData;
import com.aicareer.core.model.user.User;
import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.core.model.vacancy.FinalVacancyRequirements;
import com.aicareer.core.model.vacancy.SelectedPotentialVacancy;
import com.aicareer.core.service.ParserOfVacancy.SelectVacancy;
import com.aicareer.core.service.course.*;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.service.information.ChatWithAiAfterDeterminingVacancyService;
import com.aicareer.core.service.information.ChatWithAiBeforeDeterminingVacancyService;
import com.aicareer.core.service.information.DialogService;
import com.aicareer.core.service.roadmap.RoadmapGenerateService;
import com.aicareer.core.service.user.UserService;

import com.aicareer.core.service.user.impl.UserServiceImpl;
import com.aicareer.repository.user.CVDataRepository;
import com.aicareer.repository.user.UserRepository;
import com.aicareer.repository.user.UserSkillsRepository;
import com.aicareer.repository.user.impl.CVDataRepositoryImpl;
import com.aicareer.repository.user.impl.UserRepositoryImpl;
import com.aicareer.repository.user.impl.UserSkillsRepositoryImpl;
import java.util.List;
import java.util.Scanner;
import javax.sql.DataSource;

// Предполагаемые классы (не импортированы в оригинале, но используются):
// Добавьте их в core, если ещё не сделано:
// import com.aicareer.core.service.user.model.AuthenticationResult;
// import com.aicareer.core.service.user.model.RegistrationResult;

public class Main {

  // === База данных ===
  private static DataSource dataSource;

  // === Сервисы — объявляем один раз ===
  private static GigaChatService gigaChatService;
  private static DialogService dialogService;
  private static ChatWithAiBeforeDeterminingVacancyService chatBeforeVacancyService;
  private static ChatWithAiAfterDeterminingVacancyService chatAfterVacancyService;
  private static RoadmapGenerateService roadmapGenerateService;
  private static UserService userService; // ← добавлено

  // === Результаты этапов ===
  private static FinalVacancyRequirements vacancyRequirements;
  private static CVData cvData;
  private static ResponseByWeek responseByWeek;
  private static User currentUser;

  public static void main(String[] args) {
    System.out.println("🚀 AI-Career Navigator: Полный end-to-end цикл");
    System.out.println("================================================");

    Scanner scanner = new Scanner(System.in);
    try {
      // 1. Инициализация данных и сервисов
      initializeData(scanner);
      initializeServices();

      // 2. Цикл регистрации/аутентификации → User currentUser
      if (!runAuthCycle(scanner)) {
        System.err.println("❌ Не удалось пройти аутентификацию. Прерывание.");
        return;
      }

      // 3. Цикл: AI-знакомство → UserPreferences
      UserPreferences userPreferences = runBeginAiChatCycle();
      if (userPreferences == null) {
        System.err.println("❌ Не удалось получить UserPreferences. Прерывание.");
        return;
      }

      // 4. Цикл: подбор вакансии → FinalVacancyRequirements
      vacancyRequirements = runVacancySelectionCycle(userPreferences);
      if (vacancyRequirements == null) {
        System.err.println("❌ Не удалось получить требования вакансии. Прерывание.");
        return;
      }

      // 5. Цикл: финальный чат → CourseRequirements
      CourseRequirements courseRequirements = runCourseRequirementsCycle();
      if (courseRequirements == null) {
        System.err.println("❌ Не удалось сформировать CourseRequirements. Прерывание.");
        return;
      }

      // 6. Цикл: генерация учебного плана + Roadmap
      Roadmap roadmap = runCourseAndRoadmapGenerationCycle(courseRequirements);
      if (roadmap == null) {
        System.err.println("❌ Не удалось сгенерировать Roadmap.");
        return;
      }

      // 7. Вывод результата
      System.out.println("\n✅ УСПЕХ: полный цикл завершён!");
      System.out.println("📋 Сгенерированная дорожная карта:");
      System.out.println(roadmap.getRoadmapZones());

    } catch (Exception e) {
      System.err.println("💥 КРИТИЧЕСКАЯ ОШИБКА: " + e.getMessage());
      e.printStackTrace();
    } finally {
      scanner.close();
    }
  }

  // === 1. Инициализация данных ===
  private static void initializeData(Scanner scanner) {
    cvData = new CVData();
    cvData.setInformation(
      "Петров Алексей Сергеевич\n" +
        "Цель: Замещение должности Java-разработчика\n" +
        "Контактная информация:\n" +
        "Телефон: +7 (999) 765-43-21\n" +
        "Email: petrov.as@example.com\n" +
        "Город: Санкт-Петербург\n" +
        "Образование: Высшее, ИТМО, ПО, 2020\n" +
        "Опыт: Java-разработчик в ООО ТехноСофт (2020–н.в.)\n" +
        "Навыки: Java, Kotlin, Spring Boot, Hibernate, Git, Docker, PostgreSQL\n" +
        "Английский: Upper-Intermediate"
    );
    // Для тестов используем заглушку (реально — из этапа 5)
    responseByWeek = createTestResponse();
  }

  // === 2. Инициализация сервисов ===
  private static void initializeServices() {
    System.out.println("🔧 Инициализация сервисов...");
    GigaChatConfig config = new GigaChatConfig();
    gigaChatService = new GigaChatService(config);
    dialogService = new DialogService(gigaChatService, true);
    chatBeforeVacancyService = new ChatWithAiBeforeDeterminingVacancyService(gigaChatService, dialogService);
    chatAfterVacancyService = new ChatWithAiAfterDeterminingVacancyService(gigaChatService, dialogService);
    roadmapGenerateService = new RoadmapGenerateService(gigaChatService);
    UserRepository userRepository = new UserRepositoryImpl(dataSource);
    CVDataRepository cvDataRepository = new CVDataRepositoryImpl(dataSource);
    UserSkillsRepository userSkillsRepository = new UserSkillsRepositoryImpl(dataSource);
    userService = new UserServiceImpl(userRepository, cvDataRepository, userSkillsRepository); // ← добавлено (инициализация)
    System.out.println("✅ Сервисы инициализированы");
  }

  // === 3. Цикл регистрации/аутентификации ===
  private static boolean runAuthCycle(Scanner scanner) {
    System.out.println("\n🔐 Цикл: Регистрация/Аутентификация");

    while (true) {
      System.out.println("\nВыберите действие:");
      System.out.println("1 - Регистрация");
      System.out.println("2 - Вход");
      System.out.println("3 - Выход");
      System.out.print("Ваш выбор: ");
      String choice = scanner.nextLine().trim();

      switch (choice) {
        case "1":
          if (registerUser(scanner)) {
            return true;
          }
          break;
        case "2":
          if (authenticateUser(scanner)) {
            return true;
          }
          break;
        case "3":
          System.out.println("👋 До свидания!");
          return false;
        default:
          System.out.println("❌ Неверный выбор. Попробуйте снова.");
      }
    }
  }

  private static boolean registerUser(Scanner scanner) {
    System.out.println("\n📝 Регистрация нового пользователя");

    UserRegistrationDto registrationDto = new UserRegistrationDto();

    System.out.print("Введите email: ");
    registrationDto.setEmail(scanner.nextLine().trim());

    System.out.print("Введите пароль: ");
    registrationDto.setPassword(scanner.nextLine().trim());

    System.out.print("Введите имя: ");
    registrationDto.setName(scanner.nextLine().trim());

    // Регистрация пользователя
    // Предполагается, что RegistrationResult существует в core/service/user/
    var result = userService.registerUser(registrationDto);

    if (result.isSuccess()) {
      currentUser = result.getUser();
      System.out.println("✅ Регистрация успешна! ID пользователя: " + currentUser.getId());
      return true;
    } else {
      System.out.println("❌ Ошибка регистрации:");
      result.getErrors().forEach(System.out::println);
      return false;
    }
  }

  private static boolean authenticateUser(Scanner scanner) {
    System.out.println("\n🔑 Аутентификация пользователя");

    LoginRequestDto loginRequest = new LoginRequestDto();

    System.out.print("Введите email: ");
    loginRequest.setEmail(scanner.nextLine().trim());

    System.out.print("Введите пароль: ");
    loginRequest.setPassword(scanner.nextLine().trim());

    // Аутентификация пользователя
    // Предполагается, что AuthenticationResult существует в core/service/user/
    var result = userService.authenticateUser(loginRequest);

    if (result.isSuccess()) {
      currentUser = result.getUser();
      System.out.println("✅ Аутентификация успешна! Добро пожаловать, " + currentUser.getName());
      return true;
    } else {
      System.out.println("❌ Ошибка аутентификации:");
      result.getErrors().forEach(System.out::println);
      return false;
    }
  }

  // === 4. AI-знакомство с пользователем ===
  private static UserPreferences runBeginAiChatCycle() {
    System.out.println("\n💬 Цикл: Знакомство с пользователем (AI-чат)");
    try {
      chatBeforeVacancyService.starDialogWithUser();
      chatBeforeVacancyService.askingStandardQuestions();

      List<String> personalizedQuestions = chatBeforeVacancyService.generatePersonalizedQuestions(cvData);
      chatBeforeVacancyService.askingPersonalizedQuestions(personalizedQuestions);

      return chatBeforeVacancyService.analyzeCombinedData();
    } catch (Exception e) {
      System.err.println("❌ Ошибка в AI-знакомстве: " + e.getMessage());
      return null;
    }
  }

  // === 5. Подбор вакансии (SelectVacancy) ===
  private static FinalVacancyRequirements runVacancySelectionCycle(UserPreferences userPreferences) {
    System.out.println("\n🎯 Цикл: Подбор и анализ вакансии");
    try {
      SelectVacancy selectVacancy = new SelectVacancy();

      String analysisResult = selectVacancy.analyzeUserPreference(userPreferences);
      List<String> suggested = selectVacancy.extractThreeVacancies(analysisResult);
      SelectedPotentialVacancy selected = selectVacancy.choosenVacansy(suggested);
      String parsingResults = selectVacancy.FormingByParsing(selected);
      String finalReqStr = selectVacancy.FormingFinalVacancyRequirements(parsingResults);

      return new FinalVacancyRequirements(finalReqStr);
    } catch (Exception e) {
      System.err.println("❌ Ошибка при подборе вакансии: " + e.getMessage());
      return null;
    }
  }

  // === 6. Формирование требований к курсу ===
  private static CourseRequirements runCourseRequirementsCycle() {
    System.out.println("\n🎓 Цикл: Формирование требований к курсу");
    try {
      List<String> questions = chatAfterVacancyService.generatePersonalizedQuestions(vacancyRequirements);
      chatAfterVacancyService.askingPersonalizedQuestions(questions);
      return chatAfterVacancyService.analyzeCombinedData(vacancyRequirements);
    } catch (Exception e) {
      System.err.println("❌ Ошибка при формировании CourseRequirements: " + e.getMessage());
      return null;
    }
  }

  // === 7. Генерация учебного плана и Roadmap ===
  private static Roadmap runCourseAndRoadmapGenerationCycle(CourseRequirements courseRequirements) {
    System.out.println("\n🗺️ Цикл: Генерация учебного плана и дорожной карты");

    try {
      GigaChatConfig config = new GigaChatConfig();
      GigaChatService localGigaChat = new GigaChatService(config);
      ServicePrompt promptService = new ServicePrompt();
      ServiceGenerateCourse generator = new ServiceGenerateCourse(promptService, localGigaChat);
      ServiceWeek parser = new ServiceWeek();
      WeekDistributionService distributor = new WeekDistributionService();

      LearningPlanAssembler assembler = new LearningPlanAssembler(generator, parser, distributor);

      CourseRequest request = new CourseRequest(courseRequirements.getCourseRequirements());
      ResponseByWeek response = assembler.assemblePlan(request);
      List<Week> weeks = response.getWeeks();

      // Валидация через LLM-валидатор
      String raw = simulateLlmRawResponse(weeks);
      if (!LlmResponseValidator.validate(raw)) {
        System.err.println("❌ Валидация учебного плана провалена");
        return null;
      }

      return roadmapGenerateService.generateRoadmap(response);
    } catch (Exception e) {
      System.err.println("❌ Ошибка при генерации Roadmap: " + e.getMessage());
      return null;
    }
  }

  // === Вспомогательные методы ===

  public static ResponseByWeek createTestResponse() {
    Week week1 = new Week();
    week1.setNumber(1);
    week1.setGoal("Освоить основы Python и анализа данных");

    Task t1 = new Task();
    t1.setDescription("Изучить базовый синтаксис Python");
    t1.setUrls(List.of(
      "https://docs.python.org/3/tutorial/",
      "https://www.learnpython.org/"
    ));

    Task t2 = new Task();
    t2.setDescription("Установить Jupyter Notebook");
    t2.setUrls(List.of("https://jupyter.org/install"));

    week1.setTasks(List.of(t1, t2));

    Week week2 = new Week();
    week2.setNumber(2);
    week2.setGoal("Pandas и NumPy");

    Task t3 = new Task();
    t3.setDescription("Освоить Pandas");
    t3.setUrls(List.of(
      "https://pandas.pydata.org/docs/",
      "https://www.w3schools.com/python/pandas/"
    ));

    week2.setTasks(List.of(t3));

    ResponseByWeek res = new ResponseByWeek();
    res.setWeeks(List.of(week1, week2));
    return res;
  }

  private static String simulateLlmRawResponse(List<Week> weeks) {
    StringBuilder sb = new StringBuilder();
    for (Week w : weeks) {
      sb.append("week").append(w.getNumber())
        .append(": goal: \"").append(w.getGoal()).append("\"");
      for (int i = 0; i < w.getTasks().size(); i++) {
        Task t = w.getTasks().get(i);
        sb.append(". task").append(i + 1)
          .append(": \"").append(t.getDescription()).append("\"");
        if (!t.getUrls().isEmpty()) {
          sb.append(". urls: \"")
            .append(String.join(", ", t.getUrls().stream().map(String::trim).toList()))
            .append("\"");
        }
      }
      sb.append("\n");
    }
    return sb.toString().trim();
  }
}