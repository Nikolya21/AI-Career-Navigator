package com.aicareer.core.service.user;

import com.aicareer.core.model.user.User;
import com.aicareer.module.user.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRepositoryImpl implements UserRepository {

  private final Map<Long, User> userStore = new ConcurrentHashMap<>();
  private final Map<String, User> emailIndex = new ConcurrentHashMap<>();
  private final AtomicLong idGenerator = new AtomicLong(1);

  @Override
  public User save(User user) {
    if (user.getId() == null) {
      user.setId(idGenerator.getAndIncrement());
    }
    userStore.put(user.getId(), user);
    emailIndex.put(user.getEmail().toLowerCase(), user);
    return user;
  }

  @Override
  public Optional<User> findById(Long id) {
    return Optional.ofNullable(userStore.get(id));
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return Optional.ofNullable(emailIndex.get(email.toLowerCase()));
  }

  @Override
  public boolean existsByEmail(String email) {
    return emailIndex.containsKey(email.toLowerCase());
  }
}