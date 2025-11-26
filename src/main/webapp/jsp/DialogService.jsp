<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Career Navigator - Dialog</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/DialogService.css">
</head>
<body>
<div class="header">
    <h1>Career Navigator</h1>
</div>
<div class="dialog-container">
    <div class="dialog-history">
        <%
            List<String> dialogHistory = (List<String>) session.getAttribute("dialogHistory");
            if (dialogHistory != null) {
                for (String message : dialogHistory) {
                    String messageClass = message.startsWith("AI:") ? "ai-message" : "user-message";
        %>
        <div class="message <%= messageClass %>">
            <%= message %>
        </div>
        <%
                }
            }
        %>
    </div>
    <form action="${pageContext.request.contextPath}/send-message" method="post" class="message-form">
        <input type="text" name="message" placeholder="Type your message here..." class="message-input" required>
        <button type="submit" class="btn-send">Send</button>
    </form>
</div>
</body>
</html>