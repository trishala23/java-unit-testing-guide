# 7. Spies (partial mocking)

Companion code: [`SpyTest.java`](../src/test/java/com/example/guide/mockito/SpyTest.java)

## Mock vs. spy

| | Mock | Spy |
|---|---|---|
| Backed by | nothing — starts empty | a **real object instance** |
| Unstubbed method call | returns a default (`null`/`0`/empty) | calls the **real** method |
| Typical use | isolate the class under test from a collaborator entirely | fake one or two methods on an otherwise-real object |

```java
@Spy
private Calculator calculatorSpy; // Mockito instantiates Calculator() and wraps it

@Test
void unstubbedMethodsCallRealCode() {
    int result = calculatorSpy.add(2, 3); // REALLY executes Calculator.add()

    assertThat(result).isEqualTo(5);
    verify(calculatorSpy).add(2, 3); // you can still verify calls on a spy
}
```

Everything from chapters 4-6 (stubbing, verification, matchers) works the
same way on a spy as on a mock. The only difference is what happens when
you *don't* stub something.

## Overriding just one method

```java
doReturn(999).when(calculatorSpy).multiply(anyInt(), anyInt());

assertThat(calculatorSpy.multiply(2, 3)).isEqualTo(999); // faked
assertThat(calculatorSpy.add(2, 3)).isEqualTo(5);        // still real
```

## The #1 gotcha: use `doReturn().when()`, not `when().thenReturn()`

```java
// WRONG on a spy — actually calls calculatorSpy.divide(10, 0) FIRST to
// set up the stub, which throws ArithmeticException before the stub is
// ever registered.
when(calculatorSpy.divide(10, 0)).thenReturn(-1.0);   // <-- throws!

// RIGHT — never invokes the real method at all.
doReturn(-1.0).when(calculatorSpy).divide(10, 0);
```

`when(mock.method(...))` works by first *calling* `mock.method(...)` so
Mockito can record what was called and attach the stub to it. On a plain
mock that call is a no-op (it just returns a default). On a **spy**, that
call runs the **real method** — which is a problem the moment the real
method has a side effect, throws, or is slow. `doReturn(...).when(spy)...`
avoids the real call entirely by using a different mechanism to attach the
stub, and is the required style whenever the real method might misbehave.

The same `do*()` family exists for other stub types: `doThrow()`,
`doAnswer()`, `doNothing()` (for void methods) — all avoid invoking the
real method up front and all work on both mocks and spies.

## Spying a real, stateful object

```java
@Spy
private List<String> listSpy = new ArrayList<>();

@Test
void spyingARealCollection() {
    listSpy.add("a");
    listSpy.add("b");

    assertThat(listSpy).containsExactly("a", "b"); // real ArrayList behavior
    verify(listSpy, times(2)).add(anyString());
}
```

Note the field is initialized with a real instance (`new ArrayList<>()`)
— `@Spy` wraps *that* instance, rather than constructing one itself the
way `@Mock` does. You can do the same manually with `Mockito.spy(existingInstance)`.

## When to reach for a spy — and when not to

Use a spy when you want an object's real behavior almost entirely, and
need to fake just one collaborating call inside it (e.g. skip a slow
external call inside an otherwise-real service, in a test that's
specifically about the rest of that service's logic).

**Prefer a plain mock whenever possible.** Spies make two mistakes easy:

1. Accidentally invoking real logic (including I/O) because you forgot to
   stub a method, or stubbed it the wrong way (see the gotcha above).
2. Testing against an implementation you don't fully control the behavior
   of, making the test's outcome depend on code paths you didn't set up
   explicitly — the opposite of the isolation unit tests are meant to give
   you.

If you find yourself reaching for a spy to work around a class that's hard
to construct or has too many responsibilities, that's often a sign the
class itself should be split up rather than worked around.

## Next

→ [8. Advanced Mockito](08-advanced-mockito.md)
