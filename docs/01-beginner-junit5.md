# 1. Getting started with JUnit 5

Companion code: [`CalculatorTest.java`](../src/test/java/com/example/guide/beginner/CalculatorTest.java)

## What is a unit test?

A unit test exercises one small piece of behavior (ideally one method, or
one class) in isolation, checks the result against an expectation, and
runs in milliseconds with no external dependencies (no database, no
network, no filesystem). JUnit 5 is the standard framework for writing and
running these in Java.

## Anatomy of a test

```java
class CalculatorTest {

    private Calculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new Calculator(); // fresh instance per test
    }

    @Test
    void addsTwoPositiveNumbers() {
        int result = calculator.add(2, 3);
        assertThat(result).isEqualTo(5);
    }
}
```

- **`@Test`** marks a method as a test case. JUnit discovers and runs every
  `@Test` method in the class.
- **`@BeforeEach`** runs before *every* test method — use it to build a
  fresh instance of the class under test so tests can't leak state into
  each other.
- **`@AfterEach`** runs after every test — use it for cleanup (closing
  files, resetting static state). Rare in pure unit tests, common in
  integration tests.
- **`@BeforeAll` / `@AfterAll`** run once for the whole class (must be
  `static`, unless you opt into `@TestInstance(Lifecycle.PER_CLASS)`).
  Use for expensive one-time setup shared safely across tests.

## Naming and structure

- Test class name: `<ClassUnderTest>Test` (e.g. `Calculator` → `CalculatorTest`).
  Maven's Surefire plugin auto-discovers classes matching `*Test.java` by
  default.
- Test method name: describe the behavior, not the mechanics —
  `throwsOnDivideByZero()` is better than `test3()`.
- **`@DisplayName("...")`** gives a human-readable name shown in test
  reports and IDEs, independent of the Java method name.
- **`@Nested`** groups related tests into inner classes — useful once a
  test class covers several behaviors of the same unit (see `Addition` and
  `Division` in `CalculatorTest`).

## The Arrange-Act-Assert (AAA) pattern

Structure every test body in three parts:

```java
@Test
void throwsOnDivideByZero() {
    // Arrange: nothing extra needed here, calculator is ready from setUp()

    // Act
    // (wrapped in a lambda because we're asserting on an exception — see doc 02)

    // Assert
    assertThatThrownBy(() -> calculator.divide(10, 0))
            .isInstanceOf(ArithmeticException.class);
}
```

Keeping these visually or mentally separated (blank line, or comments while
learning) makes tests easy to scan later.

## Running tests

```bash
mvn test                       # run everything
mvn -Dtest=CalculatorTest test  # run one class
mvn -Dtest=CalculatorTest#addsTwoPositiveNumbers test  # run one method
```

Most IDEs (IntelliJ, VS Code with the Java extension) also let you run/debug
a single test by clicking the gutter icon next to `@Test`.

## Next

→ [2. Assertions with AssertJ](02-assertions-assertj.md)
