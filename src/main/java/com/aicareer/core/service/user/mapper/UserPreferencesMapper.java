package com.aicareer.core.service.user.mapper;

import com.aicareer.core.model.user.UserPreferences;
import com.aicareer.core.model.user.entity.UserPreferencesEntity;
import com.aicareer.core.model.user.entity.UserEntity;

public class UserPreferencesMapper {

  public static UserPreferences toModel(UserPreferencesEntity entity) {
    if (entity == null) return null;
    return UserPreferences.builder()
        .id(entity.getId())
        .userId(entity.getUser().getId())
        .infoAboutPerson(entity.getInfoAboutPerson())
        .build();
  }

  public static UserPreferencesEntity toEntity(UserPreferences model, UserEntity userEntity) {
    if (model == null) return null;
    return UserPreferencesEntity.builder()
        .id(model.getId())
        .user(userEntity)
        .infoAboutPerson(model.getInfoAboutPerson())
        .build();
  }
}