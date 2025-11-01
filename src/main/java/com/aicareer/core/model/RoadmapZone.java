package com.aicareer.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoadmapZone {
    private int id;
    private int sid;
    private List<WeekCard> weekCardInZone;
}
