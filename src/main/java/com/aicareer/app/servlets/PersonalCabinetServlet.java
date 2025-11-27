package com.aicareer.app.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;

@WebServlet("/personal-cabinet")
public class PersonalCabinetServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

    // Проверяем аутентификацию пользователя
    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("authenticated") == null) {
      response.sendRedirect(request.getContextPath() + "/login");
      return;
    }

    // Устанавливаем дополнительные данные, если их нет
    if (session.getAttribute("registrationDate") == null) {
      session.setAttribute("registrationDate", new Date());
    }

    if (session.getAttribute("userName") == null) {
      String userEmail = (String) session.getAttribute("userEmail");
      if (userEmail != null) {
        // Извлекаем имя из email (часть до @)
        String userName = userEmail.split("@")[0];
        session.setAttribute("userName", userName);
      }
    }

    request.getRequestDispatcher("/jsp/personal-cabinet.jsp").forward(request, response);
  }
}