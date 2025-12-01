package com.aicareer.repository.user;

import com.aicareer.core.model.user.UserPreferences;

import java.util.Optional;

public interface UserPreferencesRepository {

    UserPreferences save(UserPreferences userPreferences);
    Optional<UserPreferences> findById(Long id);
    Optional<UserPreferences> findByUserId(Long userId);
    boolean delete(Long id);
    boolean deleteByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
