package com.aicareer.app.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.aicareer.core.service.gigachat.GigaChatService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/send-message")
public class DialogService extends HttpServlet {

  private GigaChatService gigaChatService;

  @Override
  public void init() throws ServletException {
    super.init();
    this.gigaChatService = new GigaChatService();
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // Проверяем аутентификацию пользователя
    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("authenticated") == null) {
      response.sendRedirect(request.getContextPath() + "/login");
      return;
    }

    // Проверяем, не завершен ли уже диалог
    Boolean dialogCompleted = (Boolean) session.getAttribute("dialogCompleted");
    if (dialogCompleted != null && dialogCompleted) {
      response.sendRedirect(request.getContextPath() + "/dialog-completed");
      return;
    }

    // Устанавливаем атрибуты для отображения истории
    setupMessageHistory(request);

    request.getRequestDispatcher("/jsp/DialogService.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // Проверяем аутентификацию пользователя
    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("authenticated") == null) {
      response.sendRedirect(request.getContextPath() + "/login");
      return;
    }

    // Проверяем, не завершен ли уже диалог
    Boolean dialogCompleted = (Boolean) session.getAttribute("dialogCompleted");
    if (dialogCompleted != null && dialogCompleted) {
      response.sendRedirect(request.getContextPath() + "/dialog-completed");
      return;
    }

    String message = request.getParameter("message");
    String userEmail = (String) session.getAttribute("userEmail");

    System.out.println("📨 Message from " + userEmail + ": " + message);

    if (message != null && !message.trim().isEmpty()) {
      // Получаем или создаем историю сообщений
      List<String> messageHistory = (List<String>) session.getAttribute("messageHistory");
      if (messageHistory == null) {
        messageHistory = new ArrayList<>();
      }

      // Добавляем сообщение пользователя
      messageHistory.add(message.trim());

      // Подсчитываем количество вопросов пользователя (каждое второе сообщение)
      int userQuestionsCount = (messageHistory.size() + 1) / 2;
      System.out.println("❓ User questions count: " + userQuestionsCount);

      try {
        // Проверяем, не достигли ли лимита в 5 вопросов
        if (userQuestionsCount >= 5) {
          // Лимит достигнут - отправляем финальное сообщение
          String finalResponse = buildFinalResponse(messageHistory);
          messageHistory.add(finalResponse);

          // Помечаем диалог как завершенный
          session.setAttribute("dialogCompleted", true);
          session.setAttribute("dialogEndTime", System.currentTimeMillis());

          System.out.println("🎯 Dialog completed after " + userQuestionsCount + " questions");

        } else {
          // Продолжаем обычный диалог
          String prompt = buildPrompt(message, messageHistory, userQuestionsCount);
          System.out.println("🤖 Sending prompt to AI: " + prompt);

          // Получаем ответ от реальной нейросети
          String aiResponse = gigaChatService.sendMessage(prompt);
          System.out.println("🤖 AI Response: " + aiResponse);

          // Добавляем ответ AI
          messageHistory.add(aiResponse);
        }

      } catch (Exception e) {
        System.err.println("❌ Error calling AI service: " + e.getMessage());
        // Fallback ответ в случае ошибки
        String fallbackResponse = "Извините, в настоящее время сервис AI временно недоступен. Пожалуйста, попробуйте позже.";
        messageHistory.add(fallbackResponse);
      }

      // Сохраняем историю в сессии
      session.setAttribute("messageHistory", messageHistory);
      System.out.println("✅ Message history updated. Total messages: " + messageHistory.size());
    }

    // Проверяем снова, не завершился ли диалог
    dialogCompleted = (Boolean) session.getAttribute("dialogCompleted");
    if (dialogCompleted != null && dialogCompleted) {
      response.sendRedirect(request.getContextPath() + "/dialog-completed");
      return;
    }

    // ВМЕСТО redirect используем forward чтобы сохранить данные
    setupMessageHistory(request);
    request.getRequestDispatcher("/jsp/DialogService.jsp").forward(request, response);
  }

  private void setupMessageHistory(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      List<String> messageHistory = (List<String>) session.getAttribute("messageHistory");
      if (messageHistory != null) {
        request.setAttribute("messageHistory", messageHistory);
      }

      // Добавляем информацию о статусе диалога
      Boolean dialogCompleted = (Boolean) session.getAttribute("dialogCompleted");
      request.setAttribute("dialogCompleted", dialogCompleted != null && dialogCompleted);

      // Считаем количество вопросов для отображения прогресса
      if (messageHistory != null) {
        int questionsCount = (messageHistory.size() + 1) / 2;
        request.setAttribute("questionsCount", questionsCount);
      }
    }
  }

  private String buildPrompt(String currentMessage, List<String> messageHistory, int questionsCount) {
    StringBuilder prompt = new StringBuilder();

    // Системный промпт для нейросети
    prompt.append("Ты - AI помощник по карьерному развитию 'Career Navigator'. ");
    prompt.append("Твоя роль - помогать пользователям с вопросами карьеры, обучения и профессионального развития. ");
    prompt.append("Отвечай профессионально, но дружелюбно. Будь полезным и поддерживающим. ");
    prompt.append("Фокусируйся на карьерных темах: профориентация, навыки, обучение, поиск работы, карьерный рост. ");
    prompt.append("Это вопрос номер ").append(questionsCount).append(" из 5. ");
    prompt.append("После 5 вопросов диалог будет завершен. ");
    prompt.append("Если вопрос не по теме, вежливо направляй разговор в профессиональное русло.\n\n");

    // Добавляем историю диалога для контекста
    if (messageHistory != null && messageHistory.size() > 1) {
      prompt.append("Контекст предыдущего диалога:\n");
      for (int i = 0; i < messageHistory.size() - 1; i += 2) {
        if (i < messageHistory.size() - 1) {
          prompt.append("Пользователь: ").append(messageHistory.get(i)).append("\n");
        }
        if (i + 1 < messageHistory.size() - 1) {
          prompt.append("AI: ").append(messageHistory.get(i + 1)).append("\n");
        }
      }
      prompt.append("\n");
    }

    // Текущее сообщение пользователя
    prompt.append("Текущий вопрос пользователя: ").append(currentMessage).append("\n\n");
    prompt.append("Ответь на вопрос пользователя, учитывая контекст диалога:");

    return prompt.toString();
  }

  private String buildFinalResponse(List<String> messageHistory) {
    StringBuilder finalPrompt = new StringBuilder();

    finalPrompt.append("Ты - AI помощник по карьерному развитию 'Career Navigator'. ");
    finalPrompt.append("Пользователь задал 5 вопросов и диалог завершается. ");
    finalPrompt.append("Напиши финальное, завершающее сообщение которое:\n");
    finalPrompt.append("1. Подводит итоги диалога\n");
    finalPrompt.append("2. Дает общие рекомендации по карьерному развитию\n");
    finalPrompt.append("3. Побуждает пользователя к действию\n");
    finalPrompt.append("4. Прощается и желает успехов\n");
    finalPrompt.append("5. Сообщает что диалог завершен\n\n");

    finalPrompt.append("История диалога:\n");
    for (int i = 0; i < messageHistory.size(); i += 2) {
      if (i < messageHistory.size()) {
        finalPrompt.append("Пользователь: ").append(messageHistory.get(i)).append("\n");
      }
      if (i + 1 < messageHistory.size()) {
        finalPrompt.append("AI: ").append(messageHistory.get(i + 1)).append("\n");
      }
    }
    finalPrompt.append("\nНапиши финальное сообщение:");

    try {
      return gigaChatService.sendMessage(finalPrompt.toString());
    } catch (Exception e) {
      System.err.println("❌ Error generating final response: " + e.getMessage());
      return "Благодарю за диалог! Вы задали 5 вопросов, и наша беседа подошла к концу. " +
          "Надеюсь, я смог помочь вам с вопросами карьерного развития. " +
          "Желаю успехов в профессиональном росте и достижении ваших целей! " +
          "Диалог завершен.";
    }
  }
}