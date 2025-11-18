package com.aicareer.repository.user;

import com.aicareer.core.model.user.CVData;
import java.util.Optional;

public interface CVDataRepository {
  CVData save(CVData cvData);
  Optional<CVData> findByUserId(Long userId);
}
