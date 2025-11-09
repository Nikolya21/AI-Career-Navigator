package com.aicareer.core.service.information;

import com.aicareer.core.model.CourseRequirements;
import com.aicareer.core.model.FinalVacancyRequirements;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.service.information.prompts.AfterDeterminingPrompts;
import com.aicareer.repository.information.ChatWithAiAfterDeterminingVacancy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ChatWithAiAfterDeterminingVacancyService implements ChatWithAiAfterDeterminingVacancy {

    private final GigaChatService gigaChatApiService;

    private final DialogService dialogService;

    private List<String> dialogHistory = new ArrayList<>();

    @Override
    public List<String> generatePersonalizedQuestions(FinalVacancyRequirements requarements) {
        String vacancyRequirements = requarements.getVacancyAllCompactRequirements();

        String prompt = AfterDeterminingPrompts.GENERATE_QUESTIONS + vacancyRequirements;

        return Arrays.asList(gigaChatApiService.sendMessage(prompt).split("\\|"));

    }

    @Override
    public void askingPersonalizedQuestions(List<String> generatedPersonalizedQuestions) {
        for (String question : generatedPersonalizedQuestions) {
            askingQuestion(question);

            dialogHistory.add("AI: " + question);
            String userAnswer = dialogService.userAnswer(question);

            dialogHistory.add("User: " + userAnswer);

            continueDialogWithUser(userAnswer);

            String userAdditionalAnswer = dialogService.userAnswer(question);
            dialogHistory.add("User: " + userAdditionalAnswer);
        }
    }

    @Override
    public String askingQuestion(String question) {
        return question;
    }

    @Override
    public String continueDialogWithUser(String userAnswer) {
        String context = String.join("\n", dialogHistory);

        String prompt = AfterDeterminingPrompts.CONTINUE_DIALOG + context + "\n%s" + userAnswer;
        return gigaChatApiService.sendMessage(prompt);
    }

    @Override
    public CourseRequirements analyzeCombinedData(FinalVacancyRequirements requirements) {
        String vacancyRequirements = requirements.getVacancyAllCompactRequirements();
        String context = String.join("\n", dialogHistory);

        String prompt = AfterDeterminingPrompts.ANALYZE_DATA + vacancyRequirements + "\n%s" + context;

        return new CourseRequirements(gigaChatApiService.sendMessage(prompt));
    }
}
