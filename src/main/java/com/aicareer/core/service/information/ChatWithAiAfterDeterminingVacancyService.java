package com.aicareer.core.service.information;

import com.aicareer.core.model.CourseRequirements;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.repository.information.ChatWithAiAfterDeterminingVacancy;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@Data
public class ChatWithAiAfterDeterminingVacancyService implements ChatWithAiAfterDeterminingVacancy {

    private final GigaChatService gigaChatApiService;

    private List<String> dialogHistory = new ArrayList<>();

    @Override
    public List<String> generatePersonalizedQuestions(FinalVacancyRequarements requarements) {
        String vacancyRequirements = requarements.getVacancyAllCompactRequirements();

        String prompt = String.format(
                "Роль: Ты — опытный карьерный консультант и специалист по педагогическому дизайну. Твоя задача — не просто собрать информацию, а мотивировать человека и помочь ему мысленно \"примерить\" на себя процесс обучения, чтобы повысить его шансы на успешное завершение курса.\n" +
                        "\n" +
                        "Контекст: Пользователь рассматривает новую для себя вакансию и хочет пройти обучение, но есть риск, что он бросит курс из-за непонимания сложности, объема работы или отсутствия четкого плана.\n" +
                        "\n" +
                        "Задача: На основе краткого описания вакансии ниже, сгенерируй список из 5 до 7 вопросов. Вопросы должны решать три ключевые задачи:\n" +
                        "\n" +
                        "Оценка сложности и реалистичность: Помочь пользователю трезво оценить разрыв между его текущими навыками и требованиями вакансии.\n" +
                        "\n" +
                        "Планирование ресурсов: Определить, сколько времени он готов инвестировать в обучение еженедельно.\n" +
                        "\n" +
                        "Визуализация процесса (\"Onboarding в обучение\"): Создать в его воображении конкретную и понятную картину предстоящего обучения, чтобы снизить страх перед неизвестностью и дать ощущение контроля.\n" +
                        "\n" +
                        "Детали и требования к вопросам:\n" +
                        "\n" +
                        "Вопрос 1: Должен быть направлен на самооценку текущего уровня знаний относительно вакансии.\n" +
                        "\n" +
                        "Вопрос 2: Должен касаться мотивации и личной цели, чтобы напомнить, \"зачем это все\".\n" +
                        "\n" +
                        "Вопрос 3: Должен помочь оценить самый большой пробел в знаниях или самый пугающий аспект вакансии.\n" +
                        "\n" +
                        "Вопрос 4: Должен быть сфокусирован на планировании времени. Спроси о конкретных часах в неделю и о том, от чего придется отказаться ради обучения.\n" +
                        "\n" +
                        "Вопрос 5-7: Должны запустить процесс визуализации. Используй формулировки, которые заставят пользователя мысленно проиграть сценарий: \"Представь, что...\", \"Опиши свой идеальный учебный день...\", \"Как ты поймешь, что успешно освоил тему...\".\n" +
                        "\n" +
                        "Избегай абстрактных вопросов вроде \"Готовы ли вы учиться?\". Все вопросы должны быть открытыми и побуждать к размышлению.\n" +
                        "\n" +
                        "Тон вопросов: поддерживающий, практичный, побуждающий к действию.\n" +
                        "\n" +
                        "Формат вывода:\n" +
                        "Выведи строго в виде строк, разделенных знаком |. Не добавляй никаких дополнительных пояснений, заголовков или заключений после списка.\n" +
                        "\n" +
                        "Описание вакансии для анализа: " + "\n%s", vacancyRequirements
        );

        return Arrays.asList(gigaChatApiService.sendMessage(prompt).split("\\|"));

    }

    @Override
    public void askingPersonalizedQuestions(List<String> generatedPersonalizedQuestions) {
        Scanner scanner = new Scanner(System.in);
        for (String question : generatedPersonalizedQuestions) {
            askingQuestion(question);
            dialogHistory.add("AI: " + question);

            System.out.print("Вы: ");
            String userAnswer = scanner.nextLine();

            dialogHistory.add("User: " + userAnswer);
            continueDialogWithUser(userAnswer);

            String userAdditionalAnswer = scanner.nextLine();
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

        String prompt = String.format(
                "Ты - карьерный консультант. Продолжи диалог естественно и поддерживающе.\n" +
                        "История диалога:\n%s\n\n" +
                        "Твой ответ должен быть:\n" +
                        "- Кратким (1-2 предложения)\n" +
                        "- Поддерживающим\n" +
                        "- Побуждающим к размышлению\n" +
                        "- Естественным продолжением диалога\n\n" +
                        "Ответь на последнюю реплику пользователя: '%s'",
                context, userAnswer
        );
        return gigaChatApiService.sendMessage(prompt);
    }

    @Override
    public CourseRequirements analyzeCombinedData(FinalVacancyRequarements requarements) {
        String vacancyRequirements = requirements.getVacancyAllCompactRequirements();
        String context = String.join("\n", dialogHistory);

        String prompt =
                "Роль: Ты — эксперт по разработке образовательных программ и карьерный консультант.\n\n" +

                        "Контекст:\n" +
                        "Пользователь прошел собеседование-самооценку для подготовки к вакансии. " +
                        "На основе его ответов и требований вакансии нужно создать персонализированную учебную программу.\n\n" +

                        "Задача:\n" +
                        "Создай детальные требования к образовательному курсу, который подготовит пользователя к этой вакансии.\n\n" +

                        "Требования к курсу должны включать:\n" +
                        "1. Ключевые темы и модули (что именно изучать)\n" +
                        "2. Практические задания и проекты\n" +
                        "3. Ожидаемые результаты после каждого модуля\n" +
                        "4. Рекомендуемая длительность курса\n" +
                        "5. Критерии успешного завершения\n\n" +

                        "Учти из истории диалога:\n" +
                        "- Уровень текущих знаний пользователя\n" +
                        "- Его мотивацию и цели\n" +
                        "- Временные возможности\n" +
                        "- Страхи и пробелы в знаниях\n\n" +

                        "Формат вывода: структурированное описание курса с четкими требованиями.\n" +
                        "Исходные данные:\n" +
                        "1. ТРЕБОВАНИЯ ВАКАНСИИ:\n%s" + vacancyRequirements + "\n"  +
                        "2. ИСТОРИЯ ДИАЛОГА С ПОЛЬЗОВАТЕЛЕМ:\n%s" + context;

        return new CourseRequirements(gigaChatApiService.sendMessage(prompt));
    }
}
