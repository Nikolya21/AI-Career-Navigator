package com.aicareer.core.model.roadmap.entity;

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
@Table(name = "roadmap_zones", schema = "aicareer")
public class RoadmapZoneEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "roadmap_id", nullable = false)
  private Long roadmapId;

  @Column(nullable = false)
  private String name;

  @Column(name = "learning_goal", columnDefinition = "TEXT")
  private String learningGoal;

  @Column(name = "complexity_level")
  private String complexityLevel;

  @Column(name = "zone_order")
  private Integer zoneOrder;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @OneToMany(mappedBy = "roadmapZoneId", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<WeekEntity> weeks = new ArrayList<>();

  @PrePersist
  protected void onCreate() {
    createdAt = Instant.now();
  }
}