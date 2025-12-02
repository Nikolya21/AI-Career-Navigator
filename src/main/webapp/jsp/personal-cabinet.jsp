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

            <!-- ✅ Кнопка для загрузки резюме -->
            <div class="resume-section">
                <div class="resume-upload">
                    <h3>📄 Резюме</h3>
                    <div class="resume-info">
                        <%
                            // Проверяем, есть ли уже загруженное резюме
                            String resumeFilename = (String) session.getAttribute("resumeFilename");
                            if (resumeFilename != null) {
                        %>
                        <div class="resume-status uploaded">
                            <span class="resume-icon">✅</span>
                            <div class="resume-details">
                                <strong>Резюме загружено:</strong>
                                <span class="filename"><%= resumeFilename %></span>
                                <span class="upload-date">Загружено:
                                    <%
                                        java.util.Date resumeUploadDate = (java.util.Date) session.getAttribute("resumeUploadDate");
                                        if (resumeUploadDate != null) {
                                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm");
                                            out.print(sdf.format(resumeUploadDate));
                                        } else {
                                            out.print("Сегодня");
                                        }
                                    %>
                                </span>
                            </div>
                            <button class="btn-resume-change" onclick="uploadResume()">Заменить</button>
                            <button class="btn-resume-view" onclick="viewResume()">Просмотреть</button>
                        </div>
                        <% } else { %>
                        <div class="resume-status not-uploaded">
                            <span class="resume-icon">📄</span>
                            <div class="resume-details">
                                <strong>Резюме не загружено</strong>
                                <span class="resume-hint">Загрузите ваше резюме для лучшего подбора вакансий</span>
                            </div>
                        </div>
                        <% } %>
                    </div>

                    <div class="resume-actions">
                        <!-- Скрытый input для загрузки файла -->
                        <input type="file" id="resumeInput" accept=".pdf,.doc,.docx,.txt" style="display: none;">

                        <!-- Основная кнопка загрузки -->
                        <button class="btn-resume-upload" onclick="uploadResume()">
                            📎 Прикрепить резюме
                        </button>

                        <!-- ✅ ИСПРАВЛЕНА КНОПКА ДЛЯ ПЕРЕХОДА К ROADMAP -->
                        <%
                            Boolean discussionCompleted = (Boolean) session.getAttribute("vacancyDiscussionCompleted");
                            Object generatedRoadmap = session.getAttribute("generatedRoadmap");

                            // Формируем правильный URL для перехода
                            String contextPath = request.getContextPath(); // /ai_career_navigator_war
                            String roadmapUrl = contextPath + "/career-roadmap";

                            // Проверяем доступность кнопки
                            boolean isRoadmapAvailable = (discussionCompleted != null && discussionCompleted && generatedRoadmap != null);
                        %>
                        <button class="btn btn-roadmap <%= isRoadmapAvailable ? "" : "disabled" %>"
                                onclick="<%= isRoadmapAvailable ? "window.location.href='" + roadmapUrl + "'" : "alert('Сначала завершите обсуждение вакансии, чтобы получить персональный план')" %>"
                                title="<%= isRoadmapAvailable ? "Перейти к карьерному плану" : "Сначала завершите обсуждение вакансии" %>">
                            🗺️ Мой карьерный план
                        </button>
                    </div>

                    <div class="resume-formats">
                        <small>Поддерживаемые форматы: PDF, DOC, DOCX, TXT (максимум 10 МБ)</small>
                    </div>
                </div>
            </div>

            <!-- Кнопки действий -->
            <div class="actions-section">
                <button class="btn btn-primary" onclick="location.href='<%= request.getContextPath() %>/send-message'">
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
                <button class="btn-promo" onclick="location.href='<%= request.getContextPath() %>/send-message'">Начать диалог</button>
            </div>
        </div>
    </main>
</div>

<script>
  function logout() {
    if (confirm('Вы уверены, что хотите выйти из аккаунта?')) {
      window.location.href = '<%= request.getContextPath() %>/logout';
    }
  }

  // Функция для загрузки резюме
  function uploadResume() {
    // Создаем скрытый input элемент
    const fileInput = document.getElementById('resumeInput');

    // Добавляем обработчик события изменения файла
    fileInput.onchange = function(e) {
      const file = e.target.files[0];
      if (!file) return;

      // Проверяем размер файла (максимум 10 МБ)
      if (file.size > 10 * 1024 * 1024) {
        alert('Файл слишком большой! Максимальный размер: 10 МБ');
        return;
      }

      // Проверяем расширение файла
      const allowedExtensions = ['.pdf', '.doc', '.docx', '.txt'];
      const fileName = file.name.toLowerCase();
      const isValidExtension = allowedExtensions.some(ext => fileName.endsWith(ext));

      if (!isValidExtension) {
        alert('Неверный формат файла! Разрешенные форматы: PDF, DOC, DOCX, TXT');
        return;
      }

      // Показываем информацию о загрузке
      showUploadProgress();

      // Имитация загрузки файла на сервер
      setTimeout(function() {
        // Здесь должен быть реальный AJAX запрос на сервер
        // uploadResumeToServer(file);

        // Временная имитация успешной загрузки
        simulateResumeUpload(file.name);
      }, 1500);
    };

    // Кликаем по скрытому input, чтобы открыть проводник
    fileInput.click();
  }

  // Имитация загрузки резюме
  function simulateResumeUpload(filename) {
    // Показываем сообщение об успешной загрузке
    alert('✅ Резюме "' + filename + '" успешно загружено!');

    // Обновляем страницу (в реальном приложении здесь будет AJAX запрос)
    window.location.reload();
  }

  // Показывает прогресс загрузки
  function showUploadProgress() {
    // Создаем модальное окно с прогрессом
    const modal = document.createElement('div');
    modal.innerHTML = `
      <div style="position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000;">
        <div style="background: white; padding: 30px; border-radius: 10px; text-align: center; min-width: 300px;">
          <div class="spinner" style="border: 4px solid #f3f3f3; border-top: 4px solid #3498db; border-radius: 50%; width: 40px; height: 40px; animation: spin 2s linear infinite; margin: 0 auto 15px;"></div>
          <h3 style="margin-bottom: 10px;">Загрузка резюме...</h3>
          <p style="color: #666;">Пожалуйста, подождите</p>
          <style>@keyframes spin {0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); }}</style>
        </div>
      </div>
    `;
    document.body.appendChild(modal);

    // Удаляем модальное окно через 1.5 секунды (имитация загрузки)
    setTimeout(() => {
      if (modal.parentNode) {
        modal.parentNode.removeChild(modal);
      }
    }, 1500);
  }

  // Функция для просмотра резюме
  function viewResume() {
    const resumeFilename = '<%= resumeFilename != null ? resumeFilename : "" %>';
    if (resumeFilename) {
      // Здесь должен быть реальный URL для скачивания/просмотра резюме
      alert('Просмотр резюме: ' + resumeFilename + '\n\nВ реальном приложении здесь будет открытие файла');
      // window.open('<%= request.getContextPath() %>/download-resume?filename=' + encodeURIComponent(resumeFilename), '_blank');
    }
  }

  // Дополнительная функция для отладки - показывает текущий URL
  function debugPaths() {
    console.log('Context Path: <%= request.getContextPath() %>');
    console.log('Full URL: <%= request.getRequestURL() %>');
    console.log('Roadmap URL: <%= request.getContextPath() + "/career-roadmap" %>');
  }
</script>

<style>
  /* Стили для раздела с резюме */
  .resume-section {
    margin: 25px 0;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 10px;
    border: 1px solid #e9ecef;
  }

  .resume-upload h3 {
    margin-bottom: 15px;
    color: #2c3e50;
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .resume-info {
    margin-bottom: 20px;
  }

  .resume-status {
    display: flex;
    align-items: center;
    padding: 15px;
    border-radius: 8px;
    gap: 15px;
  }

  .resume-status.uploaded {
    background: #d4edda;
    border: 1px solid #c3e6cb;
  }

  .resume-status.not-uploaded {
    background: #f8d7da;
    border: 1px solid #f5c6cb;
  }

  .resume-icon {
    font-size: 24px;
  }

  .resume-details {
    flex: 1;
  }

  .resume-details .filename {
    display: block;
    color: #155724;
    font-weight: 500;
    margin: 5px 0;
  }

  .resume-details .upload-date {
    display: block;
    font-size: 12px;
    color: #6c757d;
  }

  .resume-details .resume-hint {
    display: block;
    color: #721c24;
    font-size: 14px;
    margin-top: 5px;
  }

  .resume-actions {
    display: flex;
    gap: 15px;
    flex-wrap: wrap;
    margin-bottom: 15px;
  }

  .btn-resume-upload,
  .btn-resume-change,
  .btn-resume-view {
    padding: 12px 24px;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    font-size: 16px;
    font-weight: 500;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
  }

  .btn-resume-upload {
    background: linear-gradient(135deg, #3498db, #2980b9);
    color: white;
    box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
  }

  .btn-resume-upload:hover {
    background: linear-gradient(135deg, #2980b9, #2471a3);
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(52, 152, 219, 0.4);
  }

  .btn-resume-change {
    background: linear-gradient(135deg, #f39c12, #e67e22);
    color: white;
  }

  .btn-resume-change:hover {
    background: linear-gradient(135deg, #e67e22, #d35400);
  }

  .btn-resume-view {
    background: linear-gradient(135deg, #2ecc71, #27ae60);
    color: white;
  }

  .btn-resume-view:hover {
    background: linear-gradient(135deg, #27ae60, #219653);
  }

  .resume-formats {
    color: #6c757d;
    font-size: 12px;
    text-align: center;
    margin-top: 10px;
  }

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
    min-width: 180px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
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