package com.aicareer.repository.course;
import com.aicareer.core.model.courseModel.Week;

import java.util.List;

public interface DistributionByWeek {
  public List<Week> distributionByWeek(List<Week> responseByWeek);
}
