<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AI Career Navigator</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/DialogService.css">
    <style>
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
        background: linear-gradient(135deg, #007BFF, #0056b3);
        height: 100%;
        border-radius: 10px;
        transition: width 0.3s ease;
      }
    </style>
</head>
<body>
<div class="header">
    <h1>Career Navigator</h1>
    <div class="user-info">
        <%
            String userEmail = (String) session.getAttribute("userEmail");
            if (userEmail != null) {
        %>
        <span>Вы вошли как: <%= userEmail %></span>
        <a href="/ai_career_navigator_war/login" class="logout-btn">Выйти</a>
        <% } %>
    </div>
</div>

<div class="dialog-container">
    <%-- Индикатор прогресса --%>
    <%
        Integer questionsCount = (Integer) request.getAttribute("questionsCount");
        Boolean dialogCompleted = (Boolean) request.getAttribute("dialogCompleted");

        if (questionsCount != null && questionsCount > 0 && (dialogCompleted == null
                || !dialogCompleted)) {
    %>
    <div class="progress-indicator">
        <div class="progress-text">
            Вопрос <%= questionsCount %> из 5
        </div>
        <div class="progress-bar">
            <div class="progress-fill" style="width: <%= questionsCount * 20 %>%;"></div>
        </div>
    </div>
    <% } %>

    <div class="dialog-history" id="chatHistory">
        <!-- Сообщение AI по умолчанию -->
        <div class="message ai-message">
            <div class="message-sender">AI</div>
            <div class="message-content">
                Here we can discuss your learning plan and create a personal path to your dream. To
                do this, I need to get to know you better... Tell me about your experience in
                programming
            </div>
        </div>

        <%
            // Получаем историю сообщений из request (а не из session)
            List<String> messageHistory = (List<String>) request.getAttribute("messageHistory");
            if (messageHistory == null) {
                // Если в request нет, пробуем получить из session
                messageHistory = (List<String>) session.getAttribute("messageHistory");
            }

            if (messageHistory == null) {
                messageHistory = new ArrayList<>();
            }

            // Отображаем историю сообщений
            for (int i = 0; i < messageHistory.size(); i += 2) {
                if (i < messageHistory.size()) {
                    String userMessage = messageHistory.get(i);
        %>
        <div class="message user-message">
            <div class="message-sender">User</div>
            <div class="message-content">
                <%= userMessage %>
            </div>
        </div>
        <%
            }

            if (i + 1 < messageHistory.size()) {
                String aiResponse = messageHistory.get(i + 1);
        %>
        <div class="message ai-message">
            <div class="message-sender">AI</div>
            <div class="message-content">
                <%= aiResponse %>
            </div>
        </div>
        <%
                }
            }
        %>
    </div>

    <%
        // Проверяем, не завершен ли диалог
        if (dialogCompleted == null || !dialogCompleted) {
    %>
    <form action="${pageContext.request.contextPath}/send-message" method="post"
          class="message-form" id="messageForm">
        <input type="text" name="message" placeholder="Type your message here..."
               class="message-input" id="messageInput" required>
        <button type="submit" class="btn-send">Send</button>
    </form>
    <%
    } else {
    %>
    <div class="dialog-completed-message">
        <div class="completion-info">
            <h3>Диалог завершен</h3>
            <p>Вы достигли лимита в 5 вопросов. <a
                    href="${pageContext.request.contextPath}/dialog-completed">Посмотреть итоги</a>
            </p>
        </div>
    </div>
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

  // Прокрутка при загрузке страницы
  document.addEventListener('DOMContentLoaded', function () {
    scrollToBottom();

    // Очистка поля ввода после отправки
    const messageForm = document.getElementById('messageForm');
    const messageInput = document.getElementById('messageInput');

    if (messageForm && messageInput) {
      messageForm.addEventListener('submit', function (e) {
        if (messageInput.value.trim() !== '') {
          // Можно добавить индикатор загрузки здесь
          console.log('Sending message:', messageInput.value);
        }
      });
    }
  });

  // Фокус на поле ввода
  window.onload = function () {
    const messageInput = document.getElementById('messageInput');
    if (messageInput) {
      messageInput.focus();
    }
  };
</script>
</body>
</html>