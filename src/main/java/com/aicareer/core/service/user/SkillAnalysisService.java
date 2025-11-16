package com.aicareer.core.service.user;

import java.util.Map;

public interface SkillAnalysisService {
  Map<String, Object> analyzeSkillLevel(Long userId, String targetPosition);
}