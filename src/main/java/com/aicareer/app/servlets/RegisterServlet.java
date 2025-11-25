package com.aicareer.app.servlets;

import java.io.IOException;
import java.util.List;

import com.aicareer.core.dto.user.UserRegistrationDto;
import com.aicareer.core.service.user.UserService;
import com.aicareer.core.service.user.impl.UserServiceImpl;
import com.aicareer.core.service.user.model.RegistrationResult;
import com.aicareer.core.validator.user.RegistrationValidator;
import com.aicareer.repository.user.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

  private UserService userService;

  @Override
  public void init() throws ServletException {
    super.init();
    // Инициализация сервиса с зависимостями
    // В реальном приложении используйте Dependency Injection
    UserRepository userRepository = null; // Здесь должен быть реальный репозиторий
    // Для тестирования создаем сервис с null зависимостями
    this.userService = new UserServiceImpl(userRepository, null, null, null);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String name = request.getParameter("name");
    String email = request.getParameter("email");
    String password = request.getParameter("password");
    String confirmPassword = request.getParameter("confirmPassword");

    // Проверка подтверждения пароля
    if (!password.equals(confirmPassword)) {
      request.setAttribute("errors", List.of("Пароли не совпадают"));
      request.setAttribute("email", email);
      request.setAttribute("name", name);
      request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
      return;
    }

    // Создаем DTO для регистрации
    UserRegistrationDto registrationDto = new UserRegistrationDto();
    registrationDto.setEmail(email);
    registrationDto.setPassword(password);
    registrationDto.setName(name);

    // Вызываем сервис для регистрации
    RegistrationResult result = userService.registerUser(registrationDto);

    if (result.isSuccess()) {
      // Успешная регистрация - перенаправляем на страницу логина
      response.sendRedirect(request.getContextPath() + "/login?registered=true");
    } else {
      // Ошибки регистрации - показываем форму снова
      request.setAttribute("errors", result.getErrors());
      request.setAttribute("email", email);
      request.setAttribute("name", name);
      request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
    }
  }
}