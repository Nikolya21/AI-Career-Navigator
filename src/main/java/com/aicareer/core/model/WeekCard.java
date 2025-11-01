package com.aicareer.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeekCard {
    private int id;
    private List<String> tastForTheWeek;
}
