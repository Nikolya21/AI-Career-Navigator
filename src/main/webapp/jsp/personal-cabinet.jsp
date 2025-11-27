<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Личный кабинет - МТС</title>
    <link rel="stylesheet" href="css/personal-cabinet.css">
</head>
<body>
    <div class="container">
        <header class="mts-header">
            <div class="header-content">
                <div class="logo">
                    <span class="mts-logo">МТС</span>
                    <span class="logo-text">Личный кабинет</span>
                </div>
                <nav class="header-nav">
                    <a href="main.jsp" class="nav-link">Главная</a>
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
                            <span class="avatar-initials">${empty user.firstName ? 'И' : user.firstName.charAt(0)}${empty user.lastName ? 'Ф' : user.lastName.charAt(0)}</span>
                        </div>
                        <button class="change-avatar-btn">Изменить фото</button>
                    </div>
                </div>

                <div class="user-info-section">
                    <h1 class="user-name">
                        ${empty user.firstName ? 'Иван' : user.firstName} ${empty user.lastName ? 'Иванов' : user.lastName}
                    </h1>

                    <div class="info-grid">
                        <div class="info-item">
                            <label class="info-label">Электронная почта</label>
                            <div class="info-value">${empty user.email ? 'example@mts.ru' : user.email}</div>
                        </div>

                        <div class="info-item">
                            <label class="info-label">Номер телефона</label>
                            <div class="info-value">+7 (999) 123-45-67</div>
                        </div>

                        <div class="info-item">
                            <label class="info-label">Прохождение</label>
                            <div class="info-value">«Профессия»</div>
                        </div>

                        <div class="info-item">
                            <label class="info-label">Баланс</label>
                            <div class="info-value balance">150.50 ₽</div>
                        </div>
                    </div>
                </div>

                <div class="actions-section">
                    <button class="btn btn-primary" onclick="location.href='main.jsp'">
                        📋 На главную
                    </button>
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
                    <h3>Мои услуги</h3>
                    <ul class="services-list">
                        <li>Интернет: 15 ГБ</li>
                        <li>Звонки: безлимит</li>
                        <li>Сообщения: 100 SMS</li>
                    </ul>
                </div>

                <div class="promo-card">
                    <h3>Специальные предложения</h3>
                    <p>Получите скидку 20% на дополнительные пакеты интернета</p>
                    <button class="btn-promo">Подробнее</button>
                </div>
            </div>
        </main>
    </div>

    <script>
        function logout() {
            if (confirm('Вы уверены, что хотите выйти из аккаунта?')) {
                window.location.href = 'logout.jsp';
            }
        }
    </script>
</body>
</html>