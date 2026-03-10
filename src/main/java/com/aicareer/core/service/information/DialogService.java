package com.aicareer.core.service.information;

import com.aicareer.core.service.gigachat.GigaChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Scanner;

@Slf4j
@Service
@RequiredArgsConstructor
public class DialogService {

  private final GigaChatService gigaChatService;
  private final boolean testMode = false; // можно вынести в конфигурацию

  private Scanner scanner = new Scanner(System.in);

  public String userAnswer(String question, String context) {
    if (testMode) {
      return userAnswerTest(question, context);
    } else {
      return userAnswerReal(question, context);
    }
  }

  public String userAnswerReal(String question, String context) {
    System.out.println("AI: " + question);
    System.out.print("👉 Ваш ответ: ");
    String userAnswer = scanner.nextLine();
    return userAnswer;
  }

  public String userAnswerTest(String question, String context) {
    System.out.println("AI: " + question);
    String aiAnswer = generateAiAnswer(question, context);
    System.out.println("User: " + aiAnswer);
    return aiAnswer;
  }

  private String generateAiAnswer(String question, String context) {
    String prompt = """
            Ты — Алексей, 28-летний менеджер по продажам. Ищешь новую карьеру через "Карьерный.Навигатор". Главная цель — значительно повысить зарплату для крупных покупок. Устал от роли исполнителя, хочешь перспективную работу с измеримым результатом.
            
            Твои черты:
            - Прагматик: веришь в связь «усилия → доход»
            - Амбициозен: готов усердно учиться для быстрого роста
            - Ценишь структуру: нужен четкий план развития
            
            Текущая ситуация:
            - Навыки: базовый Excel, хорошие коммуникативные способности
            - Рассматриваешь Data Analysis, Product Management, Digital-маркетинг, но не уверен в выборе
            - Скептически относишься к коротким курсам, ищешь основательный подход
            
            Твоя роль:
            Отвечай кратко и по делу на вопросы карьерного консультанта. Не задавай встречных вопросов. Делись мыслями, сомнениями, приоритетами (деньги, рост, стабильность). Будь честным, но лаконичным.
            
            Входной вопрос:
            %s
            
            Контекст диалога:
            %s
            """;
    return gigaChatService.sendMessage(String.format(prompt, question, context));
  }
}