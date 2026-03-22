package com.aicareer.core.service.information;

import com.aicareer.core.model.courseModel.CourseRequirements;
import com.aicareer.core.model.vacancy.FinalVacancyRequirements;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.service.information.prompts.AfterDeterminingPrompts;
import com.aicareer.repository.information.ChatWithAiAfterDeterminingVacancy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatWithAiAfterDeterminingVacancyService implements ChatWithAiAfterDeterminingVacancy {

  private final GigaChatService gigaChatApiService;
  private final DialogService dialogService;

  private List<String> dialogHistory = new ArrayList<>();

  @Override
  public List<String> generatePersonalizedQuestions(FinalVacancyRequirements requirements) {
    String vacancyRequirements = requirements.getVacancyAllCompactRequirements();
    String prompt = AfterDeterminingPrompts.GENERATE_QUESTIONS + vacancyRequirements;
    return Arrays.asList(gigaChatApiService.sendMessage(prompt).split("\\|"));
  }

  @Override
  public void askingPersonalizedQuestions(List<String> generatedPersonalizedQuestions) {
    for (String question : generatedPersonalizedQuestions) {
      if (question.isEmpty()) continue;
      askingQuestion(question);

      List<String> recentHistory = getRecentDialogHistory(3);
      String context = String.join("\n", recentHistory);

      dialogHistory.add("AI: " + question);
      String userAnswer = dialogService.userAnswer(question, context);
      dialogHistory.add("User: " + userAnswer);
    }
  }

  private List<String> getRecentDialogHistory(int maxMessages) {
    int fromIndex = Math.max(0, dialogHistory.size() - maxMessages * 2);
    return dialogHistory.subList(fromIndex, dialogHistory.size());
  }

  @Override
  public String continueDialogWithUser(String userAnswer, String context) {
    String prompt = AfterDeterminingPrompts.CONTINUE_DIALOG + context;
    return gigaChatApiService.sendMessage(prompt);
  }

  @Override
  public String askingQuestion(String question) {
    return question;
  }

  @Override
  public CourseRequirements analyzeCombinedData(FinalVacancyRequirements requirements) {
    String vacancyRequirements = requirements.getVacancyAllCompactRequirements();
    String context = String.join("\n", dialogHistory);
    String prompt = AfterDeterminingPrompts.ANALYZE_DATA + vacancyRequirements + "\n%s" + context;
    return new CourseRequirements(gigaChatApiService.sendMessage(prompt));
  }
}