package com.aicareer.core.DTO;

import com.aicareer.core.model.Week;
import java.util.List;

public class ResponseByWeek {
  private List<Week> weeks;

  public ResponseByWeek(List<Week> weeks) {
    this.weeks = weeks;
  }

  public List<Week> getWeeks() {
    return weeks;
  }
}