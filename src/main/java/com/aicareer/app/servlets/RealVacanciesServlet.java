package com.aicareer.app.servlets;

import com.aicareer.core.model.vacancy.RealVacancy;
import com.aicareer.core.model.vacancy.SelectedPotentialVacancy;
import com.aicareer.core.service.parserOfVacancy.SelectVacancy;
import com.aicareer.core.service.gigachat.GigaChatService;
import com.aicareer.core.service.parserOfVacancy.ParserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/real-vacancies")
public class RealVacanciesServlet extends HttpServlet {

  private SelectVacancy selectVacancy;

  @Override
  public void init() throws ServletException {
    super.init();
    this.selectVacancy = new SelectVacancy(new GigaChatService());
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // Проверяем аутентификацию
    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("authenticated") == null) {
      response.sendRedirect(request.getContextPath() + "/login");
      return;
    }

    // Получаем выбранную вакансию из сессии
    SelectedPotentialVacancy selectedVacancy = (SelectedPotentialVacancy) session.getAttribute("selectedVacancy");

    if (selectedVacancy != null) {
      try {
        String vacancyName = selectedVacancy.getNameOfVacancy();
        System.out.println("🔍 Парсим реальные вакансии для: " + vacancyName);

        // Используем ваш ParserService для получения реальных вакансий
        List<RealVacancy> realVacancies = ParserService.getVacancies(vacancyName, "1", 10);

        System.out.println("✅ Получено реальных вакансий: " + realVacancies.size());

        // Передаем данные в JSP
        request.setAttribute("realVacancies", realVacancies);
        request.setAttribute("selectedVacancy", vacancyName);

      } catch (Exception e) {
        System.err.println("❌ Ошибка при парсинге вакансий: " + e.getMessage());
        e.printStackTrace();
        request.setAttribute("error", "Ошибка при загрузке реальных вакансий: " + e.getMessage());
      }
    } else {
      request.setAttribute("error", "Вакансия не выбрана. Вернитесь к выбору вакансии.");
    }

    request.getRequestDispatcher("/jsp/RealVacancies.jsp").forward(request, response);
  }
}