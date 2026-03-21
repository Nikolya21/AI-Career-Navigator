package com.aicareer.app.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;

@WebServlet("/personal-cabinet")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1 MB
    maxFileSize = 1024 * 1024 * 10,  // 10 MB
    maxRequestSize = 1024 * 1024 * 15 // 15 MB
)
public class PersonalCabinetServlet extends HttpServlet {

  // Директория для сохранения резюме
  private static final String UPLOAD_DIR = "uploads/resumes";

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // Проверяем аутентификацию пользователя
    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("authenticated") == null) {
      response.sendRedirect(request.getContextPath() + "/login");
      return;
    }

    // Устанавливаем дополнительные данные, если их нет
    if (session.getAttribute("registrationDate") == null) {
      session.setAttribute("registrationDate", new Date());
    }

    if (session.getAttribute("userName") == null) {
      String userEmail = (String) session.getAttribute("userEmail");
      if (userEmail != null) {
        // Извлекаем имя из email (часть до @)
        String userName = userEmail.split("@")[0];
        session.setAttribute("userName", userName);
      }
    }

    request.getRequestDispatcher("/jsp/personal-cabinet.jsp").forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // Проверяем аутентификацию пользователя
    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("authenticated") == null) {
      response.sendRedirect(request.getContextPath() + "/login");
      return;
    }

    // Обработка загрузки резюме
    Part filePart = request.getPart("resumeFile");

    if (filePart != null && filePart.getSize() > 0) {
      String fileName = getFileName(filePart);

      // Проверяем расширение файла
      if (!isValidFileExtension(fileName)) {
        session.setAttribute("uploadError", "Неподдерживаемый формат файла. Разрешены: PDF, DOC, DOCX, TXT");
      } else if (filePart.getSize() > 10 * 1024 * 1024) { // 10 MB
        session.setAttribute("uploadError", "Файл слишком большой. Максимальный размер: 10 МБ");
      } else {
        // Сохраняем информацию о резюме в сессии
        session.setAttribute("resumeFilename", fileName);
        session.setAttribute("resumeUploadDate", new Date());
        session.setAttribute("resumeUploaded", true);
        session.removeAttribute("uploadError");

        // Сохраняем файл на сервере (опционально)
        try {
          saveUploadedFile(filePart, fileName, session);
        } catch (Exception e) {
          System.err.println("Ошибка сохранения файла: " + e.getMessage());
          // Продолжаем работу, так как информация уже в сессии
        }

        session.setAttribute("uploadSuccess", "Резюме успешно загружено!");
      }
    }

    // Перенаправляем обратно на страницу личного кабинета
    response.sendRedirect(request.getContextPath() + "/personal-cabinet");
  }

  // Метод для получения имени файла из Part
  private String getFileName(Part part) {
    String contentDisposition = part.getHeader("content-disposition");
    String[] tokens = contentDisposition.split(";");
    for (String token : tokens) {
      if (token.trim().startsWith("filename")) {
        return token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
      }
    }
    return "unknown";
  }

  // Проверка расширения файла
  private boolean isValidFileExtension(String fileName) {
    String[] allowedExtensions = {".pdf", ".doc", ".docx", ".txt"};
    fileName = fileName.toLowerCase();

    for (String ext : allowedExtensions) {
      if (fileName.endsWith(ext)) {
        return true;
      }
    }
    return false;
  }

  // Сохранение файла на сервере (опционально)
  private void saveUploadedFile(Part filePart, String fileName, HttpSession session) throws IOException {
    // Получаем путь к директории приложения
    String appPath = getServletContext().getRealPath("");
    String uploadPath = appPath + File.separator + UPLOAD_DIR;

    // Создаем директорию, если ее нет
    File uploadDir = new File(uploadPath);
    if (!uploadDir.exists()) {
      uploadDir.mkdirs();
    }

    // Генерируем уникальное имя файла с ID пользователя
    Long userId = (Long) session.getAttribute("userId");
    String uniqueFileName = (userId != null ? userId + "_" : "") +
        System.currentTimeMillis() + "_" + fileName;

    // Сохраняем файл
    File file = new File(uploadDir, uniqueFileName);
    try (InputStream inputStream = filePart.getInputStream()) {
      Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    // Сохраняем путь к файлу в сессии
    session.setAttribute("resumeFilePath", uploadPath + File.separator + uniqueFileName);
    session.setAttribute("resumeFileUrl", UPLOAD_DIR + "/" + uniqueFileName);
  }
}