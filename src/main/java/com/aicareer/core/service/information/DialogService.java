package com.aicareer.core.service.information;

import com.aicareer.core.service.gigachat.GigaChatService;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Data
@RequiredArgsConstructor
public class DialogService {

    private final GigaChatService gigaChatService;

    private final boolean testMode;

    private Scanner scanner = new Scanner(System.in);

    public String userAnswer(String question, String context) { // в зависимость от mode выбираем, кто отвечает: AI или человек
        if (isTestMode()) {
            return userAnswerTest(question, context);
        } else {
            return userAnswerReal(question, context);
        }
    }

    public String userAnswerReal(String question, String context) { // отвечает реальный человек
        System.out.println("AI: " + question);
        String userAnswer = scanner.nextLine();
        System.out.print("User: " + userAnswer);

        return userAnswer;
    }

    public String userAnswerTest(String question, String context) { // нейронка имитирует человека
        System.out.println("AI: " + question);

        String aiAnswer = generateAiAnswer(question, context);
        System.out.print("User: " + aiAnswer + "\n");
        return aiAnswer;
    }

    private String generateAiAnswer(String question, String context) {

        String prompt = "\"Ты — Алексей, 28-летний менеджер по продажам. Ищешь новую карьеру через \\\"Карьерный.Навигатор\\\". Главная цель — значительно повысить зарплату для крупных покупок. Устал от роли исполнителя, хочешь перспективную работу с измеримым результатом.\n" +
                "\n" +
                "Твои черты:\n" +
                "- Прагматик: веришь в связь «усилия → доход»\n" +
                "- Амбициозен: готов усердно учиться для быстрого роста  \n" +
                "- Ценишь структуру: нужен четкий план развития\n" +
                "\n" +
                "Текущая ситуация:\n" +
                "- Навыки: базовый Excel, хорошие коммуникативные способности\n" +
                "- Рассматриваешь Data Analysis, Product Management, Digital-маркетинг, но не уверен в выборе\n" +
                "- Скептически относишься к коротким курсам, ищешь основательный подход\n" +
                "\n" +
                "Твоя роль:\n" +
                "Отвечай кратко и по делу на вопросы карьерного консультанта. Не задавай встречных вопросов. Делись мыслями, сомнениями, приоритетами (деньги, рост, стабильность). Будь честным, но лаконичным.\n" +
                "\n" +
                "Входной вопрос:  \n" +
                "%s\n" + question +
                "\n" +
                "Контектс диалога:  \n" +
                "%s\"" + context;

        return gigaChatService.sendMessage(prompt);
    }
}
