package com.aicareer.core.model.courseModel.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.List;

@Converter
public class UrlsConverter implements AttributeConverter<List<String>, String> {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(List<String> attribute) {
    if (attribute == null) return null;
    try {
      return objectMapper.writeValueAsString(attribute);
    } catch (IOException e) {
      throw new RuntimeException("Failed to convert list to JSON", e);
    }
  }

  @Override
  public List<String> convertToEntityAttribute(String dbData) {
    if (dbData == null) return null;
    try {
      return objectMapper.readValue(dbData, new TypeReference<List<String>>() {});
    } catch (IOException e) {
      throw new RuntimeException("Failed to convert JSON to list", e);
    }
  }
}