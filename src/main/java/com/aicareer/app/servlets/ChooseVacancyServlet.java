package com.aicareer.app.servlets;

import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.core.model.vacancy.SelectedPotentialVacancy;
import com.aicareer.core.model.vacancy.PotentialVacancy;
import com.aicareer.core.service.parserOfVacancy.SelectVacancy;
import com.aicareer.core.service.gigachat.GigaChatService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/choose-vacancy")
public class ChooseVacancyServlet extends HttpServlet {

  private SelectVacancy selectVacancy;

  @Override
  public void init() throws ServletException {
    super.init();
    this.selectVacancy = new SelectVacancy(new GigaChatService());
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // Установка кодировки UTF-8 для корректного отображения русских символов
    response.setContentType("text/html; charset=UTF-8");
    response.setCharacterEncoding("UTF-8");
    request.setCharacterEncoding("UTF-8");

    // Проверяем аутентификацию
    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("authenticated") == null) {
      response.sendRedirect(request.getContextPath() + "/login");
      return;
    }

    // Получаем UserPreferences из сессии
    UserPreferences userPreferences = (UserPreferences) session.getAttribute("userPreferences");

    if (userPreferences != null) {
      try {
        // Используем вашу существующую логику для получения вакансий
        System.out.println("🔍 Начало процесса подбора вакансий...");

        // 1. Анализ предпочтений и извлечение вакансий
        String analysisResult = selectVacancy.analyzeUserPreference(userPreferences);
        List<String> threeVacancies = selectVacancy.extractThreeVacancies(analysisResult);

        // Повторяем попытку если список пустой
        if (threeVacancies.isEmpty()) {
          threeVacancies = selectVacancy.extractThreeVacancies(analysisResult);
        }

        System.out.println("✅ Извлечено вакансий: " + threeVacancies.size());
        System.out.println("📋 Вакансии: " + threeVacancies);

        // Сохраняем в сессии для использования в POST
        session.setAttribute("suggestedVacancies", threeVacancies);
        session.setAttribute("analysisResult", analysisResult);

        // Передаем данные в JSP
        request.setAttribute("analysisResult", analysisResult);
        request.setAttribute("suggestedVacancies", threeVacancies);

      } catch (Exception e) {
        System.err.println("❌ Ошибка при анализе вакансий: " + e.getMessage());
        e.printStackTrace();
        request.setAttribute("error", "Ошибка при подборе вакансий: " + e.getMessage());

        // Показываем тестовые вакансии при ошибке
        showTestVacancies(request);
      }
    } else {
      request.setAttribute("error", "Данные пользователя не найдены. Пройдите диалог заново.");
      // Показываем тестовые вакансии для отладки
      showTestVacancies(request);
    }

    request.getRequestDispatcher("/jsp/ChooseVacancy.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // Установка кодировки UTF-8 для корректного отображения русских символов
    response.setContentType("text/html; charset=UTF-8");
    response.setCharacterEncoding("UTF-8");
    request.setCharacterEncoding("UTF-8");

    // Проверяем аутентификацию
    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("authenticated") == null) {
      response.sendRedirect(request.getContextPath() + "/login");
      return;
    }

    String selectedVacancyName = request.getParameter("selectedVacancy");

    if (selectedVacancyName == null || selectedVacancyName.trim().isEmpty()) {
      request.setAttribute("error", "Пожалуйста, выберите вакансию");
      // Восстанавливаем список вакансий
      restoreVacanciesFromSession(request, session);
      request.getRequestDispatcher("/jsp/ChooseVacancy.jsp").forward(request, response);
      return;
    }

    try {
      // Получаем список вакансий из сессии
      List<String> suggestedVacancies = (List<String>) session.getAttribute("suggestedVacancies");

      if (suggestedVacancies == null || !suggestedVacancies.contains(selectedVacancyName)) {
        request.setAttribute("error", "Выбрана недопустимая вакансия");
        restoreVacanciesFromSession(request, session);
        request.getRequestDispatcher("/jsp/ChooseVacancy.jsp").forward(request, response);
        return;
      }

      // Создаем SelectedPotentialVacancy согласно вашей логике
      PotentialVacancy potentialVacancy = new PotentialVacancy();
      potentialVacancy.setNameOfVacancy(selectedVacancyName);

      SelectedPotentialVacancy selectedVacancy = new SelectedPotentialVacancy(potentialVacancy);

      // Сохраняем выбранную вакансию в сессии
      session.setAttribute("selectedVacancy", selectedVacancy);
      session.setAttribute("selectedVacancyName", selectedVacancyName);

      System.out.println("✅ Пользователь выбрал вакансию: " + selectedVacancyName);

      // Здесь можно вызвать дальнейшую обработку как в вашем коде
      // userService.updateVacancy(selectedVacancyName, userId);

      // Перенаправляем на следующий этап (roadmap или следующий шаг)
      response.sendRedirect(request.getContextPath() + "/real-vacancies");

    } catch (Exception e) {
      System.err.println("❌ Ошибка при обработке выбора вакансии: " + e.getMessage());
      e.printStackTrace();
      request.setAttribute("error", "Ошибка при обработке выбора: " + e.getMessage());
      restoreVacanciesFromSession(request, session);
      request.getRequestDispatcher("/jsp/ChooseVacancy.jsp").forward(request, response);
    }
  }

  private void showTestVacancies(HttpServletRequest request) {
    List<String> testVacancies = List.of(
        "Java Developer",
        "Frontend Developer",
        "Data Scientist"
    );
    request.setAttribute("suggestedVacancies", testVacancies);
    request.setAttribute("analysisResult", "Тестовый анализ: пользователь подходит для IT-профессий");
  }

  private void restoreVacanciesFromSession(HttpServletRequest request, HttpSession session) {
    List<String> suggestedVacancies = (List<String>) session.getAttribute("suggestedVacancies");
    String analysisResult = (String) session.getAttribute("analysisResult");

    if (suggestedVacancies != null) {
      request.setAttribute("suggestedVacancies", suggestedVacancies);
    }
    if (analysisResult != null) {
      request.setAttribute("analysisResult", analysisResult);
    }
  }
}