package com.aicareer.core.model;

import com.aicareer.core.model.Week;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapZone {
    private Long id;
    private Long roadmapId;
    private String name;
    private List<Week> weeks = new ArrayList<>();
    private String learningGoal;
    private String complexityLevel;
    private Integer zoneOrder;
    private LocalDateTime createdAt;

    public void addWeek(Week week) {
        if (this.weeks == null) {
            this.weeks = new ArrayList<>();
        }
        this.weeks.add(week);
    }
}
