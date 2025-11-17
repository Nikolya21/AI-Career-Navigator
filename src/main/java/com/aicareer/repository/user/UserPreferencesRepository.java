package com.aicareer.repository.user;

import com.aicareer.core.model.user.UserPreferences;

import java.util.Optional;

public interface UserPreferencesRepository {

    /**
     * Сохранить или обновить настройки пользователя
     */
    UserPreferences save(UserPreferences userPreferences);

    /**
     * Найти настройки по ID
     */
    Optional<UserPreferences> findById(Long id);

    /**
     * Найти настройки по ID пользователя
     */
    Optional<UserPreferences> findByUserId(Long userId);

    /**
     * Удалить настройки по ID
     */
    boolean delete(Long id);

    /**
     * Удалить настройки по ID пользователя
     */
    boolean deleteByUserId(Long userId);

    /**
     * Проверить существование настроек для пользователя
     */
    boolean existsByUserId(Long userId);
}
