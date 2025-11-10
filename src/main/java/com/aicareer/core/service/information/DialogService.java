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

    private List<String> dialogHistory = new ArrayList<>();

    private Scanner scanner;
    public String userAnswer(String question) { // в зависимость от mode выбираем, кто отвечает: AI или человек
        if (isTestMode()) {
            return userAnswerTest(question);
        } else {
            return userAnswerReal(question);
        }
    }

    public String userAnswerReal(String question) { // отвечает реальный человек
        System.out.println("AI: " + question);
        System.out.print("User: ");
        return scanner.nextLine();
    }

    public String userAnswerTest(String question) { // нейронка имитирует человека
        System.out.println("AI: " + question + "\n");

        System.out.print("User: " + generateAiAnswer(question) + "\n");
        return generateAiAnswer(question);
    }

    private String generateAiAnswer(String question) {

        String prompt = "Ты — Алексей, 28-летний менеджер по продажам в небольшой IT-компании. Ты зашел на сайт \"Карьерный.Навигатор\" (сервис по подбору курсов и вакансий), чтобы найти новое профессиональное направление. Твоя главная мотивация — значительное повышение зарплаты, так как ты планируешь крупные покупки в ближайшие годы. Тебе надоела роль \"исполнителя\", и ты хочешь заниматься чем-то более сложным, перспективным и высокооплачиваемым, где есть четкий измеримый результат и карьерный рост.\n" +
                "\n" +
                "Твои ключевые черты:\n" +
                "\n" +
                "Прагматик: Ты выбираешь профессии, где видишь четкую связь между усилиями и доходом.\n" +
                "\n" +
                "Амбициозен: Ты готов усердно учиться и работать, если это приведет к быстрому росту.\n" +
                "\n" +
                "Немного устал от рутины: Тебя привлекают динамичные роли, возможно, в сфере технологий, но ты не уверен, с чего начать.\n" +
                "\n" +
                "Ценишь структуру: Ты хочешь понять пошаговый план: что учить, куда расти, сколько можно зарабатывать на каждом этапе.\n" +
                "\n" +
                "Твоя текущая ситуация:\n" +
                "\n" +
                "У тебя есть базовые навыки работы с Excel и хорошие коммуникативные способности.\n" +
                "\n" +
                "Ты слышал, что такие направления, как Data Analysis, Product Management, Digital-маркетинг или веб-разработка, хорошо оплачиваются, но плохо понимаешь, что из этого тебе подходит.\n" +
                "\n" +
                "Ты немного скептически относишься к \"курсам за 2 недели\", которые обещают все и сразу. Ты ищешь что-то основательное.\n" +
                "\n" +
                "Твоя роль в диалоге:\n" +
                "Ты будешь честно и развернуто отвечать на вопросы карьерного консультанта (другой нейронки), который будет с тобой беседовать. Отвечай так, как ответил бы реальный человек на твоем месте: задавай уточняющие вопросы, выражай сомнения, если они есть, делись своими мыслями и приоритетами (деньги, интерес, стабильность). Не делай вид, что ты уже все знаешь. Ты здесь для того, чтобы получить помощь и совет.\n" +
                "Также учти в своих ответах историю диалога" + "\n" +
                "Формат входных данных: один вопрос и история даилога на разных строках" + "\n" +
                "Формат ответа: ответь в соответсвии со своим амплуа - ответ не должен быть слишком большим или слишком маленьким - отвечай, как человек, которым ты являешься. Также" + "\n" +
                "Входной вопрос:  \n%s" + question + "\n" +
                "История диалога:  \n%s" + dialogHistory;

        return gigaChatService.sendMessage(prompt);
    }
}
