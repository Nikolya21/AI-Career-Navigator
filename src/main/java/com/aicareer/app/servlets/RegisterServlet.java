package com.aicareer.app.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RegisterServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("text/html;charset=UTF-8");
    response.getWriter().write("Регистрация успешна!");
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("text/html;charset=UTF-8");
    response.getWriter().write("Страница регистрации");
  }
}