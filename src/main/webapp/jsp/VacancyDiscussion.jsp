<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Обсуждение вакансии - AI Career Navigator</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/DialogService.css">
    <style>
      .vacancy-header {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        padding: 20px;
        border-radius: 10px;
        margin-bottom: 20px;
        text-align: center;
      }

      .progress-indicator {
        background: white;
        padding: 15px 20px;
        border-radius: 10px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        margin-bottom: 15px;
      }

      .progress-text {
        font-size: 14px;
        color: #666;
        margin-bottom: 8px;
        text-align: center;
      }

      .progress-bar {
        background: #e9ecef;
        border-radius: 10px;
        height: 8px;
        overflow: hidden;
      }

      .progress-fill {
        background: linear-gradient(135deg, #667eea, #764ba2);
        height: 100%;
        border-radius: 10px;
        transition: width 0.3s ease;
      }

      .roadmap-button-section {
        text-align: center;
        margin: 30px 0;
        padding: 0;
      }

      .roadmap-btn {
        background: linear-gradient(135deg, #28a745, #20c997);
        color: white;
        padding: 15px 30px;
        border: none;
        border-radius: 10px;
        font-size: 16px;
        font-weight: 600;
        text-decoration: none;
        cursor: pointer;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 10px;
        box-shadow: 0 4px 12px rgba(40, 167, 69, 0.3);
      }

      .roadmap-btn:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(40, 167, 69, 0.4);
      }

      .completion-message {
        text-align: center;
        margin-bottom: 20px;
        padding: 20px;
        background: linear-gradient(135deg, #f8f9fa, #e9ecef);
        border-radius: 10px;
        border-left: 4px solid #28a745;
      }

      .completion-message h3 {
        color: #28a745;
        margin-bottom: 10px;
      }

      .completion-message p {
        color: #666;
        font-size: 16px;
        margin-bottom: 15px;
      }

      .redirect-message {
        text-align: center;
        padding: 30px;
        background: linear-gradient(135deg, #d4edda, #c3e6cb);
        border-radius: 10px;
        border-left: 4px solid #28a745;
        margin: 20px 0;
      }

      .redirect-message h3 {
        color: #155724;
        margin-bottom: 15px;
      }

      .loading-spinner {
        border: 4px solid #f3f3f3;
        border-top: 4px solid #3498db;
        border-radius: 50%;
        width: 40px;
        height: 40px;
        animation: spin 2s linear infinite;
        margin: 0 auto 15px;
      }

      @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
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

<div class="dialog-container">
    <%-- Заголовок с выбранной вакансией --%>
    <%
        String selectedVacancy = (String) request.getAttribute("selectedVacancy");
        if (selectedVacancy != null) {
    %>
    <div class="vacancy-header">
        <h2>🎯 Обсуждаем вакансию: <%= selectedVacancy %></h2>
        <p>Давайте подробнее обсудим ваши цели и составим персонализированный план</p>
    </div>
    <% } %>

    <%-- Индикатор прогресса --%>
    <%
        Integer questionsCount = (Integer) request.getAttribute("questionsCount");
        if (questionsCount != null && questionsCount > 0 && questionsCount <= 5) {
    %>
    <div class="progress-indicator">
        <div class="progress-text">
            Вопрос <%= questionsCount %> из 5
        </div>
        <div class="progress-bar">
            <div class="progress-fill" style="width: <%= (questionsCount - 1) * 20 %>%;"></div>
        </div>
    </div>
    <% } %>

    <div class="dialog-history" id="chatHistory">
        <%
            List<String> discussionHistory = (List<String>) request.getAttribute("discussionHistory");
            if (discussionHistory != null && !discussionHistory.isEmpty()) {
                for (int i = 0; i < discussionHistory.size(); i++) {
                    if (i % 2 == 0) {
                        // Сообщение AI
        %>
        <div class="message ai-message">
            <div class="message-sender">🤖 AI Ассистент</div>
            <div class="message-content">
                <%= discussionHistory.get(i) %>
            </div>
            <div class="message-meta">Вопрос <%= (i/2) + 1 %></div>
        </div>
        <%
        } else {
            // Сообщение пользователя
        %>
        <div class="message user-message">
            <div class="message-sender">👤 Вы</div>
            <div class="message-content">
                <%= discussionHistory.get(i) %>
            </div>
            <div class="message-meta">Ответ <%= (i/2) + 1 %></div>
        </div>
        <%
                    }
                }
            }
        %>
    </div>

    <%-- Кнопка перехода к Roadmap (показывается если диалог завершен) --%>
    <%
        Boolean showRoadmapButton = (Boolean) request.getAttribute("showRoadmapButton");
        if (showRoadmapButton != null && showRoadmapButton) {
    %>
    <div class="completion-message">
        <h3>🎉 Диалог успешно завершен!</h3>
        <p>На основе нашего обсуждения вакансии <strong><%= selectedVacancy %></strong> мы подготовили для вас персонализированный карьерный план</p>
        <p>Теперь вы можете перейти к просмотру пошагового плана развития</p>
    </div>

    <div class="roadmap-button-section">
        <a href="${pageContext.request.contextPath}/career-roadmap" class="roadmap-btn">
            📊 Перейти к моему Roadmap
        </a>
    </div>
    <%
    } else if (questionsCount != null && questionsCount <= 5) {
    %>
    <%-- Форма ввода сообщения (показывается во время диалога) --%>
    <form action="${pageContext.request.contextPath}/vacancy-discussion" method="post"
          class="message-form" id="messageForm">
        <input type="text" name="message" placeholder="Введите ваш ответ..."
               class="message-input" id="messageInput" required autocomplete="off">
        <button type="submit" class="btn-send">📤 Отправить</button>
    </form>
    <%
    } else {
    %>
    <%-- Сообщение о завершении и автоматическое перенаправление --%>
    <div class="redirect-message">
        <div class="loading-spinner"></div>
        <h3>✅ Диалог завершен!</h3>
        <p>Спасибо за ваши ответы! Сейчас вы будете перенаправлены на страницу с вашим персонализированным планом развития.</p>
        <p><small>Если перенаправление не произошло автоматически, <a href="${pageContext.request.contextPath}/career-roadmap">нажмите сюда</a>.</small></p>
    </div>
    <script>
      // Автоматический переход через 2 секунды
      setTimeout(function() {
        console.log("🔄 Автоматическое перенаправление на страницу roadmap");
        window.location.href = "${pageContext.request.contextPath}/career-roadmap";
      }, 2000);
    </script>
    <% } %>
</div>

<script>
  // Автопрокрутка к последнему сообщению
  function scrollToBottom() {
    const chatHistory = document.getElementById('chatHistory');
    if (chatHistory) {
      chatHistory.scrollTop = chatHistory.scrollHeight;
    }
  }

  document.addEventListener('DOMContentLoaded', function() {
    scrollToBottom();

    const messageInput = document.getElementById('messageInput');
    if (messageInput) {
      messageInput.focus();

      // Очистка поля после отправки
      const messageForm = document.getElementById('messageForm');
      if (messageForm) {
        messageForm.addEventListener('submit', function() {
          setTimeout(function() {
            messageInput.value = '';
          }, 100);
        });
      }
    }
  });
</script>
</body>
</html>