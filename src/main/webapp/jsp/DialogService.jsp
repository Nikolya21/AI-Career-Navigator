<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>DialogService</title>
    <link rel="stylesheet" href="../css/DialogService.css">
</head>
<body>
    <div class="header">
        <h1>Java Spring boot</h1>
    </div>
    <div class="dialog-container">
        <div class="message-box">
            <p>Here we can discuss your learning plan and create a personal path to your dream. To do this, I need to get to know you better... Tell me about your experience in programming</p>
        </div>
        <form action="/send-message" method="post">
            <input type="text" name="message" placeholder="Message" class="message-input">
            <button type="submit" class="btn-send">Отправить</button>
        </form>
    </div>
</body>
</html>