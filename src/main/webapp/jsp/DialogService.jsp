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
    <div class="dialog-history" id="chatHistory">
        <!-- Сообщение AI по умолчанию -->
        <div class="message ai-message">
            <div class="message-sender">AI</div>
            <div class="message-content">
                Here we can discuss your learning plan and create a personal path to your dream. To do this, I need to get to know you better... Tell me about your experience in programming
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

            System.out.println("JSP: Displaying " + messageHistory.size() + " messages");

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

    <form action="${pageContext.request.contextPath}/send-message" method="post" class="message-form" id="messageForm">
        <input type="text" name="message" placeholder="Type your message here..." class="message-input" id="messageInput" required>
        <button type="submit" class="btn-send">Send</button>
    </form>
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
  document.addEventListener('DOMContentLoaded', function() {
    scrollToBottom();

    // Очистка поля ввода после отправки
    const messageForm = document.getElementById('messageForm');
    const messageInput = document.getElementById('messageInput');

    if (messageForm && messageInput) {
      messageForm.addEventListener('submit', function(e) {
        if (messageInput.value.trim() !== '') {
          // Можно добавить индикатор загрузки здесь
          console.log('Sending message:', messageInput.value);
        }
      });
    }
  });

  // Фокус на поле ввода
  window.onload = function() {
    const messageInput = document.getElementById('messageInput');
    if (messageInput) {
      messageInput.focus();
    }
  };
</script>
</body>
</html>