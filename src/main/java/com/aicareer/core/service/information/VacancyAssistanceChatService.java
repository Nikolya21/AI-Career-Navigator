package com.aicareer.core.service.information;

import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.service.information.prompts.GuidancePrompts;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Data
@Service
@RequiredArgsConstructor
public class VacancyAssistanceChatService {

  private final GigaChatService gigaChatApiService;
  private final DialogService dialogService;
  private final List<String> dialogHistory = new ArrayList<>();

  private static final int MAX_TURNS = 15;

  /**
   * Запускает информационный чат об IT-сфере.
   * Пользователь может задавать вопросы и получать информацию.
   * Сервис ничего не сохраняет, просто помогает ознакомиться с IT.
   */
  public void conductInfoChat() {
    say(GuidancePrompts.WELCOME_MESSAGE);

    String currentQuestion = generateFirstQuestion();
    int turn = 0;

    while (turn < MAX_TURNS) {
      String userAnswer = dialogService.userAnswer(currentQuestion, getRecentContext(3));
      dialogHistory.add("User: " + userAnswer);

      if (isExitIntent(userAnswer)) {
        say(GuidancePrompts.EXIT_MESSAGE);
        break;
      }

      String aiResponse = generateResponse(userAnswer);
      say(aiResponse);

      currentQuestion = generateNextQuestion();
      turn++;
    }
  }

  private void say(String message) {
    System.out.println("AI: " + message);
    dialogHistory.add("AI: " + message);
  }

  private String generateFirstQuestion() {
    return gigaChatApiService.sendMessage(GuidancePrompts.GENERATE_FIRST_QUESTION);
  }

  private String generateResponse(String userMessage) {
    String prompt = String.format(
      GuidancePrompts.GENERATE_RESPONSE,
      userMessage,
      String.join("\n", dialogHistory)
    );
    return gigaChatApiService.sendMessage(prompt);
  }

  private String generateNextQuestion() {
    String prompt = String.format(
      GuidancePrompts.GENERATE_NEXT_QUESTION,
      String.join("\n", dialogHistory)
    );
    return gigaChatApiService.sendMessage(prompt);
  }

  private boolean isExitIntent(String answer) {
    String lower = answer.toLowerCase();
    return lower.contains("хочу выбрать профессию")
      || lower.contains("завершить")
      || lower.contains("выйти")
      || lower.contains("спасибо, достаточно")
      || lower.contains("перейти к цели");
  }

  private String getRecentContext(int maxMessages) {
    int fromIndex = Math.max(0, dialogHistory.size() - maxMessages * 2);
    return String.join("\n", dialogHistory.subList(fromIndex, dialogHistory.size()));
  }
}