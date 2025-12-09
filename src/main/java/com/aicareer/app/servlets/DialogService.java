package com.aicareer.app.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.aicareer.core.model.user.UserPreferences;
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

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("authenticated") == null) {
      response.sendRedirect(request.getContextPath() + "/login");
      return;
    }

    String chatType = request.getParameter("chatType");

    if ("clarification".equals(chatType)) {
      // Начинаем уточняющую беседу после показа вакансий
      startClarificationChat(session);
    }

    setupMessageHistory(request);
    request.getRequestDispatcher("/jsp/DialogService.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("authenticated") == null) {
      response.sendRedirect(request.getContextPath() + "/login");
      return;
    }

    String message = request.getParameter("message");
    String userEmail = (String) session.getAttribute("userEmail");

    System.out.println("📨 Message from " + userEmail + ": " + message);

    // Проверяем тип чата
    String chatType = (String) session.getAttribute("currentChatType");

    if ("clarification".equals(chatType)) {
      handleClarificationChat(session, message, response, request);
      return;
    }

    // Оригинальная логика для обычного диалога
    handleRegularDialog(session, message, response, request);
  }

  private void startClarificationChat(HttpSession session) {
    String selectedField = (String) session.getAttribute("selectedField");

    // Генерируем первый уточняющий вопрос
    String firstQuestion = generateFirstClarificationQuestion(selectedField);

    // Инициализируем историю для уточняющего чата
    List<String> clarificationHistory = new ArrayList<>();
    clarificationHistory.add(firstQuestion);

    session.setAttribute("clarificationChatHistory", clarificationHistory);
    session.setAttribute("clarificationQuestionCount", 1);
    session.setAttribute("currentChatType", "clarification");
    session.setAttribute("messageHistory", clarificationHistory); // Для отображения в JSP
  }

  private void handleClarificationChat(HttpSession session, String message,
      HttpServletResponse response, HttpServletRequest request)
      throws ServletException, IOException {

    List<String> clarificationHistory = (List<String>) session.getAttribute("clarificationChatHistory");
    Integer questionCount = (Integer) session.getAttribute("clarificationQuestionCount");
    String selectedField = (String) session.getAttribute("selectedField");

    // Добавляем ответ пользователя
    clarificationHistory.add(message);

    if (questionCount < 5) {
      // Генерируем следующий вопрос
      String nextQuestion = generateNextClarificationQuestion(clarificationHistory, selectedField, questionCount);
      clarificationHistory.add(nextQuestion);

      session.setAttribute("clarificationQuestionCount", questionCount + 1);
      session.setAttribute("clarificationChatHistory", clarificationHistory);
      session.setAttribute("messageHistory", clarificationHistory);

      setupMessageHistory(request);
      request.getRequestDispatcher("/jsp/DialogService.jsp").forward(request, response);
    } else {
      // Завершаем уточняющую беседу и генерируем roadmap
      completeClarificationAndGenerateRoadmap(session, clarificationHistory, selectedField, response, request);
    }
  }

  private String generateFirstClarificationQuestion(String field) {
    String prompt = "Пользователь выбрал направление: " + field +
        ". Он только что посмотрел 10 реальных вакансий с hh.ru в этой области. " +
        "Задай первый уточняющий вопрос чтобы понять его текущий уровень, опыт и цели. " +
        "Вопрос должен быть конкретным и помогать составить персонализированный roadmap.";
    return gigaChatService.sendMessage(prompt);
  }

  private String generateNextClarificationQuestion(List<String> history, String field, int currentQuestion) {
    StringBuilder chatContext = new StringBuilder();
    for (int i = 0; i < history.size(); i++) {
      if (i % 2 == 0) {
        chatContext.append("AI: ").append(history.get(i)).append("\n");
      } else {
        chatContext.append("User: ").append(history.get(i)).append("\n");
      }
    }

    String prompt = "История уточняющей беседы:\n" + chatContext.toString() +
        "\nНаправление: " + field +
        "\nЗадай следующий уточняющий вопрос (" + (currentQuestion + 1) + "/5). " +
        "Вопрос должен углублять понимание потребностей пользователя для составления roadmap.";
    return gigaChatService.sendMessage(prompt);
  }

  private void completeClarificationAndGenerateRoadmap(HttpSession session, List<String> history,
      String field, HttpServletResponse response,
      HttpServletRequest request)
      throws IOException, ServletException {

    StringBuilder fullDialog = new StringBuilder();
    for (int i = 0; i < history.size(); i++) {
      if (i % 2 == 0) {
        fullDialog.append("AI: ").append(history.get(i)).append("\n");
      } else {
        fullDialog.append("User: ").append(history.get(i)).append("\n");
      }
    }

    String roadmapPrompt = "На основе всего диалога и выбранного направления создай подробный roadmap.\n" +
        "Направление: " + field + "\n" +
        "Полный диалог:\n" + fullDialog.toString() + "\n\n" +
        "Создай структурированный план обучения с этапами, сроками и конкретными шагами.";

    String roadmap = gigaChatService.sendMessage(roadmapPrompt);
    session.setAttribute("finalRoadmap", roadmap);

    // Перенаправляем на страницу с roadmap
    response.sendRedirect(request.getContextPath() + "/career-roadmap");
  }

  private void handleRegularDialog(HttpSession session, String message,
      HttpServletResponse response, HttpServletRequest request)
      throws ServletException, IOException {

    // Ваша оригинальная логика для обычного диалога
    List<String> messageHistory = (List<String>) session.getAttribute("messageHistory");
    if (messageHistory == null) {
      messageHistory = new ArrayList<>();
    }

    if (message != null && (message.equalsIgnoreCase("/complete") ||
        message.equalsIgnoreCase("/finish") ||
        message.equalsIgnoreCase("завершить"))) {
      completeDialogAndRedirect(session, response, request);
      return;
    }

    if (message != null && !message.trim().isEmpty()) {
      messageHistory.add(message.trim());

      try {
        String prompt = buildPrompt(message, messageHistory);
        String aiResponse = gigaChatService.sendMessage(prompt);
        messageHistory.add(aiResponse);

        if (isDialogComplete(messageHistory)) {
          completeDialogAndRedirect(session, response, request);
          return;
        }
      } catch (Exception e) {
        String fallbackResponse = "Извините, в настоящее время сервис AI временно недоступен.";
        messageHistory.add(fallbackResponse);
      }

      session.setAttribute("messageHistory", messageHistory);
    }

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
    prompt.append("После ответа задай уточняющий вопрос по смежной теме. ");
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

  private boolean isDialogComplete(List<String> messageHistory) {
    // Логика определения завершения диалога
    if (messageHistory == null) return false;

    // Простая логика: если есть хотя бы 6 сообщений (3 от пользователя, 3 от AI)
    return messageHistory.size() >= 6;
  }

  private void completeDialogAndRedirect(HttpSession session, HttpServletResponse response, HttpServletRequest request)
      throws IOException {

    try {
      // Создаем UserPreferences на основе диалога
      UserPreferences userPreferences = new UserPreferences();

      // Заполняем данными из истории сообщений
      List<String> messageHistory = (List<String>) session.getAttribute("messageHistory");
      String userInfo = "Информация из диалога:\n";

      if (messageHistory != null && !messageHistory.isEmpty()) {
        for (int i = 0; i < messageHistory.size(); i += 2) {
          if (i < messageHistory.size()) {
            userInfo += "Пользователь: " + messageHistory.get(i) + "\n";
          }
          if (i + 1 < messageHistory.size()) {
            userInfo += "AI: " + messageHistory.get(i + 1) + "\n";
          }
        }
      } else {
        userInfo = "Пользователь прошел краткий диалог";
      }

      userPreferences.setInfoAboutPerson(userInfo);

      Long userId = (Long) session.getAttribute("userId");
      if (userId != null) {
        userPreferences.setUserId(userId);
      }

      // Сохраняем в сессии
      session.setAttribute("userPreferences", userPreferences);

      System.out.println("✅ Диалог завершен, UserPreferences сохранены");

      // Перенаправляем на выбор вакансии
      response.sendRedirect(request.getContextPath() + "/choose-vacancy");

    } catch (Exception e) {
      System.err.println("❌ Ошибка при завершении диалога: " + e.getMessage());
      e.printStackTrace();
      response.sendRedirect(request.getContextPath() + "/send-message");
    }
  }
}