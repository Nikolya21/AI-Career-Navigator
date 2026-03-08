package com.aicareer.core.service.information;

import com.aicareer.core.model.courseModel.CourseRequirements;
import com.aicareer.core.model.user.UserLearningProfile;
import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.service.information.prompts.AdaptationPrompts;
import com.aicareer.core.service.information.prompts.StandartPullQuestion;
import com.aicareer.repository.user.UserPreferencesRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdaptationCourseChat {

  private final GigaChatService gigaChatApiService;
  private final DialogService dialogService;
  private final ObjectMapper objectMapper;
  private final UserPreferencesRepository preferencesRepository;
  private final List<String> dialogHistory = new ArrayList<>();

  /**
   * Проводит интервью для сбора предпочтений и объединяет с существующей целью.
   * @param userPreferencesWithGoal UserPreferences с заполненным infoAboutPerson (из GoalWindowService)
   * @return CourseRequirements с полными данными
   */
  public CourseRequirements generateCourseRequirements(UserPreferences userPreferencesWithGoal) {
    Long userId = userPreferencesWithGoal.getUserId();

    // Шаг 1: Собираем предпочтения через диалог
    askQuestion(StandartPullQuestion.TIME_QUESTION);
    askQuestion(StandartPullQuestion.INFORMATION_FORMAT);
    askQuestion(StandartPullQuestion.MOTIVATION_CONTEXT);
    askQuestion(StandartPullQuestion.BACKGROUND_QUESTION);

    // Шаг 2: Анализируем ответы и получаем учебный профиль
    UserLearningProfile learningProfile = analyzeResponses();

    // Шаг 3: Получаем существующие предпочтения из БД или создаём новые
    UserPreferences existingPreferences = preferencesRepository.findByUserId(userId)
      .orElse(UserPreferences.builder()
        .userId(userId)
        .build());

    // Шаг 4: Обновляем поля
    existingPreferences.setInfoAboutPerson(userPreferencesWithGoal.getInfoAboutPerson());
    existingPreferences.setUserLearningProfile(learningProfile); // сохраняем весь профиль целиком

    // Шаг 5: Сохраняем в БД
    UserPreferences savedPreferences = preferencesRepository.save(existingPreferences);

    // Шаг 6: Формируем и возвращаем CourseRequirements
    return CourseRequirements.builder()
      .userPreferences(savedPreferences)
      .build();
  }

  private void askQuestion(String question) {
    String context = getRecentContext(3);
    dialogHistory.add("AI: " + question);
    String userAnswer = dialogService.userAnswer(question, context);
    dialogHistory.add("User: " + userAnswer);
  }

  private String getRecentContext(int maxMessages) {
    int total = dialogHistory.size();
    int start = Math.max(0, total - maxMessages * 2);
    return String.join("\n", dialogHistory.subList(start, total));
  }

  private UserLearningProfile analyzeResponses() {
    String fullDialog = String.join("\n", dialogHistory);

    String prompt = String.format(AdaptationPrompts.ANALYZE_PREFERENCES, fullDialog);

    String analysisResult = gigaChatApiService.sendMessage(prompt);

    try {
      return objectMapper.readValue(analysisResult, UserLearningProfile.class);
    } catch (Exception e) {
      throw new RuntimeException("Ошибка парсинга JSON от GigaChat: " + analysisResult, e);
    }
  }
}