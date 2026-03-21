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
import java.util.ArrayList;
import java.util.List;

@WebServlet("/real-vacancies")
public class RealVacanciesServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // Установите кодировку ПЕРЕД любыми операциями
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");
    response.setContentType("text/html; charset=UTF-8");


    // Проверяем аутентификацию
    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("authenticated") == null) {
      response.sendRedirect(request.getContextPath() + "/login");
      return;
    }

    // Получаем выбранную вакансию из сессии
    SelectedPotentialVacancy selectedVacancy = (SelectedPotentialVacancy) session.getAttribute("selectedVacancy");
    String selectedVacancyName = (String) session.getAttribute("selectedVacancyName");

    if (selectedVacancy != null || selectedVacancyName != null) {
      try {
        String vacancyName = (selectedVacancy != null) ? selectedVacancy.getNameOfVacancy() : selectedVacancyName;
        System.out.println("🔍 Парсим реальные вакансии для: " + vacancyName);

        // Используем ваш ParserService для получения реальных вакансий
        List<RealVacancy> realVacancies = ParserService.getVacancies(vacancyName, "1", 10);

        System.out.println("✅ Получено реальных вакансий: " + (realVacancies != null ? realVacancies.size() : 0));

        // Декодируем все текстовые поля в вакансиях
        if (realVacancies != null) {
          realVacancies = decodeVacancies(realVacancies);
        }

        // Передаем данные в JSP
        request.setAttribute("realVacancies", realVacancies);
        request.setAttribute("selectedVacancy", vacancyName);

      } catch (Exception e) {
        System.err.println("❌ Ошибка при парсинге вакансий: " + e.getMessage());
        e.printStackTrace();
        request.setAttribute("error", "Ошибка при загрузке реальных вакансий: " + e.getMessage());
      }
    } else {
      System.out.println("❌ Вакансия не выбрана в сессии");
      request.setAttribute("error", "Вакансия не выбрана. Вернитесь к выбору вакансии.");
    }

    request.getRequestDispatcher("/jsp/RealVacancies.jsp").forward(request, response);
  }

  /**
   * Декодирует все текстовые поля в вакансиях
   */
  private List<RealVacancy> decodeVacancies(List<RealVacancy> vacancies) {

    if (vacancies == null) return vacancies;

    for (RealVacancy vacancy : vacancies) {
      if (vacancy.getNameOfVacancy() != null) {
        vacancy.setNameOfVacancy(fixEncoding(vacancy.getNameOfVacancy()));
      }
      if (vacancy.getEmployer() != null) {
        vacancy.setEmployer(fixEncoding(vacancy.getEmployer()));
      }
      if (vacancy.getExperience() != null) {
        vacancy.setExperience(fixEncoding(vacancy.getExperience()));
      }
      if (vacancy.getAge() != null) {
        vacancy.setAge(fixEncoding(vacancy.getAge()));
      }

      // Декодируем навыки
      if (vacancy.getVacancyRequirements() != null) {
        List<String> decodedSkills = new ArrayList<>();
        for (String skill : vacancy.getVacancyRequirements()) {
          decodedSkills.add(fixEncoding(skill));
        }
        vacancy.setVacancyRequirements(decodedSkills);
      }
    }

    return vacancies;
  }

  /**
   * Исправляет проблемы с кодировкой
   */
  private String fixEncoding(String text) {
    if (text == null) return null;

    try {
      // Если текст выглядит как UTF-8, ошибочно прочитанный как ISO-8859-1
      if (text.contains("Р") && text.contains("С")) {
        // Попробуем конвертировать из неправильной кодировки
        byte[] bytes = text.getBytes("ISO-8859-1");
        return new String(bytes, "UTF-8");
      }

      // Или если есть HTML-сущности
      if (text.contains("&")) {
        text = text
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&amp;", "&");
      }

      return text;

    } catch (Exception e) {
      System.err.println("Ошибка при исправлении кодировки: " + e.getMessage());
      return text;
    }
  }
}