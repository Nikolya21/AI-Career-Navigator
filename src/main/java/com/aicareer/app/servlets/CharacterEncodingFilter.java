package com.aicareer.app.servlets;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*") // Фильтр применяется ко всем URL
public class CharacterEncodingFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    System.out.println("✅ CharacterEncodingFilter инициализирован");
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    // Приводим к HTTP типу
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    // Устанавливаем UTF-8 для ВСЕХ запросов и ответов
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");

    // Для HTML устанавливаем Content-Type
    if (httpRequest.getRequestURI().endsWith(".jsp") ||
        httpRequest.getRequestURI().endsWith(".html")) {
      response.setContentType("text/html; charset=UTF-8");
    }

    // Для API/JSON устанавливаем соответствующий Content-Type
    if (httpRequest.getRequestURI().contains("/api/") ||
        httpRequest.getRequestURI().endsWith(".json")) {
      response.setContentType("application/json; charset=UTF-8");
    }

    // Логируем для отладки
    System.out.println("🔧 Фильтр кодировки: " + httpRequest.getRequestURI() +
        " | Encoding: UTF-8");

    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
    System.out.println("❌ CharacterEncodingFilter уничтожен");
  }
}