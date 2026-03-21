package com.aicareer.app.servlets;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/upload-document")
@MultipartConfig(
  fileSizeThreshold = 1024 * 1024, // 1 MB
  maxFileSize = 5 * 1024 * 1024,    // 5 MB
  maxRequestSize = 10 * 1024 * 1024 // 10 MB
)
public class UploadDocumentServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

    Part filePart = request.getPart("documentFile");
    String uploadError = null;

    if (filePart == null || filePart.getSize() == 0) {
      uploadError = "Файл не выбран.";
    } else {
      String contentType = filePart.getContentType();
      // Проверяем на PDF или DOCX
      if (!"application/pdf".equals(contentType) &&
        !"application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(contentType)) {
        uploadError = "Разрешены только PDF или DOCX.";
      } else {
        HttpSession session = request.getSession();
        String fileName = extractFileName(filePart);
        session.setAttribute("uploadedDocumentName", fileName);
        session.setAttribute("documentUploaded", true);
        // TODO: Сохранить файл (на диск / в БД)
      }
    }

    // Всегда редирект — чтобы избежать дублирования шапки
    String redirectUrl = request.getContextPath() + "/personal-cabinet";
    if (uploadError != null) {
      String encodedError = URLEncoder.encode(uploadError, StandardCharsets.UTF_8);
      redirectUrl += "?error=" + encodedError;
    }

    response.sendRedirect(redirectUrl);
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
    return "document.pdf";
  }
}