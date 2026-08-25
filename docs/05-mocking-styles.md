# 5. Different ways of mocking

Companion code: [`MockingStylesTest.java`](../src/test/java/com/example/guide/mockito/MockingStylesTest.java)

Mockito offers several ways to create and wire a mock. They're all the
same underlying mechanism — pick the one that fits the context.

## Style 1 — `Mockito.mock(Class)`: manual, framework-agnostic

```java
UserRepository repository = Mockito.mock(UserRepository.class);
when(repository.findById(1L)).thenReturn(Optional.of(alice));

UserService service = new UserService(repository);
```

No annotations, no test-runner extension required. Works in plain `main`
methods, static initializers, or any test framework. This is what `@Mock`
does under the hood.

## Style 2 — `@Mock` + `MockitoExtension`: declarative

```java
@ExtendWith(MockitoExtension.class)
class SomeTest {
    @Mock
    private UserRepository userRepository;
    ...
}
```

The most common style in modern JUnit 5 codebases. Less boilerplate,
consistent mock lifecycle (a fresh mock per test method), and enables
strict-stubbing checks for free.

## Style 3 — `@InjectMocks`: auto-wiring

```java
@Mock private UserRepository userRepository;
@InjectMocks private UserService userService; // Mockito constructs UserService
                                               // and injects the @Mock fields
```

Mockito finds a constructor (or setters, or fields, in that priority
order) on `UserService` and wires the mocks in automatically. Convenient,
but implicit: it can silently stop injecting a field if the constructor
signature changes shape, and it hides *how* the object is built from the
reader. Many teams prefer explicit construction in `@BeforeEach` (Style 2)
specifically because `new UserService(userRepository)` is unambiguous and
refactor-safe — reach for `@InjectMocks` when a class has many
dependencies and the constructor wiring is genuinely repetitive.

## Style 4 — mocks with a custom default `Answer`

```java
Clock clock = mock(Clock.class, RETURNS_DEEP_STUBS);
```

`RETURNS_DEEP_STUBS` lets you stub a chained call (`a.b().c()`) without
stubbing every link. Convenient for legacy chains you don't control, but
treat it as a smell in your own code — it usually means the collaborator's
interface should expose the thing you actually need directly instead of
forcing callers to walk an object graph.

## Style 5 — lenient stubbing

By default, `MockitoExtension` is **strict**: a stub you set up but never
call in that test fails the test (`UnnecessaryStubbingException`). This
catches copy-pasted or stale stubs. Opt a specific stub, or a whole test
class, out when the strictness is a false positive (e.g. a shared
`@BeforeEach` stub that only some test methods actually exercise):

```java
lenient().when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
```

or at the class level:

```java
@MockitoSettings(strictness = Strictness.LENIENT)
```

Prefer fixing the test (move the stub into the test that needs it) over
reaching for `LENIENT` — it's an escape hatch, not a default.

## Style 6 — mocking concrete classes

Mockito doesn't require an interface — it can mock a concrete class too
(it subclasses it at runtime via byte-buddy):

```java
Clock fixedClock = mock(Clock.class); // Clock is an abstract class, not an interface
```

Since Mockito 5, the **inline mock maker** (needed for `final` classes,
`final` methods, and static methods) is the default — no extra
`mockito-inline` dependency is needed, unlike in Mockito 4 and earlier.
See [8. Advanced Mockito](08-advanced-mockito.md) for static mocking.

## Which style should I use?

| Situation | Style |
|---|---|
| Everyday unit test in a JUnit 5 codebase | `@Mock` + `MockitoExtension` (2) |
| Outside a test framework, or a quick one-off | `Mockito.mock()` (1) |
| Many constructor dependencies, tedious wiring | `@InjectMocks` (3) — but prefer explicit wiring when the constructor is small |
| A stub only some tests in the class use | `lenient()` on that one stub (5), not class-wide |

## Next

→ [6. Verification](06-verification.md)
