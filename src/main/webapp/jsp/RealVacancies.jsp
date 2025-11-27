<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.aicareer.core.model.vacancy.RealVacancy" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Реальные вакансии - Career Navigator</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/DialogService.css">
    <style>
      .vacancies-container {
        max-width: 1000px;
        margin: 0 auto;
        padding: 20px;
      }

      .vacancies-header {
        text-align: center;
        margin-bottom: 30px;
      }

      .vacancy-card {
        background: white;
        border-radius: 12px;
        padding: 25px;
        margin-bottom: 20px;
        box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
        border: 1px solid #e1e8ed;
        transition: all 0.3s ease;
      }

      .vacancy-card:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
      }

      .vacancy-title {
        font-size: 20px;
        font-weight: 600;
        color: #2d3748;
        margin-bottom: 10px;
      }

      .vacancy-salary {
        font-size: 18px;
        font-weight: 600;
        color: #28a745;
        margin-bottom: 15px;
      }

      .vacancy-requirements {
        margin-bottom: 15px;
      }

      .requirement-item {
        background: #f8f9fa;
        padding: 8px 12px;
        margin: 5px 0;
        border-radius: 6px;
        font-size: 14px;
        color: #4a5568;
      }

      .vacancy-meta {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 15px;
        padding-top: 15px;
        border-top: 1px solid #e1e8ed;
      }

      .company-info {
        font-size: 14px;
        color: #666;
      }

      .vacancy-source {
        background: #e7f3ff;
        color: #007BFF;
        padding: 4px 8px;
        border-radius: 4px;
        font-size: 12px;
        font-weight: 500;
      }

      .error-message {
        background: #ffeaea;
        color: #d63031;
        padding: 15px;
        border-radius: 8px;
        margin-bottom: 20px;
        text-align: center;
      }

      .info-box {
        background: #e7f3ff;
        border: 1px solid #b3d9ff;
        border-radius: 8px;
        padding: 20px;
        margin-bottom: 25px;
      }

      .back-btn {
        display: inline-block;
        background: #6c757d;
        color: white;
        padding: 10px 20px;
        border-radius: 8px;
        text-decoration: none;
        margin-bottom: 20px;
        transition: all 0.3s ease;
      }

      .back-btn:hover {
        background: #5a6268;
        color: white;
        text-decoration: none;
      }

      .vacancy-count {
        background: #007BFF;
        color: white;
        padding: 5px 10px;
        border-radius: 20px;
        font-size: 14px;
        font-weight: 500;
      }
    </style>
</head>
<body>
<div class="header">
    <div class="header-left">
        <a href="${pageContext.request.contextPath}/personal-cabinet" class="cabinet-btn">
            👤 Личный кабинет
        </a>
    </div>
    <h1>Career Navigator</h1>
    <div class="user-info">
        <%
            String userEmail = (String) session.getAttribute("userEmail");
            if (userEmail != null) {
        %>
        <span>Вы вошли как: <%= userEmail %></span>
        <a href="${pageContext.request.contextPath}/logout" class="logout-btn">Выйти</a>
        <% } %>
    </div>
</div>
<div style="text-align: center; margin: 30px 0;">
    <a href="${pageContext.request.contextPath}/vacancy-discussion"
       style="background: linear-gradient(135deg, #667eea, #764ba2);
              color: white;
              padding: 15px 30px;
              border-radius: 10px;
              text-decoration: none;
              font-size: 16px;
              font-weight: bold;
              display: inline-block;">
        💬 Обсудить выбранную вакансию с AI
    </a>
    <p style="color: #666; margin-top: 10px; font-size: 14px;">
        Получите персонализированный план развития для вашей целевой вакансии
    </p>
</div>
<div class="dialog-container">
    <div class="vacancies-container">
        <a href="${pageContext.request.contextPath}/send-message" class="back-btn">← Назад к
            диалогу</a>

        <div class="vacancies-header">
            <h2>Реальные вакансии</h2>
            <%
                String selectedVacancy = (String) request.getAttribute("selectedVacancy");
                List<RealVacancy> realVacancies = (List<RealVacancy>) request.getAttribute(
                        "realVacancies");
                if (selectedVacancy != null) {
            %>
            <p>Найдено вакансий по профессии: <strong><%= selectedVacancy %>
            </strong>
                <span class="vacancy-count"><%= realVacancies != null ? realVacancies.size() : 0 %> вакансий</span>
            </p>
            <% } %>
        </div>

        <%-- Сообщение об ошибке --%>
        <%
            String error = (String) request.getAttribute("error");
            if (error != null) {
        %>
        <div class="error-message">
            <%= error %>
        </div>
        <% } %>

        <div class="info-box">
            <h4>💼 Актуальные предложения рынка</h4>
            <p>Это реальные вакансии с популярных платформ. Изучите требования и зарплатные ожидания
                для вашей целевой профессии.</p>
        </div>

        <%-- Список реальных вакансий --%>
        <%
            if (realVacancies != null && !realVacancies.isEmpty()) {
                for (RealVacancy vacancy : realVacancies) {
        %>
        <div class="vacancy-card">
            <div class="vacancy-title"><%= vacancy.getNameOfVacancy() != null
                    ? vacancy.getNameOfVacancy() : "Название не указано" %>
            </div>

            <div class="vacancy-salary">
                <%= vacancy.getSalary() != null ? vacancy.getSalary() : "Зарплата не указана" %>
            </div>

            <div class="vacancy-requirements">
                <%
                    List<String> requirements = vacancy.getVacancyRequirements();
                    if (requirements != null && !requirements.isEmpty()) {
                        for (String requirement : requirements) {
                            if (requirement != null && !requirement.trim().isEmpty()) {
                %>
                <div class="requirement-item"><%= requirement %>
                </div>
                <%
                        }
                    }
                } else {
                %>
                <div class="requirement-item">Требования не указаны</div>
                <% } %>
            </div>
        </div>
        <%
            }
        } else if (selectedVacancy != null) {
        %>
        <div class="error-message">
            Не удалось найти вакансии по запросу: <%= selectedVacancy %>
        </div>
        <% } %>
    </div>
</div>
</body>
</html>