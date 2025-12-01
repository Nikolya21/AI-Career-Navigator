package com.aicareer.app.servlets;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/upload-avatar")
@MultipartConfig(
  fileSizeThreshold = 1024 * 1024,
  maxFileSize = 5 * 1024 * 1024,
  maxRequestSize = 10 * 1024 * 1024
)
public class UploadAvatarServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

    Part filePart = request.getPart("avatarFile");
    String uploadError = null;

    if (filePart == null || filePart.getSize() == 0) {
      uploadError = "Файл не выбран.";
    } else {
      String contentType = filePart.getContentType();
      if (contentType == null || !contentType.equals("image/png")) {
        uploadError = "Неверный формат. Разрешены только PNG.";
      } else {
        HttpSession session = request.getSession();
        String fileName = extractFileName(filePart);
        session.setAttribute("avatarFileName", fileName);
        session.setAttribute("avatarUploaded", true);
        // TODO: Сохранить InputStream в БД или на диск
        // try (InputStream is = filePart.getInputStream()) { ... }
      }
    }

    // Возврат в личный кабинет с ошибкой или без
    if (uploadError != null) {
      request.setAttribute("uploadError", uploadError);
      request.getRequestDispatcher("/jsp/personal-cabinet.jsp").forward(request, response);
    } else {
      response.sendRedirect(request.getContextPath() + "/personal-cabinet");
    }
  }

  private String extractFileName(Part part) {
    String contentDisp = part.getHeader("content-disposition");
    if (contentDisp != null) {
      for (String token : contentDisp.split(";")) {
        token = token.trim();
        if (token.startsWith("filename")) {
          String fileName = token.substring(token.indexOf('=') + 1).replace("\"", "");
          return fileName.substring(fileName.lastIndexOf('/') + 1)
            .substring(fileName.lastIndexOf('\\') + 1);
        }
      }
    }
    return "avatar.png";
  }
}
