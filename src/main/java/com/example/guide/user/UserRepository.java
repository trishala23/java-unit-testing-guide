package com.example.guide.user;

import java.util.List;
import java.util.Optional;

/**
 * An interface with no implementation shipped in this module on purpose —
 * in real projects this would be a database-backed repository (JPA, JDBC...).
 * Tests never need a real implementation: they mock this interface.
 */
public interface UserRepository {

    Optional<User> findById(long id);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    User save(User user);

    void deleteById(long id);
}
