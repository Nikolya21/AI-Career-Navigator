package com.aicareer.core.model.roadmap.entity;

import com.aicareer.core.model.courseModel.Task;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "weeks", schema = "aicareer")
public class WeekEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "roadmap_zone_id", nullable = false)
  private Long roadmapZoneId;

  @Column(name = "week_number")
  private int number;

  @Column(columnDefinition = "TEXT")
  private String goal;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @OneToMany(mappedBy = "weekId", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TaskEntity> tasks = new ArrayList<>();

  @PrePersist
  protected void onCreate() {
    createdAt = Instant.now();
  }
}