package com.aicareer.core.model.user;

import java.io.File;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.protocol.types.Field;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CVData {
  private Long id;
  private Long userId;
  private File file;
  private String information;
}
