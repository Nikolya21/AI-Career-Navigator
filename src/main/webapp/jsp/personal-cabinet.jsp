<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.aicareer.core.model.user.User" %>
<html>
<head>
    <title>Личный кабинет - МТС</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/personal-cabinet.css">
</head>
<body>
    <div class="container">
        <header class="mts-header">
            <div class="header-content">
                <!-- Форма загрузки PNG в левом верхнем углу -->
                <form action="${pageContext.request.contextPath}/upload-avatar"
                      method="post"
                      enctype="multipart/form-data"
                      class="upload-form">
                    <input type="file" name="avatarFile" accept="image/png" required>
                    <button type="submit" class="upload-png-btn">📁 Загрузить PNG</button>
                    <%
                        String uploadError = (String) request.getAttribute("uploadError");
                        if (uploadError != null && !uploadError.trim().isEmpty()) {
                    %>
                        <div class="upload-error">❌ <%= uploadError %></div>
                    <%
                        }
                    %>
                </form>

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
                        <!-- Кнопка "Изменить фото" теперь не нужна — заменена формой -->
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
                <div class="actions-section">
                    <a href="${pageContext.request.contextPath}/send-message" class="btn btn-primary">
                        📋 На главную
                    </a>
                    <button class="btn btn-secondary" onclick="history.back()">
                        ↩️ Вернуться назад
                    </button>
                    <button class="btn btn-logout" onclick="if(confirm('Вы уверены?'))location.href='${pageContext.request.contextPath}/logout'">
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
                    <a href="${pageContext.request.contextPath}/send-message" class="btn-promo">Начать диалог</a>
                </div>
            </div>
        </main>
    </div>
</body>
</html>