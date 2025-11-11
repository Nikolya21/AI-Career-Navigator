package com.aicareer.core.service.user;

import com.aicareer.repository.user.SkillAnalysisServiceRepository;
import java.time.LocalDateTime;
import java.util.*;

public class SkillAnalysisService implements SkillAnalysisServiceRepository {

  @Override
  public Map<String, Object> analyzeSkillLevel(Long userId, String targetPosition) {

    Map<String, Object> analysis = new HashMap<>();
    analysis.put("compliancePercentage", calculateCompliance(targetPosition));
    analysis.put("skillGaps", identifySkillGaps(targetPosition));
    analysis.put("recommendedActions", generateRecommendations(targetPosition));
    analysis.put("targetPosition", targetPosition);
    analysis.put("analysisDate", LocalDateTime.now());

    return analysis;
  }

  private double calculateCompliance(String targetPosition) {
    Random random = new Random();
    return 30.0 + random.nextDouble() * 50.0;
  }

  private Map<String, Double> identifySkillGaps(String targetPosition) {
    Map<String, Double> gaps = new HashMap<>();
    gaps.put("Java Spring", 0.75);
    gaps.put("Python", 0.60);
    gaps.put("SQL", 0.45);
    gaps.put("Docker", 0.80);
    return gaps;
  }

  private List<String> generateRecommendations(String targetPosition) {
    return Arrays.asList(
        "Learn Java Spring Boot",
        "Practice Python lib:Pytorch",
        "Spend more time for SQL injections",
        "Remind info about Docker"
    );
  }
}