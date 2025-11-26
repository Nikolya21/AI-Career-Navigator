package com.aicareer.app.servlets;

import com.aicareer.core.dto.user.LoginRequestDto;
import com.aicareer.core.validator.user.AuthenticationValidator;
import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@WebServlet("/send-message")
public class DialogService extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    request.getRequestDispatcher("/jsp/DialogService.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String email = request.getParameter("email");
    String password = request.getParameter("password");

    System.out.println(
        "DEBUG: email = [" + email + "], length = " + (email != null ? email.length() : "null"));
    System.out.println(
        "DEBUG: password = [" + password + "], length = " + (password != null ? password.length()
            : "null"));

    LoginRequestDto dto = new LoginRequestDto(email, password);
    List<String> errors = AuthenticationValidator.validate(dto);

    System.out.println("DEBUG: errors = " + errors);

    if (errors.isEmpty()) {
      response.sendRedirect(request.getContextPath() + "/wrong.html");
    } else {
      request.setAttribute("errors", errors);
      request.setAttribute("email", email);
      request.getRequestDispatcher("/jsp/login.jsp").forward(request, response);
    }
    String message = request.getParameter("message");

  }
}
