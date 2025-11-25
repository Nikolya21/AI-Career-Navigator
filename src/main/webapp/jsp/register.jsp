<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Регистрация - AI Career Navigator</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/Register.css">
</head>
<body>
<div class="register-container">
    <h2>Регистрация</h2>

    <%-- Сообщение об ошибках --%>
    <%
        List<String> errors = (List<String>) request.getAttribute("errors");
        String prefilledEmail = (String) request.getAttribute("email");
        String prefilledName = (String) request.getAttribute("name");

        if (errors != null && !errors.isEmpty()) {
    %>
    <div class="error-message">
        <strong>Ошибки:</strong>
        <ul>
            <% for (String error : errors) { %>
            <li><%= error %></li>
            <% } %>
        </ul>
    </div>
    <% } %>

    <form action="${pageContext.request.contextPath}/register" method="post" class="register-form">
        <div class="input-group">
            <input type="text"
                   name="name"
                   value="<%= prefilledName != null ? prefilledName : "" %>"
                   placeholder="Имя"
                   required
                   maxlength="100">
        </div>

        <div class="input-group">
            <input type="email"
                   name="email"
                   value="<%= prefilledEmail != null ? prefilledEmail : "" %>"
                   placeholder="Электронная почта"
                   required
                   maxlength="255">
        </div>

        <div class="input-group">
            <input type="password"
                   name="password"
                   placeholder="Пароль"
                   required
                   minlength="6"
                   maxlength="100">
            <small class="password-hint">Минимум 6 символов</small>
        </div>

        <div class="input-group">
            <input type="password"
                   name="confirmPassword"
                   placeholder="Подтвердите пароль"
                   required
                   minlength="6"
                   maxlength="100">
        </div>

        <button type="submit" class="btn-register">Зарегистрироваться</button>

        <div class="login-link">
            Уже есть аккаунт?
            <a href="${pageContext.request.contextPath}/login">Войти</a>
        </div>
    </form>
</div>
</body>
</html>