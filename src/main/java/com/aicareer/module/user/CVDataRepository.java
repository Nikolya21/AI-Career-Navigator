package com.aicareer.module.user;

import com.aicareer.core.model.user.CVData;

public interface CVDataRepository {
  void save(CVData CVData);
  CVData findByUserId(Long userId);
  void deleteByUserId(Long userId);
}
