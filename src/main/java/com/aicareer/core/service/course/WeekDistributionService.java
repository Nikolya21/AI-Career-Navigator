package com.aicareer.core.service.course;

import com.aicareer.core.model.courseModel.Week;
import com.aicareer.repository.course.DistributionByWeek;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class WeekDistributionService implements DistributionByWeek {

  @Override
  public List<Week> distributionByWeek(List<Week> responseByWeek) {
    log.info("Распределение недель (без изменений)");
    return responseByWeek;
  }
}