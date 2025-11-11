package com.aicareer.core.service.user;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordEncoder {

  private static final int SALT_LENGTH = 16;
  private static final int ITERATIONS = 10000;

  public String encode(String password) {
    try {
      byte[] salt = generateSalt();
      byte[] hash = hashPassword(password, salt);

      byte[] combined = new byte[salt.length + hash.length];
      System.arraycopy(salt, 0, combined, 0, salt.length);
      System.arraycopy(hash, 0, combined, salt.length, hash.length);

      return Base64.getEncoder().encodeToString(combined);

    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Error encoding password", e);
    }
  }

  public boolean matches(String password, String encodedPassword) {
    try {
      byte[] combined = Base64.getDecoder().decode(encodedPassword);
      byte[] salt = new byte[SALT_LENGTH];
      byte[] storedHash = new byte[combined.length - SALT_LENGTH];

      System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);
      System.arraycopy(combined, SALT_LENGTH, storedHash, 0, storedHash.length);

      byte[] computedHash = hashPassword(password, salt);

      return MessageDigest.isEqual(storedHash, computedHash);

    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Error verifying password", e);
    }
  }

  private byte[] generateSalt() {
    byte[] salt = new byte[SALT_LENGTH];
    new SecureRandom().nextBytes(salt);
    return salt;
  }

  private byte[] hashPassword(String password, byte[] salt) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    digest.reset();
    digest.update(salt);

    byte[] hash = digest.digest(password.getBytes());

    for (int i = 0; i < ITERATIONS - 1; i++) {
      digest.reset();
      hash = digest.digest(hash);
    }

    return hash;
  }
}