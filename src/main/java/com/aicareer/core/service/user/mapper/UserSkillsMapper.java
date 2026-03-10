package com.aicareer.core.service.user.mapper;

import com.aicareer.core.model.user.UserSkills;
import com.aicareer.core.model.user.entity.UserSkillsEntity;
import com.aicareer.core.model.user.entity.UserEntity;

public class UserSkillsMapper {

  public static UserSkills toModel(UserSkillsEntity entity) {
    if (entity == null) return null;
    return UserSkills.builder()
        .id(entity.getId())
        .userId(entity.getUser().getId())
        .fullCompliancePercentage(entity.getFullCompliancePercentage())
        .skillGaps(entity.getSkillGaps())
        .calculatedAt(entity.getCalculatedAt())
        .build();
  }

  public static UserSkillsEntity toEntity(UserSkills model, UserEntity userEntity) {
    if (model == null) return null;
    return UserSkillsEntity.builder()
        .id(model.getId())
        .user(userEntity)
        .fullCompliancePercentage(model.getFullCompliancePercentage())
        .skillGaps(model.getSkillGaps())
        .calculatedAt(model.getCalculatedAt())
        .build();
  }
}