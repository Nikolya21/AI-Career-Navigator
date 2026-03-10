package com.aicareer.application;

import com.aicareer.core.dto.courseDto.CourseRequest;
import com.aicareer.core.dto.courseDto.ResponseByWeek;
import com.aicareer.core.dto.user.LoginRequestDto;
import com.aicareer.core.dto.user.UserRegistrationDto;
import com.aicareer.core.exception.*;
import com.aicareer.core.model.courseModel.CourseRequirements;
import com.aicareer.core.model.roadmap.Roadmap;
import com.aicareer.core.model.user.User;
import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.core.model.vacancy.FinalVacancyRequirements;
import com.aicareer.core.service.course.LearningPlanAssembler;
import com.aicareer.core.service.information.ChatWithAiAfterDeterminingVacancyService;
import com.aicareer.core.service.information.ChatWithAiBeforeDeterminingVacancyService;
import com.aicareer.core.service.parserOfVacancy.SelectVacancy;
import com.aicareer.core.service.roadmap.RoadmapGenerateService;
import com.aicareer.core.service.roadmap.RoadmapService;
import com.aicareer.core.service.user.UserService;
import com.aicareer.core.service.user.model.AuthenticationResult;
import com.aicareer.core.service.user.model.RegistrationResult;
import com.aicareer.core.service.user.model.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.Scanner;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CareerNavigatorApplicationImpl implements CareerNavigatorApplication {

  private final UserService userService;
  private final ChatWithAiBeforeDeterminingVacancyService chatBeforeVacancyService;
  private final SelectVacancy selectVacancy;
  private final ChatWithAiAfterDeterminingVacancyService chatAfterVacancyService;
  private final RoadmapGenerateService roadmapGenerateService;
  private final RoadmapService roadmapService;
  private final LearningPlanAssembler learningPlanAssembler;

  @Override
  public Long register(String email, String password, String name) throws AuthenticationException {
    log.info("Регистрация пользователя: {}", email);

    try {
      UserRegistrationDto dto = new UserRegistrationDto();
      dto.setEmail(email);
      dto.setPassword(password);
      dto.setName(name);

      RegistrationResult result = userService.registerUser(dto);
      if (!result.isSuccess()) {
        throw new AuthenticationException(
            AuthenticationException.Type.USER_ALREADY_EXISTS,
            "Регистрация не удалась: " + String.join("; ", result.getErrors())
        );
      }
      User currentUser = result.getUser();
      Long userId = currentUser.getId();

      // Загрузка резюме (консольный ввод)
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

      UpdateResult uploadResult = userService.uploadCV(cvFile, userId);
      if (!uploadResult.isSuccess()) {
        throw new AuthenticationException(
            AuthenticationException.Type.ACCOUNT_LOCKED,
            "Ошибка загрузки резюме: " + uploadResult.getErrors()
        );
      }

      UserPreferences userPreferences = handleUserPreferences(currentUser);
      if (userPreferences == null) return userId;

      FinalVacancyRequirements vacancyRequirements = handleVacancySelection(userPreferences);
      if (vacancyRequirements == null) return userId;

      CourseRequirements courseRequirements = handleCourseDefinition(vacancyRequirements);
      if (courseRequirements == null) return userId;

      log.info("Передаём требования в генератор курса...");
      CourseRequest courseRequest = new CourseRequest(courseRequirements);
      ResponseByWeek responseByWeek = learningPlanAssembler.assemblePlan(courseRequest);
      log.info("Курс сгенерирован: {} недель", responseByWeek.getWeeks().size());

      Roadmap roadmap = handleRoadmapGeneration(responseByWeek, currentUser);
      if (roadmap == null) return userId;
      userService.updateRoadmap(roadmap.getId(), userId);

      log.info("УСПЕХ: полный цикл завершён!");
      return userId;

    } catch (Exception e) {
      log.error("Системная ошибка при регистрации: {}", e.getMessage(), e);
      throw new AuthenticationException(
          AuthenticationException.Type.ACCOUNT_LOCKED,
          "Системная ошибка при регистрации: " + e.getMessage(),
          e
      );
    }
  }

  @Override
  public Long authenticate(String email, String password) throws AuthenticationException {
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
      LoginRequestDto loginDto = new LoginRequestDto(email, password);
      AuthenticationResult result = userService.authenticateUser(loginDto);
      if (result.isSuccess()) {
        log.info("Пользователь {} успешно аутентифицирован", email);
        return result.getUser().getId();
      } else {
        throw new AuthenticationException(
            AuthenticationException.Type.INVALID_CREDENTIALS,
            "Вход не удался: " + String.join("; ", result.getErrors())
        );
      }
    } catch (Exception e) {
      log.error("Ошибка аутентификации {}: {}", email, e.getMessage(), e);
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
      throw new ChatException(ChatException.Type.INVALID_RESPONSE_FORMAT, "CV не может быть пустым");
    }

    try {
      chatBeforeVacancyService.starDialogWithUser();
      chatBeforeVacancyService.askingStandardQuestions();

      UserPreferences userPreferences = chatBeforeVacancyService.analyzeCombinedData();
      if (userPreferences == null) {
        throw new ChatException(ChatException.Type.INVALID_RESPONSE_FORMAT, "AI не вернул предпочтения");
      }

      // Сохраняем через UserService
      UserPreferences saved = userService.saveUserPreferences(userPreferences, user.getId());
      return saved;

    } catch (ChatException e) {
      throw e;
    } catch (Exception e) {
      log.error("Ошибка AI-анализа для пользователя {}: {}", user.getId(), e.getMessage(), e);
      throw new ChatException(ChatException.Type.MODEL_ERROR, "Ошибка AI: " + e.getMessage(), e);
    }
  }

  @Override
  public FinalVacancyRequirements selectVacancy(UserPreferences preferences) throws VacancySelectionException {
    if (preferences == null) {
      throw new VacancySelectionException(VacancySelectionException.Type.INVALID_PREFERENCES,
          "UserPreferences не могут быть null");
    }

    try {
      String analysisResult = selectVacancy.analyzeUserPreference(preferences);
      if (analysisResult == null || analysisResult.trim().isEmpty()) {
        throw new VacancySelectionException(VacancySelectionException.Type.NO_VACANCIES_FOUND,
            "AI не вернул анализ");
      }

      var threeVacancies = selectVacancy.extractThreeVacancies(analysisResult, 0);
      if (threeVacancies.isEmpty()) {
        threeVacancies = selectVacancy.extractThreeVacancies(analysisResult, 0);
      }
      log.debug("Извлечено вакансий: {}", threeVacancies.size());

      var selected = selectVacancy.choosenVacansy(threeVacancies);
      log.info("Выбрана вакансия: {}", selected.getNameOfVacancy());
      userService.updateVacancy(selected.getNameOfVacancy(), preferences.getUserId());

      String parsed = selectVacancy.formingByParsing(selected);
      log.debug("Парсинг вакансии завершён, длина результата: {}", parsed.length());

      FinalVacancyRequirements finalReqs = selectVacancy.formingFinalVacancyRequirements(parsed);
      log.info("Финальные требования для вакансии сформированы");
      return finalReqs;

    } catch (Exception e) {
      log.error("Ошибка при подборе вакансии для пользователя {}: {}", preferences.getUserId(), e.getMessage(), e);
      throw new VacancySelectionException(VacancySelectionException.Type.PARSING_FAILED,
          "Ошибка подбора вакансии: " + e.getMessage(), e);
    }
  }

  @Override
  public User getUserProfile(Long userId) {
    return userService.getUserProfile(userId);
  }

  @Override
  public CourseRequirements defineCourseRequirements(FinalVacancyRequirements vacancyRequirements)
      throws CourseDefinitionException {
    if (vacancyRequirements == null || vacancyRequirements.getVacancyAllCompactRequirements() == null) {
      throw new CourseDefinitionException(CourseDefinitionException.Type.INSUFFICIENT_DATA,
          "Требования вакансии не заданы");
    }

    try {
      chatAfterVacancyService.askingPersonalizedQuestions(
          chatAfterVacancyService.generatePersonalizedQuestions(vacancyRequirements)
      );
      return chatAfterVacancyService.analyzeCombinedData(vacancyRequirements);
    } catch (Exception e) {
      log.error("Ошибка формирования требований к курсу: {}", e.getMessage(), e);
      throw new CourseDefinitionException(CourseDefinitionException.Type.COURSE_GENERATION_FAILED,
          "Не удалось сформировать требования к курсу", e);
    }
  }

  @Override
  public Roadmap generateRoadmap(ResponseByWeek responseByWeek, User user) throws RoadmapGenerationException {
    if (responseByWeek == null) {
      throw new RoadmapGenerationException(RoadmapGenerationException.Type.MISSING_COURSE_DATA,
          "ResponseByWeek не может быть null");
    }

    try {
      log.debug("Генерация roadmap для пользователя {}", user.getId());

      String weeksInfo = roadmapGenerateService.gettingWeeksInformation(responseByWeek);
      String zonesAnalysis = roadmapGenerateService.informationComplexityAndQuantityAnalyzeAndCreatingZone(weeksInfo);
      var zones = roadmapGenerateService.splittingWeeksIntoZones(zonesAnalysis, responseByWeek.getWeeks());
      log.debug("Создано зон: {}", zones.size());

      Roadmap generated = roadmapGenerateService.identifyingThematicallySimilarZones(zones);
      generated.setUserId(user.getId());

      Roadmap saved = roadmapService.saveCompleteRoadmap(generated);
      log.info("Roadmap сохранён, ID: {}", saved.getId());
      return saved;

    } catch (Exception e) {
      log.error("Ошибка генерации roadmap для пользователя {}: {}", user.getId(), e.getMessage(), e);
      throw new RoadmapGenerationException(RoadmapGenerationException.Type.INFRASTRUCTURE_ERROR,
          "Ошибка генерации roadmap: " + e.getMessage(), e);
    }
  }

  private UserPreferences handleUserPreferences(User user) {
    log.info("Цикл: Знакомство с пользователем (AI-чат)");
    try {
      String cvText = userService.getCVDataByUserId(user.getId()).getInformation();
      return gatherUserPreferences(user, cvText);
    } catch (Exception e) {
      log.error("Ошибка в AI-знакомстве: {}", e.getMessage(), e);
      return null;
    }
  }

  private FinalVacancyRequirements handleVacancySelection(UserPreferences preferences) {
    log.info("Цикл: Подбор и анализ вакансии");
    try {
      return selectVacancy(preferences);
    } catch (Exception e) {
      log.error("Ошибка при подборе вакансии: {}", e.getMessage(), e);
      return null;
    }
  }

  private CourseRequirements handleCourseDefinition(FinalVacancyRequirements vacancyRequirements) {
    log.info("Цикл: Формирование требований к курсу");
    try {
      return defineCourseRequirements(vacancyRequirements);
    } catch (Exception e) {
      log.error("Ошибка при формировании CourseRequirements: {}", e.getMessage(), e);
      return null;
    }
  }

  private Roadmap handleRoadmapGeneration(ResponseByWeek responseByWeek, User user) {
    log.info("Цикл: Генерация учебного плана и дорожной карты");
    try {
      return generateRoadmap(responseByWeek, user);
    } catch (Exception e) {
      log.error("Ошибка при генерации Roadmap: {}", e.getMessage(), e);
      return null;
    }
  }

  public Roadmap getSavedRoadmap(Long userId) throws RoadmapGenerationException {
    try {
      return roadmapService.findFullRoadmapById(
          roadmapService.findRoadmapByUserId(userId)
              .orElseThrow(() -> new RoadmapGenerationException(
                  RoadmapGenerationException.Type.MISSING_COURSE_DATA,
                  "Roadmap не найдена для пользователя: " + userId
              )).getId()
      ).orElseThrow(() -> new RoadmapGenerationException(
          RoadmapGenerationException.Type.MISSING_COURSE_DATA,
          "Roadmap не найдена для пользователя: " + userId
      ));
    } catch (Exception e) {
      log.error("Ошибка при получении roadmap для пользователя {}: {}", userId, e.getMessage(), e);
      throw new RoadmapGenerationException(
          RoadmapGenerationException.Type.INFRASTRUCTURE_ERROR,
          "Ошибка при получении roadmap",
          e
      );
    }
  }
}