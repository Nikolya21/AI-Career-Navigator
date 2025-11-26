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

      try {
        // Генерируем промпт с контекстом для нейросети
        String prompt = buildPrompt(message, messageHistory);
        System.out.println("🤖 Sending prompt to AI: " + prompt);

        // Получаем ответ от реальной нейросети
        String aiResponse = gigaChatService.sendMessage(prompt);
        System.out.println("🤖 AI Response: " + aiResponse);

        // Добавляем ответ AI
        messageHistory.add(aiResponse);

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
    }
  }

  private String buildPrompt(String currentMessage, List<String> messageHistory) {
    StringBuilder prompt = new StringBuilder();

    // Системный промпт для нейросети
    prompt.append("Ты - AI помощник по карьерному развитию 'Career Navigator'. ");
    prompt.append("Твоя роль - помогать пользователям с вопросами карьеры, обучения и профессионального развития. ");
    prompt.append("Отвечай профессионально, но дружелюбно. Будь полезным и поддерживающим. ");
    prompt.append("Фокусируйся на карьерных темах: профориентация, навыки, обучение, поиск работы, карьерный рост. ");
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
}