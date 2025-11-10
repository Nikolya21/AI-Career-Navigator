package com.aicareer.core.model.user;

import java.io.File;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVData {
  private File file;
  private String information; // текст, извлеченный из file(он может быть только в формате PDF или DOCX)

  private LocalDateTime uploadedAt;

  public void updateTimestamps() {
      uploadedAt = LocalDateTime.now();
  }
}
