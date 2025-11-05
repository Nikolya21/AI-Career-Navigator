package com.aicareer.core.service.user;

import com.aicareer.module.user.SkillAnalysisServiceRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.logging.Logger;

@Service
public class SkillAnalysisService implements SkillAnalysisServiceRepository {

  private final Logger log = Logger.getLogger(SkillAnalysisService.class.getName());

  @Override
  public Map<String, Object> analyzeSkillLevel(Long userId, String targetPosition) {
    log.info(String.format("Analyzing skilupdatels for user {%s} targeting {%s}", userId, targetPosition));

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
    gaps.put("Java", 0.75);
    gaps.put("Spring Framework", 0.60);
    gaps.put("SQL", 0.45);
    gaps.put("Docker", 0.80);
    return gaps;
  }

  private List<String> generateRecommendations(String targetPosition) {
    return Arrays.asList(
        "Complete Spring Boot certification",
        "Practice Docker containerization",
        "Learn advanced SQL queries",
        "Build portfolio project with microservices"
    );
  }
}