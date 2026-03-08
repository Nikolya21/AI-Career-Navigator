package com.aicareer.core.service.information;

import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.core.service.information.prompts.GoalPrompts;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Data
@Service
@RequiredArgsConstructor
public class GoalWindowService {

  private final DialogService dialogService;
  private final List<String> dialogHistory = new ArrayList<>();

  /**
   * Запрашивает у пользователя его цель и возвращает объект UserPreferences.
   * @param userId идентификатор пользователя
   * @return UserPreferences с заполненным полем infoAboutPerson
   */
  public UserPreferences askGoal(Long userId) {
    dialogHistory.add("AI: " + GoalPrompts.ASK_GOAL_QUESTION);

    String context = getRecentContext(3);
    String userAnswer = dialogService.userAnswer(GoalPrompts.ASK_GOAL_QUESTION, context);
    dialogHistory.add("User: " + userAnswer);

    return UserPreferences.builder()
      .userId(userId)
      .infoAboutPerson(userAnswer)
      .build();
  }

  private String getRecentContext(int maxMessages) {
    int fromIndex = Math.max(0, dialogHistory.size() - maxMessages * 2);
    return String.join("\n", dialogHistory.subList(fromIndex, dialogHistory.size()));
  }
}