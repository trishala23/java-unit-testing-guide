package com.example.guide.mockito;

import com.example.guide.user.User;
import com.example.guide.user.UserRepository;
import com.example.guide.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * STEP 4 of the guide: DIFFERENT WAYS OF MOCKING.
 *
 * There is more than one way to create a mock in Mockito. This file walks
 * through each style so you can recognize (and choose between) them in
 * real codebases.
 */
@DisplayName("Different ways of mocking")
class MockingStylesTest {

    // ------------------------------------------------------------------
    // STYLE 1: Mockito.mock(...) — plain Java, no annotations, no
    // extension needed. Works anywhere, including outside JUnit.
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Style 1: Mockito.mock() — manual, framework-agnostic")
    void manualMockCreation() {
        UserRepository repository = Mockito.mock(UserRepository.class);
        when(repository.findById(1L)).thenReturn(Optional.of(new User(1L, "Alice", "a@x.com", true)));

        UserService service = new UserService(repository);

        assertThat(service.getUserById(1L).getName()).isEqualTo("Alice");
    }

    // ------------------------------------------------------------------
    // STYLE 2: @Mock + @ExtendWith(MockitoExtension.class) — declarative,
    // the most common style in modern JUnit 5 codebases. Mockito creates
    // the mock and injects it into the field automatically.
    // ------------------------------------------------------------------
    @ExtendWith(MockitoExtension.class)
    @DisplayName("Style 2: @Mock annotation")
    static class AnnotationStyle {

        @Mock
        private UserRepository userRepository;

        @Test
        @DisplayName("field is a ready-to-use mock before the test body runs")
        void mockFieldIsUsable() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(new User(1L, "Bob", "b@x.com", true)));

            assertThat(userRepository.findById(1L)).isPresent();
        }
    }

    // ------------------------------------------------------------------
    // STYLE 3: @Mock + @InjectMocks — Mockito also constructs the class
    // under test and auto-wires its @Mock fields into it (via constructor,
    // setter, or field injection, in that order). Convenient, but implicit —
    // many teams prefer STYLE 2's explicit `new UserService(mock)` in
    // setUp() because it's easier to read and doesn't silently break when
    // the constructor signature changes.
    // ------------------------------------------------------------------
    @ExtendWith(MockitoExtension.class)
    @DisplayName("Style 3: @InjectMocks")
    static class InjectMocksStyle {

        @Mock
        private UserRepository userRepository;

        @InjectMocks
        private UserService userService; // Mockito finds the (UserRepository) constructor and injects the mock

        @Test
        @DisplayName("dependency is injected automatically, no manual `new`")
        void dependencyIsAutoInjected() {
            when(userRepository.findById(2L)).thenReturn(Optional.of(new User(2L, "Carol", "c@x.com", true)));

            assertThat(userService.getUserById(2L).getName()).isEqualTo("Carol");
        }
    }

    // ------------------------------------------------------------------
    // STYLE 4: mocking with a custom default Answer — instead of null/0/
    // empty-collection defaults, define what EVERY unstubbed call returns.
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Style 4: mock with RETURNS_DEEP_STUBS for chained calls")
    void deepStubs() {
        // Deep stubs let you stub a chain (a.b().c()) without stubbing each
        // link manually. Use sparingly — it often signals the design should
        // expose a simpler collaborator interface instead.
        Clock clock = mock(Clock.class, RETURNS_DEEP_STUBS);
        when(clock.instant()).thenReturn(Instant.parse("2024-01-01T00:00:00Z"));

        assertThat(clock.instant()).isEqualTo(Instant.parse("2024-01-01T00:00:00Z"));
    }

    // ------------------------------------------------------------------
    // STYLE 5: strict stubbing / lenient — MockitoExtension is strict by
    // default and fails a test that stubs something it never calls
    // (UnnecessaryStubbingException). lenient() opts a specific stub out.
    // ------------------------------------------------------------------
    @ExtendWith(MockitoExtension.class)
    @MockitoSettings(strictness = Strictness.LENIENT)
    @DisplayName("Style 5: lenient stubbing")
    static class LenientStyle {

        @Mock
        private UserRepository userRepository;

        @Test
        @DisplayName("a stub set up but not used in this test does not fail the suite")
        void unusedStubIsTolerated() {
            lenient().when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
            // This test doesn't call findById at all — normally that would be
            // flagged as an unnecessary stub under strict mode.
            assertThat(true).isTrue();
        }
    }

    // ------------------------------------------------------------------
    // STYLE 6: real, non-final classes CAN be mocked too — Mockito doesn't
    // require an interface. Concrete classes work the same way (the
    // mockito-inline dependency in this project also enables mocking final
    // classes/methods and static methods — see docs/07-advanced-topics.md).
    // ------------------------------------------------------------------
    @Test
    @DisplayName("Style 6: mocking a concrete class, not just an interface")
    void mockingConcreteClass() {
        Clock fixedClock = mock(Clock.class);
        when(fixedClock.getZone()).thenReturn(ZoneOffset.UTC);

        assertThat(fixedClock.getZone()).isEqualTo(ZoneOffset.UTC);
    }
}
