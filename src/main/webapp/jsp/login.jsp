<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login</title>
    <link rel="stylesheet" href="../css/login.css">
</head>
<body>
    <div class="login-container">
        <h2>Вход</h2>
        <%
            List<String> errors = (List<String>) request.getAttribute("errors");
            String prefilledEmail = (String) request.getAttribute("email");
            if (errors != null && !errors.isEmpty()) {
        %>
            <div style="color: red; background: #ffe6e6; padding: 10px; border-radius: 4px; margin-bottom: 15px;">
                <strong>Ошибки:</strong>
                <ul style="margin: 5px 0 0 20px;">
                    <% for (String error : errors) { %>
                        <li><%= error %></li>
                    <% } %>
                </ul>
            </div>
        <% } %>

        <form action="/login" method="post">
            <div class="input-group">
                <input type="text" name="email" value="<%= prefilledEmail != null ? prefilledEmail : "" %>" 
                       placeholder = "Электронная почта" 
                       required>
            </div>
            <div class="input-group">
                <input type="password" 
                       name="password" 
                       placeholder="Пароль" 
                       required>
            </div>
            <button type="submit" class="btn-login">Войти</button>
            <div class="forgot-password">
                Нет аккаунта?
            </div>
        </form>
    </div>
</body>
</html>