package com;

import com.aicareer.core.DTO.ResponseByWeek;
import com.aicareer.core.model.*;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.service.information.ChatWithAiAfterDeterminingVacancyService;
import com.aicareer.core.service.information.ChatWithAiBeforeDeterminingVacancyService;
import com.aicareer.core.service.information.DialogService;
import com.aicareer.core.service.roadmap.RoadmapGenerateService;
import com.aicareer.repository.information.ChatWithAiBeforeDeterminingVacancy;


import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;


public class Main {
  public static void main(String[] args) {

  }

  private static void initializeServices() {
    System.out.println("1. ИНИЦИАЛИЗАЦИЯ СЕРВИСОВ...");

    // Инициализация DataSource
    //DataSource dataSource = setupDataSource();

    // Инициализация репозиториев
    //RoadmapRepository roadmapRepository = new RoadmapRepositoryImpl(dataSource);


    // Инициализация сервисов
    GigaChatService gigaChatService = new GigaChatService();

    DialogService dialogService = new DialogService(gigaChatService, true);

    ChatWithAiBeforeDeterminingVacancy chatBeforeVacancyService = new ChatWithAiBeforeDeterminingVacancyService(gigaChatService, dialogService);
    ChatWithAiAfterDeterminingVacancyService chatAfterVacancyService = new ChatWithAiAfterDeterminingVacancyService(gigaChatService, dialogService);

    RoadmapGenerateService roadmapService = new RoadmapGenerateService(gigaChatService);

    System.out.println("✅ Все сервисы инициализированы");
  }

  private static UserPreferences runBeginAiChatCycle(ChatWithAiBeforeDeterminingVacancy chatBeforeVacancyService, CVdata cvData) { //вместо CVdata должен быть какойто сервис,связанный с User
    System.out.println("\n💬 ЦИКЛ 2: ЗНАКОМСТВО С ПОЛЬЗОВАТЕЛЕМ ЧЕРЕЗ AI-ЧАТ");

    try {
      // Симуляция чата с AI
      chatBeforeVacancyService.starDialogWithUser();

      chatBeforeVacancyService.askingStandardQuestions();


      List<String> personalizedQuestions = chatBeforeVacancyService.generatePersonalizedQuestions(cvData);
      chatBeforeVacancyService.askingPersonalizedQuestions(personalizedQuestions);

      UserPreferences userPreferences = chatBeforeVacancyService.analyzeCombinedData();

      return userPreferences;

    } catch (Exception e) {
      System.out.println("❌ Ошибка в цикле AI-чата: {}" + " " + e.getMessage());
      return null;
    }
  }

  private static UserPreferences runSummarizingAiChatCycle(ChatWithAiBeforeDeterminingVacancy chatBeforeVacancyService, CVdata cvData) {
    System.out.println("\n💬 ЦИКЛ 4: ФОРМИРОВАНИЕ ВСПОМОГАТЕЛЬНОЙ ИНФОРМАЦИИ НА ОСНОВЕ ДИАЛОГА С ПОЛЬЗОВАТЕЛЕМ ЧЕРЕЗ AI-ЧАТ");

    try {
      // Симуляция чата с AI
      chatBeforeVacancyService.starDialogWithUser();

      chatBeforeVacancyService.askingStandardQuestions();


      List<String> personalizedQuestions = chatBeforeVacancyService.generatePersonalizedQuestions(cvData);
      chatBeforeVacancyService.askingPersonalizedQuestions(personalizedQuestions);

      UserPreferences userPreferences = chatBeforeVacancyService.analyzeCombinedData();

      return userPreferences;

    } catch (Exception e) {

      System.out.println("❌ Ошибка в цикле AI-чата: {}" + " " + e.getMessage());

      return null;
    }
  }

  private static CourseRequirements runCourseRequirementsCycle(ChatWithAiAfterDeterminingVacancyService chatAfterVacancyService,
                                                               FinalVacancyRequirements vacancyRequirements) {
    System.out.println("\n🎓 ЦИКЛ 6: ФОРМИРОВАНИЕ ТРЕБОВАНИЙ К КУРСУ ОБУЧЕНИЯ ЧЕРЕЗ РЕЗЮМИРУЮЩИЙ ДИАЛОГ");

    try {
      System.out.println("📝 ФОРМИРУЕМ ТРЕБОВАНИЯ К КУРСУ НА ОСНОВЕ ФИНАЛЬНОГО ДИАЛОГА И ИНФОРМАЦИИ, СОБРАННОЙ НА ПРЕДЫДУЩИХ ШАГАХ...");

      List<String> personalizedQuestions = chatAfterVacancyService.generatePersonalizedQuestions(vacancyRequirements);

      chatAfterVacancyService.askingPersonalizedQuestions(personalizedQuestions);

      // AI формирует требования к учебному курсу
      CourseRequirements courseRequirements = chatAfterVacancyService.analyzeCombinedData(vacancyRequirements);


      return courseRequirements;

    } catch (Exception e) {
      System.out.println("❌ Ошибка в цикле формирования требований к курсу: {}" + " " + e.getMessage());
      return null;
    }
  }


  private static Roadmap runCourseAndRoadmapGenerationCycle(RoadmapGenerateService roadmapGenerateService,
                                                                 ResponseByWeek responseByWeek) {
    System.out.println("\n🗺️ ЦИКЛ 8: ГЕНЕРАЦИЯ КУРСА И ДОРОЖНОЙ КАРТЫ");

    try {
      System.out.println("🚀 СОЗДАЕМ ПЕРСОНАЛИЗИРОВАННЫЙ КУРС И ROADMAP...");

      String weeksInformation = roadmapGenerateService.gettingWeeksInformation(responseByWeek);

      String resultOfComplexityAndQuantityAnalyze = roadmapGenerateService.informationComplexityAndQuantityAnalyzeAndCreatingZone(weeksInformation);

      List<Week> weeks = responseByWeek.getWeeks();
      List<RoadmapZone> roadmapZones = roadmapGenerateService.splittingWeeksIntoZones(resultOfComplexityAndQuantityAnalyze, weeks);

      Roadmap roadmap = roadmapGenerateService.identifyingThematicallySimilarZones(roadmapZones);

      return roadmap;

    } catch (Exception e) {

      System.out.println("❌ Ошибка в цикле генерации курса и roadmap: {}" + " " + e.getMessage());

      return null;
    }
  }
}