# Java Unit Testing Guide

A beginner-to-advanced, hands-on guide to writing Java unit tests with
**JUnit 5** and **Mockito** — covering assertions, test structure, every
common style of mocking, verification, spies, and the git/PR workflow for
shipping test changes.

Every concept in the docs has a matching, runnable test class in
[`src/test/java`](src/test/java/com/example/guide) — read the doc, then open
the file it points to and run it yourself.

## Quick start

```bash
git clone <this-repo>
cd java-unit-testing-guide
mvn test
```

Requires JDK 17+ and Maven. All 48 example tests should pass (one is
intentionally `@Disabled` to demonstrate the annotation).

## How to use this guide

Read in order if you're new to Java testing; jump directly to a chapter if
you already know the basics and want a specific technique.

| # | Guide | What you'll learn | Example test class |
|---|-------|--------------------|---------------------|
| 1 | [Getting started with JUnit 5](docs/01-beginner-junit5.md) | `@Test`, lifecycle hooks, `@Nested`, `@DisplayName`, running tests | [`CalculatorTest`](src/test/java/com/example/guide/beginner/CalculatorTest.java) |
| 2 | [Assertions with AssertJ](docs/02-assertions-assertj.md) | Fluent assertions, exceptions, collections | [`CalculatorTest`](src/test/java/com/example/guide/beginner/CalculatorTest.java) |
| 3 | [Parameterized & advanced JUnit 5](docs/03-junit5-parameterized.md) | `@ParameterizedTest`, tags, assumptions, timeouts | [`JUnit5FeaturesTest`](src/test/java/com/example/guide/beginner/JUnit5FeaturesTest.java) |
| 4 | [Mockito basics: your first mock](docs/04-mockito-basics.md) | Why mock, `@Mock`, stubbing with `when()` | [`UserServiceMockitoBasicsTest`](src/test/java/com/example/guide/mockito/UserServiceMockitoBasicsTest.java) |
| 5 | [Different ways of mocking](docs/05-mocking-styles.md) | `Mockito.mock()`, `@Mock`, `@InjectMocks`, deep stubs, lenient mode | [`MockingStylesTest`](src/test/java/com/example/guide/mockito/MockingStylesTest.java) |
| 6 | [Verification](docs/06-verification.md) | `verify()`, `times()`, `InOrder`, `ArgumentCaptor`, `verifyNoInteractions` | [`VerificationTest`](src/test/java/com/example/guide/mockito/VerificationTest.java) |
| 7 | [Spies (partial mocking)](docs/07-spies.md) | `@Spy`, `doReturn().when()`, mock vs. spy | [`SpyTest`](src/test/java/com/example/guide/mockito/SpyTest.java) |
| 8 | [Advanced Mockito](docs/08-advanced-mockito.md) | Exceptions, consecutive returns, custom `Answer`, BDD style, static mocking | [`AdvancedMockingTest`](src/test/java/com/example/guide/advanced/AdvancedMockingTest.java) |
| 9 | [Best practices & anti-patterns](docs/09-best-practices.md) | FIRST principles, what not to mock, naming, coverage traps | — |
| 10 | [Git workflow for test changes](docs/10-git-workflow.md) | Branch-per-change, commit hygiene, running tests locally | — |
| 11 | [Pull request guide](docs/11-pr-guide.md) | PR checklist, review etiquette, CI expectations | — |

## Project structure

```
src/main/java/com/example/guide/
  util/    Calculator, StringUtils          — dependency-free, for beginner tests
  user/    User, UserRepository, UserService — single-dependency, for basic mocking
  order/   Order + 4 collaborators, OrderService — multi-collaborator, for verification/spy/InOrder

src/test/java/com/example/guide/
  beginner/  Plain JUnit 5, no mocking
  mockito/   Mocking styles, verification, spies
  advanced/  Exceptions, static mocking, BDD style

docs/        The numbered guide chapters above
```

## Running a single test class

```bash
mvn -Dtest=VerificationTest test
```

## Running tests by tag

```bash
mvn -Dgroups=fast test
```
