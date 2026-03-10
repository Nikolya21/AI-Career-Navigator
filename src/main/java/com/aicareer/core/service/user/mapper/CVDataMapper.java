package com.aicareer.core.service.user.mapper;

import com.aicareer.core.model.user.CVData;
import com.aicareer.core.model.user.entity.CVDataEntity;
import com.aicareer.core.model.user.entity.UserEntity;

import java.io.File;

public class CVDataMapper {

  public static CVData toModel(CVDataEntity entity) {
    if (entity == null) return null;
    // В старой модели есть поле File, но мы не можем восстановить файл из байт без создания временного файла.
    // Пока оставляем file = null.
    return CVData.builder()
        .id(entity.getId())
        .userId(entity.getUser().getId())
        .file(null) // или можно создать временный файл, но это затратно
        .information(entity.getInformation())
        .uploadedAt(entity.getUploadedAt())
        .build();
  }

  public static CVDataEntity toEntity(CVData model, UserEntity userEntity, byte[] fileContent) {
    if (model == null) return null;
    return CVDataEntity.builder()
        .id(model.getId())
        .user(userEntity)
        .information(model.getInformation())
        .uploadedAt(model.getUploadedAt())
        .build();
  }
}