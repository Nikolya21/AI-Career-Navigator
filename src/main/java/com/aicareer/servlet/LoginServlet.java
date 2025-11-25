package com.aicareer.servlet;

import java.io.IOException;

import com.aicareer.core.dto.user.LoginRequestDto;
import com.aicareer.core.validator.user.AuthenticationValidator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/login")
public class LoginServlet extends HttpServlet{
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    request.getRequestDispatcher("/jsp/login.jsp").forward(request, response);
  }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        LoginRequestDto loginRequestDto = new LoginRequestDto(email, password);
        var errors = AuthenticationValidator.validate(loginRequestDto);
        if (AuthenticationValidator.validate(loginRequestDto).size() == 0) {
            response.sendRedirect("/success.html");
        } else {
            request.setAttribute("errors", errors);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/jsp/login.jsp").forward(request, response);
        }
    }
}
