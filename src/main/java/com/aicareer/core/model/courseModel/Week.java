package com.aicareer.core.model.courseModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Week {
    private int number;
    private String goal;
    private List<Task> tasks;
}
