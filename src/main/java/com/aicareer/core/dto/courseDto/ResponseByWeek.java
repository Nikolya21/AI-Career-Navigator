package com.aicareer.core.dto.courseDto;

import com.aicareer.core.model.courseModel.Week;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseByWeek {
    private List<Week> weeks;
}
