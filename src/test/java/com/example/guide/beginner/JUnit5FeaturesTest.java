package com.example.guide.beginner;

import com.example.guide.util.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeout;

/**
 * STEP 2 of the guide: JUnit 5 features beyond a single @Test method —
 * lifecycle hooks, parameterized tests, tags, assumptions, and timeouts.
 */
@DisplayName("JUnit 5 feature tour")
class JUnit5FeaturesTest {

    @BeforeAll
    static void beforeAll() {
        // Runs once before all tests in this class. Must be static (unless the
        // class uses @TestInstance(Lifecycle.PER_CLASS)).
        System.out.println("Starting JUnit5FeaturesTest suite");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("Finished JUnit5FeaturesTest suite");
    }

    @ParameterizedTest(name = "\"{0}\" is a palindrome")
    @ValueSource(strings = {"racecar", "level", "A man a plan a canal Panama", "noon"})
    @DisplayName("recognizes palindromes")
    void recognizesPalindromes(String candidate) {
        assertThat(StringUtils.isPalindrome(candidate)).isTrue();
    }

    @ParameterizedTest(name = "\"{0}\" is NOT a palindrome")
    @ValueSource(strings = {"hello", "java", "mocking"})
    void rejectsNonPalindromes(String candidate) {
        assertThat(StringUtils.isPalindrome(candidate)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("blank/null input is never a palindrome match worth trusting, isBlank agrees")
    void blankInputsAreBlank(String candidate) {
        assertThat(StringUtils.isBlank(candidate)).isTrue();
    }

    @ParameterizedTest(name = "{0} + {1} = {2}")
    @CsvSource({
            "1, 1, 2",
            "2, 3, 5",
            "10, -5, 5"
    })
    @DisplayName("CsvSource feeds multiple arguments per invocation")
    void addsWithCsvSource(int a, int b, int expectedSum) {
        assertThat(a + b).isEqualTo(expectedSum);
    }

    @Test
    @Tag("fast")
    @DisplayName("tags let you group and selectively run tests (mvn test -Dgroups=fast)")
    void taggedTest() {
        assertThat(1 + 1).isEqualTo(2);
    }

    @Test
    @DisplayName("assumptions skip a test instead of failing it when a precondition is unmet")
    void skippedViaAssumption() {
        Assumptions.assumeTrue(System.getenv("CI") == null, "Skipped on CI");
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("assertTimeout fails the test if it runs too long")
    void completesWithinTimeout() {
        assertTimeout(Duration.ofMillis(500), () -> {
            // simulate fast work
            int sum = 0;
            for (int i = 0; i < 1000; i++) sum += i;
        });
    }

    @Test
    @Disabled("Example of temporarily disabling a test with a documented reason")
    void disabledExample() {
        Assertions.fail("Should never run");
    }
}
