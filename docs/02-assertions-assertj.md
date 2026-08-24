# 2. Assertions with AssertJ

Companion code: [`CalculatorTest.java`](../src/test/java/com/example/guide/beginner/CalculatorTest.java)

JUnit 5 ships its own `Assertions` class (`assertEquals`, `assertTrue`,
`assertThrows`, ...). This guide uses **AssertJ** instead, because its
fluent, chainable API reads closer to English and gives far better failure
messages. Both work together fine — AssertJ isn't a replacement framework,
just a better assertion library layered on top of JUnit.

## Basic assertions

```java
import static org.assertj.core.api.Assertions.assertThat;

assertThat(result).isEqualTo(5);
assertThat(name).isEqualTo("Alice");
assertThat(flag).isTrue();
assertThat(value).isNotNull();
```

Compare the failure message you get from each style when `result` is `4`
instead of `5`:

- JUnit: `expected: <5> but was: <4>`
- AssertJ: `expected: 5\n but was: 4` — same info, but AssertJ's messages
  get dramatically more useful once you move to collections and objects.

## Collections

```java
assertThat(users)
        .hasSize(2)
        .extracting(User::getName)
        .containsExactly("Alice", "Bob");

assertThat(users).isEmpty();
assertThat(users).containsExactlyInAnyOrder(bob, alice); // order-independent
```

## Exceptions

```java
import static org.assertj.core.api.Assertions.assertThatThrownBy;

assertThatThrownBy(() -> calculator.divide(10, 0))
        .isInstanceOf(ArithmeticException.class)
        .hasMessage("Cannot divide by zero");
```

The code under test must be wrapped in a lambda so AssertJ can invoke it
and capture the exception itself, rather than the exception propagating
and failing the test before the assertion runs.

For exception-only tests, plain JUnit's `assertThrows` is equally common
and returns the exception for further inspection:

```java
ArithmeticException ex = assertThrows(ArithmeticException.class,
        () -> calculator.divide(10, 0));
assertThat(ex.getMessage()).isEqualTo("Cannot divide by zero");
```

## Chaining and soft assertions

Assertions on the same subject chain fluently:

```java
assertThat(user.getName())
        .isNotBlank()
        .startsWith("A")
        .hasSizeGreaterThan(2);
```

When you want to see *every* failing assertion in one run instead of
stopping at the first, use `SoftAssertions`:

```java
SoftAssertions softly = new SoftAssertions();
softly.assertThat(user.getName()).isEqualTo("Alice");
softly.assertThat(user.getEmail()).isEqualTo("alice@example.com");
softly.assertAll(); // reports all failures together
```

## One assertion concept per test

A test can have multiple `assertThat(...)` calls, but they should all be
verifying the *same behavior* (e.g. several fields of one result object).
If you're asserting two unrelated behaviors, split into two tests — it
makes failures immediately diagnosable ("which behavior broke?") instead
of requiring you to read the test body to find out.

## Next

→ [3. Parameterized & advanced JUnit 5](03-junit5-parameterized.md)
