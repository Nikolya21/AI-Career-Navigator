package com.aicareer.core.model;

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Week #").append(number).append("\n");
        sb.append("├── Goal: ").append(goal != null ? goal : "No goal specified").append("\n");

        if (tasks != null && !tasks.isEmpty()) {
            sb.append("└── Tasks:\n");
            for (int i = 0; i < tasks.size(); i++) {
                Task task = tasks.get(i);
                sb.append(i == tasks.size() - 1 ? "    └── " : "    ├── ")
                        .append("Task ").append(i + 1).append(":\n");

                // Description
                sb.append(i == tasks.size() - 1 ? "        └── " : "        ├── ")
                        .append("Description: ")
                        .append(task.getDescription() != null ? task.getDescription() : "No description")
                        .append("\n");

                // URLs
                if (task.getUrls() != null && !task.getUrls().isEmpty()) {
                    sb.append(i == tasks.size() - 1 ? "        └── " : "        ├── ")
                            .append("URLs: ").append(task.getUrls().size()).append(" links\n");

                    for (int j = 0; j < task.getUrls().size(); j++) {
                        String url = task.getUrls().get(j);
                        boolean isLastUrl = j == task.getUrls().size() - 1;
                        boolean isLastTask = i == tasks.size() - 1;

                        sb.append(isLastTask ? "            " : "        │   ");
                        sb.append(isLastUrl ? "└── " : "├── ")
                                .append(url)
                                .append("\n");
                    }
                } else {
                    sb.append(i == tasks.size() - 1 ? "        └── " : "        ├── ")
                            .append("URLs: No URLs provided\n");
                }
            }
        } else {
            sb.append("└── Tasks: No tasks assigned\n");
        }

        return sb.toString();
    }
}
