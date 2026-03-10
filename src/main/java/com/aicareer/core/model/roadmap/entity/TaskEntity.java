package com.aicareer.core.model.roadmap.entity;

import com.aicareer.core.model.courseModel.converter.UrlsConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tasks", schema = "aicareer")
public class TaskEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "week_id", nullable = false)
  private Long weekId;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Convert(converter = UrlsConverter.class)
  @Column(columnDefinition = "TEXT")
  private List<String> urls;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = Instant.now();
  }
}