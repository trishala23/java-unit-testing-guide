# 3. Parameterized & advanced JUnit 5

Companion code: [`JUnit5FeaturesTest.java`](../src/test/java/com/example/guide/beginner/JUnit5FeaturesTest.java)

## Parameterized tests

Instead of copy-pasting near-identical `@Test` methods for different
inputs, write one method and feed it a table of arguments.

```java
@ParameterizedTest(name = "\"{0}\" is a palindrome")
@ValueSource(strings = {"racecar", "level", "noon"})
void recognizesPalindromes(String candidate) {
    assertThat(StringUtils.isPalindrome(candidate)).isTrue();
}
```

Common argument sources:

| Annotation | Use for |
|---|---|
| `@ValueSource` | a single primitive/String argument per invocation |
| `@CsvSource` | multiple arguments per invocation, as inline CSV rows |
| `@CsvFileSource` | the same, loaded from a `.csv` resource file |
| `@MethodSource` | arguments computed by a factory method (complex objects) |
| `@NullAndEmptySource` / `@NullSource` / `@EmptySource` | edge cases for `null`/`""` |
| `@EnumSource` | every (or a filtered subset of) an enum's values |

```java
@ParameterizedTest(name = "{0} + {1} = {2}")
@CsvSource({
        "1, 1, 2",
        "2, 3, 5",
        "10, -5, 5"
})
void addsWithCsvSource(int a, int b, int expectedSum) {
    assertThat(a + b).isEqualTo(expectedSum);
}
```

Parameterized tests need the `junit-jupiter-params` artifact — already
included transitively via the `junit-jupiter` aggregator dependency in
this project's `pom.xml`.

## Tags

```java
@Test
@Tag("fast")
void taggedTest() { ... }
```

Run only tagged tests: `mvn -Dgroups=fast test`. Common uses: separating
`fast` unit tests from `slow` integration tests so CI can run the fast
suite on every push and the slow suite less often.

## Assumptions

`Assumptions.assumeTrue(...)` **skips** a test (reported separately from a
failure) when a precondition doesn't hold — e.g. skip an OS-specific test
on the wrong platform, or skip a test that needs a real network in CI.
Don't reach for this to hide a genuinely failing test — that's what
`@Disabled` (with a reason) is for, and even that should be temporary.

## Timeouts

```java
assertTimeout(Duration.ofMillis(500), () -> { /* work */ });
```

Fails the test if the block takes longer than the given duration. Prefer
this over manual `System.currentTimeMillis()` bookkeeping.

## Disabling a test

```java
@Test
@Disabled("Flaky pending investigation — JIRA-1234")
void disabledExample() { ... }
```

Always include a reason. A disabled test with no explanation and no
tracking ticket tends to stay disabled forever — treat `@Disabled` as a
temporary, tracked state, not a way to silence a test you don't want to
fix.

## Next

→ [4. Mockito basics: your first mock](04-mockito-basics.md)
