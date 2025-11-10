package com.aicareer.core.service.information;

import com.aicareer.core.model.user.CVData;
import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.service.information.prompts.BeforeDeterminingPrompts;
import com.aicareer.repository.information.ChatWithAiBeforeDeterminingVacancy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ChatWithAiBeforeDeterminingVacancyService implements ChatWithAiBeforeDeterminingVacancy {

    private final GigaChatService gigaChatApiService;

    private final DialogService dialogService;

    private List<String> dialogHistory = new ArrayList<>();


    private List<String> pullStandartQuestions = Arrays.asList(
            "Представь, что мы с тобой встречаемся через год. Какой главный профессиональный результат этого года заставил бы тебя гордиться собой? ",
            "Что для тебя важнее в работе сейчас: глубокая экспертиза в одной области или возможность работать на стыке разных направлений? Почему?",
            "Опиши своего «идеального работодателя» или «идеальный проект» тремя словами. Что делает их такими привлекательными?",
            "Если бы у тебя была суперспособность, чтобы прокачать один свой профессиональный навык за один день, что бы это был за навык и как он изменил бы твою карьерную ситуацию?",
            "Вспомни самую интересную задачу, которую тебе доводилось решать. Что именно в ней цепляло тебя больше всего — процесс поиска решения, работа в команде или неочевидный результат?",
            "Как ты думаешь, какие soft skills («гибкие навыки») становятся все более важными на современном рынке труда, независимо от профессии? Какие из них тебе интересно было бы развить в себе?"
    );

    private String firstMessage =
            "Здравствуйте и добро пожаловать!\n" +
            "\n" +
            "Очень рад нашей встрече. Прежде чем мы перейдем к обсуждению вакансий и конкретных шагов, я бы хотел лучше познакомиться с вами и вашей уникальной историей.\n" +
            "\n" +
            "Самый важый этап в любом путешествии — это понять, куда мы идем и зачем. Карьера — это не просто список должностей, а история вашего роста, ценностей и устремлений.\n" +
            "\n" +
            "Давайте начнем с самого интересного — с вашего будущего. Представьте, что мы с вами встречаемся через год за чашкой кофе, и вы с гордостью и огоньком в глазах делитесь своим главным профессиональным достижением за этот период.\n" +
            "\n" +
            "Итак, первый вопрос: ";


    @Override
    public String starDialogWithUser() {
        return this.firstMessage;
    }

    @Override
    public void askingStandardQuestions() {
        for (String question : pullStandartQuestions) {
            askingStandardQuestion(question);

            dialogHistory.add("AI: " + question);
            String userAnswer = dialogService.userAnswer(question);

            dialogHistory.add("User: " + userAnswer);

            continueDialogWithUser(userAnswer);

            String userAdditionalAnswer = dialogService.userAnswer(question);
            dialogHistory.add("User: " + userAdditionalAnswer);
        }
    }

    @Override
    public String askingStandardQuestion(String question) {
        return question;
    }


    @Override
    public String continueDialogWithUser(String userAnswer) {
        String context = String.join("\n", dialogHistory);

        String prompt = BeforeDeterminingPrompts.CONTINUE_DIALOG + context;

        return gigaChatApiService.sendMessage(prompt);
    }

    @Override
    public List<String> generatePersonalizedQuestions(CVData cvData) {
        String informationAboutResume = cvData.getInformation();
        String prompt = BeforeDeterminingPrompts.CONTINUE_DIALOG + informationAboutResume + "\n%s" + dialogHistory;

        return Arrays.asList(gigaChatApiService.sendMessage(prompt).split("\\|"));
    }

    @Override
    public void askingPersonalizedQuestions(List<String> generatedPersonalizedQuestions) {
        for (String question : generatedPersonalizedQuestions) {

            askingPersonalizedQuestion(question);

            dialogHistory.add("AI: " + question);
            String userAnswer = dialogService.userAnswer(question);

            dialogHistory.add("User: " + userAnswer);

            continueDialogWithUser(userAnswer);

            String userAdditionalAnswer = dialogService.userAnswer(question);
            dialogHistory.add("User: " + userAdditionalAnswer);
        }
    }

    @Override
    public String askingPersonalizedQuestion(String question) {
        return question;
    }


    @Override
    public UserPreferences analyzeCombinedData() {
        String prompt = BeforeDeterminingPrompts.ANALYZE_DATA + dialogHistory;
        return new UserPreferences(gigaChatApiService.sendMessage(prompt));
    }
}
