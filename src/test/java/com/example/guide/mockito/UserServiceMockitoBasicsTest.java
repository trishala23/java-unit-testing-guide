package com.example.guide.mockito;

import com.example.guide.user.User;
import com.example.guide.user.UserRepository;
import com.example.guide.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * STEP 3 of the guide: your first mock.
 *
 * WHY MOCK AT ALL? UserService depends on UserRepository, which in a real
 * app talks to a database. In a unit test we don't want a database — we
 * want to test UserService's LOGIC in isolation, assuming its collaborator
 * behaves a certain way. A mock is a fake UserRepository we fully control.
 *
 * @ExtendWith(MockitoExtension.class) wires up Mockito for JUnit 5: it
 * initializes every @Mock field before each test and validates stubbing
 * usage (e.g. flags stubs you set up but never used).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService (Mockito basics)")
class UserServiceMockitoBasicsTest {

    @Mock
    private UserRepository userRepository; // a fake, controllable UserRepository

    private UserService userService;

    @BeforeEach
    void setUp() {
        // The class under test is REAL. Only its dependency is mocked.
        userService = new UserService(userRepository);
    }

    @Test
    @DisplayName("getUserById returns the user when the repository finds one")
    void getUserByIdReturnsUser() {
        User alice = new User(1L, "Alice", "alice@example.com", true);
        // STUBBING: "when the mock is called this way, return this value"
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));

        User result = userService.getUserById(1L);

        assertThat(result).isEqualTo(alice);
    }

    @Test
    @DisplayName("getUserById throws when the repository finds nothing")
    void getUserByIdThrowsWhenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("getAllUsers delegates directly to the repository")
    void getAllUsersDelegates() {
        List<User> users = List.of(
                new User(1L, "Alice", "alice@example.com", true),
                new User(2L, "Bob", "bob@example.com", true)
        );
        when(userRepository.findAll()).thenReturn(users);

        assertThat(userService.getAllUsers()).containsExactlyElementsOf(users);
    }

    @Test
    @DisplayName("registerUser saves a new user and returns the saved instance")
    void registerUserSaves() {
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        User saved = new User(10L, "New", "new@example.com", true);
        // any() matcher: accept a User with any field values
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = userService.registerUser("New", "new@example.com");

        assertThat(result).isEqualTo(saved);
    }

    @Test
    @DisplayName("registerUser rejects an invalid email WITHOUT touching the repository")
    void registerUserRejectsInvalidEmail() {
        assertThatThrownBy(() -> userService.registerUser("Bad", "not-an-email"))
                .isInstanceOf(IllegalArgumentException.class);

        // Prove the repository was never called: fast-fail validation should
        // short-circuit before any collaborator interaction.
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("a mock returns sensible defaults for un-stubbed calls")
    void unstubbedCallsReturnDefaults() {
        // No when(...) set up for findAll() here. Mockito returns an empty
        // list (its default for List-returning methods) rather than null.
        assertThat(userRepository.findAll()).isEmpty();
    }
}
