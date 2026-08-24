package com.example.guide.beginner;

import com.example.guide.util.Calculator;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * STEP 1 of the guide: plain JUnit 5, no mocking at all.
 *
 * Read docs/01-beginner-junit5.md alongside this file. Key ideas demonstrated:
 *  - @Test marks a test method
 *  - @BeforeEach / @AfterEach run around every test
 *  - @DisplayName gives a human-readable test name
 *  - @Nested groups related tests
 *  - AssertJ's assertThat(...) reads like a sentence
 */
@DisplayName("Calculator")
class CalculatorTest {

    private Calculator calculator;

    @BeforeEach
    void setUp() {
        // A fresh instance for every test method avoids state leaking between tests.
        calculator = new Calculator();
    }

    @Nested
    @DisplayName("addition")
    class Addition {

        @Test
        @DisplayName("adds two positive numbers")
        void addsTwoPositiveNumbers() {
            int result = calculator.add(2, 3);

            assertThat(result).isEqualTo(5);
        }

        @Test
        @DisplayName("adds a negative and a positive number")
        void addsNegativeAndPositive() {
            assertThat(calculator.add(-2, 3)).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("division")
    class Division {

        @Test
        @DisplayName("divides two numbers")
        void dividesTwoNumbers() {
            assertThat(calculator.divide(10, 2)).isEqualTo(5.0);
        }

        @Test
        @DisplayName("throws ArithmeticException when dividing by zero")
        void throwsOnDivideByZero() {
            assertThatThrownBy(() -> calculator.divide(10, 0))
                    .isInstanceOf(ArithmeticException.class)
                    .hasMessage("Cannot divide by zero");
        }
    }

    @Test
    @DisplayName("isEven correctly classifies numbers")
    void isEven() {
        assertThat(calculator.isEven(4)).isTrue();
        assertThat(calculator.isEven(7)).isFalse();
    }
}
