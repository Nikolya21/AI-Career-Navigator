package com.aicareer.module.user;

import java.util.Map;

public interface SkillAnalysisServiceRepository {
  Map<String, Object> analyzeSkillLevel(Long userId, String targetPosition);
}