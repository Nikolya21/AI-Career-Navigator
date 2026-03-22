<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.aicareer.core.model.vacancy.RealVacancy" %>
<%@ page import="java.nio.charset.StandardCharsets" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Реальные вакансии - Career Navigator</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/DialogService.css">-
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

      .vacancy-details-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 15px;
        margin-bottom: 20px;
      }

      .detail-item {
        background: #f8f9fa;
        padding: 12px;
        border-radius: 8px;
        border-left: 3px solid #3498db;
      }

      .detail-label {
        display: block;
        font-size: 12px;
        color: #718096;
        text-transform: uppercase;
        letter-spacing: 0.5px;
        margin-bottom: 5px;
        font-weight: 600;
      }

      .detail-value {
        font-size: 14px;
        color: #2d3748;
        font-weight: 500;
      }

      .detail-value.company {
        color: #2b6cb0;
        font-weight: 600;
      }

      .detail-value.experience {
        color: #d69e2e;
      }

      .detail-value.age {
        color: #38a169;
      }

      .vacancy-requirements {
        margin-bottom: 15px;
      }

      .requirements-title {
        font-size: 16px;
        font-weight: 600;
        color: #4a5568;
        margin-bottom: 10px;
        display: flex;
        align-items: center;
        gap: 8px;
      }

      .requirement-item {
        background: linear-gradient(135deg, #edf2f7, #e2e8f0);
        padding: 10px 15px;
        margin: 8px 0;
        border-radius: 6px;
        font-size: 14px;
        color: #4a5568;
        border-left: 3px solid #4299e1;
        transition: all 0.2s ease;
      }

      .requirement-item:hover {
        background: linear-gradient(135deg, #e2e8f0, #cbd5e0);
        transform: translateX(3px);
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

      .vacancy-apply-btn {
        background: linear-gradient(135deg, #28a745, #20c997);
        color: white;
        border: none;
        padding: 10px 20px;
        border-radius: 6px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 8px;
        margin-top: 15px;
      }

      .vacancy-apply-btn:hover {
        background: linear-gradient(135deg, #20c997, #1e9e8a);
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(40, 167, 69, 0.3);
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

      .no-skills {
        color: #a0aec0;
        font-style: italic;
        padding: 10px;
        background: #f7fafc;
        border-radius: 6px;
        text-align: center;
        font-size: 14px;
      }

      .badge {
        display: inline-block;
        padding: 4px 8px;
        border-radius: 4px;
        font-size: 12px;
        font-weight: 500;
        margin-right: 5px;
      }

      .badge-primary {
        background: #4299e1;
        color: white;
      }

      .badge-success {
        background: #38a169;
        color: white;
      }

      .badge-warning {
        background: #d69e2e;
        color: white;
      }

      .badge-info {
        background: #319795;
        color: white;
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
       style="background: linear-gradient(135deg, #d5d0dd, #ebffcd);
              color: #232020;
              padding: 15px 30px;
              border-radius: 10px;
              text-decoration: none;
              font-size: 16px;
              font-weight: bold;
              display: inline-block;">
        💬 Обсудить выбранную вакансию с AI
    </a>
    <p style="color:  #FFFFFF; margin-top: 10px; font-size: 14px;">
        Получите персонализированный план развития для вашей целевой вакансии
    </p>
</div>
<div class="dialog-container">
    <div class="vacancies-container">
        <a href="${pageContext.request.contextPath}/send-message" class="back-btn">← Назад к диалогу</a>

        <div class="vacancies-header">
            <h2>Реальные вакансии</h2>
            <%
                String selectedVacancy = (String) request.getAttribute("selectedVacancy");
                List<RealVacancy> realVacancies = (List<RealVacancy>) request.getAttribute("realVacancies");
                if (selectedVacancy != null) {
            %>
            <p>Найдено вакансий по профессии: <strong><%= selectedVacancy %></strong>
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
            <p>Это реальные вакансии с популярных платформ. Изучите требования и зарплатные ожидания для вашей целевой профессии.</p>
            <p><small>📊 Источник: HeadHunter API | 🔄 Данные обновляются в реальном времени</small></p>
        </div>

        <%-- Список реальных вакансий --%>
        <%
            if (realVacancies != null && !realVacancies.isEmpty()) {
                for (RealVacancy vacancy : realVacancies) {
        %>
        <div class="vacancy-card">
            <div class="vacancy-title"><%= vacancy.getNameOfVacancy() != null
                    ? vacancy.getNameOfVacancy() : "Название не указано" %></div>

            <div class="vacancy-salary">
                <% if (vacancy.getSalary() != null && !vacancy.getSalary().isEmpty()) { %>
                💰 <%= vacancy.getSalary() %>
                <% } else { %>
                💰 Зарплата не указана
                <% } %>
            </div>

            <div class="vacancy-details-grid">
                <%-- Компания --%>
                <div class="detail-item">
                    <span class="detail-label">🏢 Компания</span>
                    <span class="detail-value company">
                        <% if (vacancy.getEmployer() != null && !vacancy.getEmployer().isEmpty()) { %>
                            <%= vacancy.getEmployer() %>
                        <% } else { %>
                            Не указана
                        <% } %>
                    </span>
                </div>

                <%-- Требуемый опыт --%>
                <div class="detail-item">
                    <span class="detail-label">📈 Требуемый опыт</span>
                    <span class="detail-value experience">
                        <% if (vacancy.getExperience() != null && !vacancy.getExperience().isEmpty()) { %>
                            <%= vacancy.getExperience() %>
                        <% } else { %>
                            Не указан
                        <% } %>
                    </span>
                </div>

                <%-- Возрастные ограничения --%>
                <div class="detail-item">
                    <span class="detail-label">🎂 Возраст</span>
                    <span class="detail-value age">
                        <% if (vacancy.getAge() != null && !vacancy.getAge().isEmpty()) { %>
                            <%= vacancy.getAge() %>
                        <% } else { %>
                            Не указано
                        <% } %>
                    </span>
                </div>
            </div>

            <div class="vacancy-requirements">
                <div class="requirements-title">🔧 Ключевые навыки и требования</div>
                <%
                    List<String> requirements = vacancy.getVacancyRequirements();
                    if (requirements != null && !requirements.isEmpty()) {
                        int skillCount = 0;
                        for (String requirement : requirements) {
                            if (requirement != null && !requirement.trim().isEmpty()) {
                                skillCount++;
                %>
                <div class="requirement-item">
                    <span class="badge badge-primary"><%= skillCount %></span> <%= requirement %>
                </div>
                <%
                        }
                    }
                } else {
                %>
                <div class="no-skills">Навыки не указаны в данной вакансии</div>
                <% } %>
            </div>

            <div class="vacancy-meta">
                <div class="company-info">
                    <span class="badge badge-info">HH.ru</span>
                    <% if (vacancy.getExperience() != null) { %>
                    <span class="badge badge-warning"><%= vacancy.getExperience() %></span>
                    <% } %>
                    <% if (vacancy.getAge() != null) { %>
                    <span class="badge badge-success"><%= vacancy.getAge() %></span>
                    <% } %>
                </div>
                <button class="vacancy-apply-btn" onclick="applyToVacancy('<%= vacancy.getNameOfVacancy() %>')">
                    📝 Откликнуться
                </button>
            </div>
        </div>
        <%
            }
        } else if (selectedVacancy != null) {
        %>
        <div class="error-message">
            Не удалось найти вакансии по запросу: <%= selectedVacancy %>
            <p style="margin-top: 10px; font-size: 14px;">
                Попробуйте изменить название вакансии или уточнить поисковый запрос.
            </p>
        </div>
        <% } %>
    </div>
</div>

<script>
  function applyToVacancy(vacancyName) {
    if (confirm('Вы хотите откликнуться на вакансию "' + vacancyName + '"?\n\nВ реальной системе здесь будет переход на страницу вакансии на HH.ru')) {
      // В реальном приложении здесь будет редирект на страницу вакансии
      alert('В реальной системе вы будете перенаправлены на страницу вакансии "' + vacancyName + '" на HeadHunter.ru');
      // window.open('https://hh.ru/vacancy/' + vacancyId, '_blank');
    }
  }

  // Добавляем фильтрацию по опыту
  document.addEventListener('DOMContentLoaded', function() {
    const vacancyCards = document.querySelectorAll('.vacancy-card');

    // Можно добавить фильтры в будущем
    console.log('Загружено вакансий: ' + vacancyCards.length);
  });
</script>
</body>
</html>