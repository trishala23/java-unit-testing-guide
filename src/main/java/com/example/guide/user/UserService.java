package com.example.guide.user;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Depends only on the {@link UserRepository} interface. In tests, this
 * dependency is what gets mocked — UserService itself is the "real" object
 * under test (never mock the class you're testing).
 */
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No user with id " + id));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User registerUser(String name, String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email: " + email);
        }
        userRepository.findByEmail(email).ifPresent(existing -> {
            throw new IllegalStateException("Email already registered: " + email);
        });
        User user = new User(0, name, email, true);
        return userRepository.save(user);
    }

    public void deactivateUser(long id) {
        User user = getUserById(id);
        user.setActive(false);
        userRepository.save(user);
    }

    public void deleteUser(long id) {
        // confirm the user exists before attempting delete
        getUserById(id);
        userRepository.deleteById(id);
    }
}
