package com.aicareer.app.servlets;

import com.aicareer.core.service.gigachat.GigaChatService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/send-message")
public class DialogService extends HttpServlet {

  private final GigaChatService gigaChatService = new GigaChatService();

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Инициализируем сессию для хранения истории диалога
    HttpSession session = request.getSession();
    if (session.getAttribute("dialogHistory") == null) {
      List<String> dialogHistory = new ArrayList<>();
      dialogHistory.add("AI: Here we can discuss your learning plan and create a personal path to your dream. To do this, I need to get to know you better... Tell me about your experience in programming");
      session.setAttribute("dialogHistory", dialogHistory);
    }

    request.getRequestDispatcher("/jsp/DialogService.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String userMessage = request.getParameter("message");

    if (userMessage == null || userMessage.trim().isEmpty()) {
      response.sendRedirect(request.getContextPath() + "/send-message");
      return;
    }

    HttpSession session = request.getSession();
    List<String> dialogHistory = (List<String>) session.getAttribute("dialogHistory");

    if (dialogHistory == null) {
      dialogHistory = new ArrayList<>();
      dialogHistory.add("AI: Here we can discuss your learning plan and create a personal path to your dream. To do this, I need to get to know you better... Tell me about your experience in programming");
    }

    // Добавляем сообщение пользователя в историю
    dialogHistory.add("User: " + userMessage.trim());

    // Генерируем ответ от нейросети
    try {
      String aiResponse = generateAIResponse(userMessage, String.join("\n", dialogHistory));
      dialogHistory.add("AI: " + aiResponse);
    } catch (Exception e) {
      dialogHistory.add("AI: Sorry, I'm having trouble responding right now. Please try again.");
      e.printStackTrace();
    }

    // Сохраняем обновленную историю в сессии
    session.setAttribute("dialogHistory", dialogHistory);

    // Перенаправляем обратно на страницу диалога
    response.sendRedirect(request.getContextPath() + "/send-message");
  }

  private String generateAIResponse(String userMessage, String context) {
    String prompt = String.format(
        "Ты — карьерный консультант. Помоги пользователю с карьерным планированием. " +
            "Отвечай кратко и по делу.\n\n" +
            "Последнее сообщение пользователя: %s\n\n" +
            "Контекст предыдущего диалога:\n%s\n\n" +
            "Ответь на русском языке:",
        userMessage, context
    );

    return gigaChatService.sendMessage(prompt);
  }
}