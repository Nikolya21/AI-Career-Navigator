<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.aicareer.core.model.user.User" %>
<html>
<head>
    <title>Личный кабинет - МТС</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/personal-cabinet.css">
</head>
<body>
<div class="container">
    <!-- Шапка в стиле МТС -->
    <header class="mts-header">
        <div class="header-content">
            <div class="logo">
                <span class="mts-logo">МТС</span>
                <span class="logo-text">Личный кабинет</span>
            </div>
            <nav class="header-nav">
                <a href="${pageContext.request.contextPath}/send-message" class="nav-link">Главная</a>
                <a href="#" class="nav-link">Услуги</a>
                <a href="#" class="nav-link">Помощь</a>
            </nav>
        </div>
    </header>

    <!-- Основной контент -->
    <main class="main-content">
        <div class="cabinet-card">
            <!-- Блок аватара -->
            <div class="avatar-section">
                <div class="avatar-container">
                    <div class="avatar">
                        <%
                            String userEmail = (String) session.getAttribute("userEmail");
                            String userName = (String) session.getAttribute("userName");
                            String initials = "П";
                            if (userName != null && !userName.isEmpty()) {
                                initials = userName.substring(0, 1).toUpperCase();
                            } else if (userEmail != null && !userEmail.isEmpty()) {
                                initials = userEmail.substring(0, 1).toUpperCase();
                            }
                        %>
                        <span class="avatar-initials"><%= initials %></span>
                    </div>
                    <button class="change-avatar-btn">Изменить фото</button>
                </div>
            </div>

            <!-- Информация о пользователе -->
            <div class="user-info-section">
                <h1 class="user-name">
                    <%
                        if (userName != null && !userName.isEmpty()) {
                            out.print(userName);
                        } else {
                            out.print("Пользователь");
                        }
                    %>
                </h1>

                <div class="info-grid">
                    <div class="info-item">
                        <label class="info-label">Электронная почта</label>
                        <div class="info-value">
                            <%= userEmail != null ? userEmail : "Не указано" %>
                        </div>
                    </div>

                    <div class="info-item">
                        <label class="info-label">ID пользователя</label>
                        <div class="info-value">
                            <%
                                Long userId = (Long) session.getAttribute("userId");
                                out.print(userId != null ? userId : "Не указано");
                            %>
                        </div>
                    </div>

                    <div class="info-item">
                        <label class="info-label">Статус</label>
                        <div class="info-value">Активный</div>
                    </div>

                    <div class="info-item">
                        <label class="info-label">Дата регистрации</label>
                        <div class="info-value">
                            <%
                                java.util.Date registrationDate = (java.util.Date) session.getAttribute("registrationDate");
                                if (registrationDate != null) {
                                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy");
                                    out.print(sdf.format(registrationDate));
                                } else {
                                    out.print("Сегодня");
                                }
                            %>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Кнопки действий -->
            <div class="actions-section">
                <button class="btn btn-primary" onclick="location.href='${pageContext.request.contextPath}/send-message'">
                    📋 На главную
                </button>

                <!-- ✅ ДОБАВЛЕНА КНОПКА ДЛЯ ПЕРЕХОДА К ROADMAP -->
                <%
                    // Проверяем, есть ли сгенерированный roadmap в сессии
                    Boolean discussionCompleted = (Boolean) session.getAttribute("vacancyDiscussionCompleted");
                    Object generatedRoadmap = session.getAttribute("generatedRoadmap");

                    if (discussionCompleted != null && discussionCompleted && generatedRoadmap != null) {
                %>
                <button class="btn btn-roadmap"
                        onclick="location.href='${pageContext.request.contextPath}/career-roadmap'">
                    🗺️ Мой карьерный план
                </button>
                <%
                } else {
                %>
                <button class="btn btn-roadmap disabled"
                        onclick="alert('Сначала завершите обсуждение вакансии, чтобы получить персональный план')"
                        title="Сначала завершите обсуждение вакансии">
                    🗺️ Мой карьерный план
                </button>
                <% } %>

                <button class="btn btn-secondary" onclick="history.back()">
                    ↩️ Вернуться назад
                </button>
                <button class="btn btn-logout" onclick="logout()">
                    🚪 Выйти из аккаунта
                </button>
            </div>
        </div>

        <!-- Дополнительные карточки -->
        <div class="additional-cards">
            <div class="service-card">
                <h3>Активность</h3>
                <ul class="services-list">
                    <li>Сообщений отправлено:
                        <%
                            java.util.List<String> messageHistory = (java.util.List<String>) session.getAttribute("messageHistory");
                            int messageCount = messageHistory != null ? messageHistory.size() / 2 : 0;
                            out.print(messageCount);
                        %>
                    </li>
                    <li>Последняя активность: Сегодня</li>
                    <li>Статус: Online</li>
                </ul>
            </div>

            <div class="promo-card">
                <h3>Специальные предложения</h3>
                <p>Получите персональную консультацию по карьерному развитию</p>
                <button class="btn-promo" onclick="location.href='${pageContext.request.contextPath}/send-message'">Начать диалог</button>
            </div>
        </div>
    </main>
</div>

<script>
  function logout() {
    if (confirm('Вы уверены, что хотите выйти из аккаунта?')) {
      window.location.href = '${pageContext.request.contextPath}/logout';
    }
  }
</script>

<style>
  /* Стили для кнопки roadmap */
  .btn-roadmap {
    background: linear-gradient(135deg, #28a745, #20c997);
    color: white;
    border: none;
    padding: 12px 24px;
    border-radius: 8px;
    cursor: pointer;
    font-size: 16px;
    font-weight: 500;
    transition: all 0.3s ease;
    box-shadow: 0 4px 12px rgba(40, 167, 69, 0.3);
  }

  .btn-roadmap:hover {
    background: linear-gradient(135deg, #218838, #1e9e8a);
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(40, 167, 69, 0.4);
  }

  .btn-roadmap.disabled {
    background: linear-gradient(135deg, #95a5a6, #7f8c8d);
    cursor: not-allowed;
    opacity: 0.7;
    box-shadow: none;
  }

  .btn-roadmap.disabled:hover {
    transform: none;
    background: linear-gradient(135deg, #95a5a6, #7f8c8d);
    box-shadow: none;
  }

  .actions-section {
    display: flex;
    gap: 15px;
    flex-wrap: wrap;
    justify-content: center;
    margin-top: 30px;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 10px;
    border: 1px solid #e9ecef;
  }

  .actions-section .btn {
    min-width: 180px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
  }
</style>
</body>
</html>