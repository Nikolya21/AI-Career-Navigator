package com.aicareer.app.servlets;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/dialog-completed")
public class DialogCompletedServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // Проверяем аутентификацию пользователя
    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("authenticated") == null) {
      response.sendRedirect(request.getContextPath() + "/login");
      return;
    }

    // Проверяем, что диалог действительно завершен
    Boolean dialogCompleted = (Boolean) session.getAttribute("dialogCompleted");
    if (dialogCompleted == null || !dialogCompleted) {
      response.sendRedirect(request.getContextPath() + "/send-message");
      return;
    }

    // Передаем историю сообщений на страницу завершения
    List<String> messageHistory = (List<String>) session.getAttribute("messageHistory");
    request.setAttribute("messageHistory", messageHistory);

    // Передаем количество вопросов
    if (messageHistory != null) {
      int questionsCount = (messageHistory.size() + 1) / 2;
      request.setAttribute("questionsCount", questionsCount);
    }

    request.getRequestDispatcher("/jsp/DialogCompleted.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // Обработка возможности начать новый диалог
    HttpSession session = request.getSession(false);
    if (session != null) {
      // Очищаем историю и статус для нового диалога
      session.removeAttribute("messageHistory");
      session.removeAttribute("dialogCompleted");
      session.removeAttribute("dialogEndTime");
    }

    response.sendRedirect(request.getContextPath() + "/send-message");
  }
}