package com.aicareer.core.service.course;

import com.aicareer.core.model.Week;
import com.aicareer.repository.course.DistributionByWeek;
import java.util.List;

public class WeekDistributionService implements DistributionByWeek {

  @Override
  public List<Week> distributionByWeek(List<Week> responseByWeek) {
    // на будущее можно будет сделать:
    // - сортировку по номеру недели,
    // - ограничение по количеству недель и т.д.
    return responseByWeek;
  }
}