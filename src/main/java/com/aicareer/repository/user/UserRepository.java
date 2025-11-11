package com.aicareer.repository.user;

import com.aicareer.core.model.user.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
  User save(User user);
  Optional<User> findById(Long id);
  Optional<User> findByEmail(String email);
  boolean existsByEmail(String email);
  boolean delete(Long id);
  List<User> findAll();
}