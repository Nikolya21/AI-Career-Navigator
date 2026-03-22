package com.aicareer.core.service.user.mapper;

import com.aicareer.core.model.user.User;
import com.aicareer.core.model.user.entity.UserEntity;

public class UserMapper {

  public static User toModel(UserEntity entity) {
    if (entity == null) return null;
    return User.builder()
        .id(entity.getId())
        .name(entity.getName())
        .email(entity.getEmail())
        .passwordHash(entity.getPasswordHash())
        .vacancyNow(entity.getVacancyNow())
        .roadmapId(entity.getRoadmapId())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  public static UserEntity toEntity(User model) {
    if (model == null) return null;
    UserEntity entity = UserEntity.builder()
        .id(model.getId())
        .name(model.getName())
        .email(model.getEmail())
        .passwordHash(model.getPasswordHash())
        .vacancyNow(model.getVacancyNow())
        .roadmapId(model.getRoadmapId())
        .createdAt(model.getCreatedAt())
        .updatedAt(model.getUpdatedAt())
        .build();
    return entity;
  }
}