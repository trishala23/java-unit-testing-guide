# 4. Mockito basics: your first mock

Companion code: [`UserServiceMockitoBasicsTest.java`](../src/test/java/com/example/guide/mockito/UserServiceMockitoBasicsTest.java)

## Why mock at all?

Look at the class under test:

```java
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) { ... }

    public User getUserById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No user with id " + id));
    }
}
```

`UserRepository` is an interface — in a real app it's backed by a
database. To unit-test `UserService`'s *logic* (does it throw the right
exception when the user is missing? does it delegate correctly?) we don't
want to stand up a real database. A **mock** is a fake `UserRepository`
that does nothing on its own except what we tell it to.

**The class under test stays real.** Only its collaborators are mocked:

```java
userRepository = mock; // fake
userService = new UserService(userRepository); // REAL UserService, wired to the fake
```

Never mock the class you're actually testing — there'd be nothing left to
test.

## Your first mock

```java
@ExtendWith(MockitoExtension.class)
class UserServiceMockitoBasicsTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void getUserByIdReturnsUser() {
        User alice = new User(1L, "Alice", "alice@example.com", true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));

        User result = userService.getUserById(1L);

        assertThat(result).isEqualTo(alice);
    }
}
```

- **`@ExtendWith(MockitoExtension.class)`** wires Mockito into JUnit 5: it
  initializes every `@Mock` field before each test, and — importantly —
  enables *strict stubbing*, which fails a test that stubs something it
  never actually calls (catches stale/copy-pasted stubs).
- **`@Mock`** creates the fake. Every method on it returns a safe default
  (`null`, `0`, `false`, an empty `Optional`/`List`/`Map`...) until you
  stub it.
- **`when(mock.method(args)).thenReturn(value)`** is *stubbing*: "when
  this exact call happens, return this value." This is the core Mockito
  vocabulary — everything else in this guide builds on it.

## Stubbing exceptions and defaults

```java
when(userRepository.findById(99L)).thenReturn(Optional.empty());
// getUserById(99L) will now hit the .orElseThrow(...) branch
```

An un-stubbed call is not an error — it just returns Mockito's default for
that return type:

```java
// No when(...) set up for findAll() — Mockito returns an empty list,
// not null, because List is its known default for that return type.
assertThat(userRepository.findAll()).isEmpty();
```

## Proving a collaborator was never touched

```java
@Test
void registerUserRejectsInvalidEmail() {
    assertThatThrownBy(() -> userService.registerUser("Bad", "not-an-email"))
            .isInstanceOf(IllegalArgumentException.class);

    verifyNoInteractions(userRepository);
}
```

`verifyNoInteractions` is your first taste of **verification** — checking
what happened to a mock, not just what it returned. Chapter 6 goes deep on
this.

## Next

→ [5. Different ways of mocking](05-mocking-styles.md)
