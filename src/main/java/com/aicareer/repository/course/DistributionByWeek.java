package com.aicareer.repository.course;
import com.aicareer.core.model.Week;

import java.util.List;

public interface DistributionByWeek {
  public List<Week> distributionByWeek(List<Week> responseByWeek);
}
