# 8. Advanced Mockito

Companion code: [`AdvancedMockingTest.java`](../src/test/java/com/example/guide/advanced/AdvancedMockingTest.java)

## Making a mock throw

```java
when(inventoryService.reserveStock(anyString(), anyInt()))
        .thenThrow(new IllegalStateException("Inventory service unavailable"));
```

For **void** methods, `when(...).thenThrow(...)` doesn't compile (there's
no return value to attach the stub to via `when`) — use `doThrow(...).when(mock)...`:

```java
doThrow(new RuntimeException("DB write failed")).when(userRepository).save(any(User.class));
```

This is the same `do*()` family from chapter 7's spy gotcha — it's not
spy-specific, just required for void methods on both mocks and spies.

## Consecutive return values

```java
when(inventoryService.reserveStock(anyString(), anyInt()))
        .thenReturn(true)   // 1st call
        .thenReturn(false); // 2nd call, and every call after that
```

Useful for simulating "first attempt fails, retry succeeds" or "flaky
then stable" scenarios without hand-rolling a stateful fake.

## Custom `Answer`: compute the result from the actual arguments

```java
when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
    Order order = invocation.getArgument(0);
    order.setId("generated-" + order.getSku());
    return order;
});
```

Reach for `thenAnswer` when the return value legitimately depends on what
was passed in — most commonly, a "save" stub that just echoes back
whatever it was given (`inv -> inv.getArgument(0)`), which you'll see
throughout this project's tests wherever `orderRepository.save(...)` is
stubbed.

## BDD-style API

Same Mockito engine, different vocabulary — some teams prefer this because
it mirrors Given-When-Then test structure directly:

```java
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

// given
given(inventoryService.reserveStock("SKU-2", 1)).willReturn(true);

// when
orderService.placeOrder(order);

// then
then(notificationService).should().sendOrderConfirmation(eq("bdd@example.com"), anyString());
```

`given(...).willReturn(...)` ≡ `when(...).thenReturn(...)`.
`then(mock).should()` ≡ `verify(mock)`. Pick one style per codebase and
stay consistent — mixing both in the same file hurts readability more
than either style costs on its own.

## Mocking static methods

Static methods (`Instant.now()`, legacy utility classes) can't be mocked
by wrapping an instance — Mockito instead intercepts the class itself for
a scoped block:

```java
try (MockedStatic<Instant> mockedInstant = mockStatic(Instant.class)) {
    mockedInstant.when(Instant::now).thenReturn(fixed);

    assertThat(Instant.now()).isEqualTo(fixed);
}
// Outside the try-with-resources block, Instant.now() behaves normally again.
```

`mockStatic` must be used inside try-with-resources (or manually closed) —
the mock only applies for the block's duration and **only on the thread
that opened it**. Since Mockito 5, this works out of the box via
`mockito-core`'s default inline mock maker; no extra dependency needed.

Treat static mocking as a last resort — it usually means the code
under test calls a static method it should instead receive as an injected
collaborator (e.g. a `Clock` parameter instead of `Instant.now()`
directly). Reach for it on legacy code you can't easily refactor yet, not
as a first choice in new code.

## Summary: `when()` vs `do*()`

| | Non-void methods | Void methods | On a spy (real method has side effects) |
|---|---|---|---|
| Return a value | `when(m).thenReturn(v)` | n/a | `doReturn(v).when(m)` |
| Throw | `when(m).thenThrow(e)` (works, but calls the stubbed method first) | `doThrow(e).when(m)` | `doThrow(e).when(m)` |
| Custom logic | `when(m).thenAnswer(a)` | `doAnswer(a).when(m)` | `doAnswer(a).when(m)` |
| Do nothing | n/a | `doNothing().when(m)` (rarely needed — it's the default) | `doNothing().when(m)` |

Rule of thumb: **on a mock**, `when().thenX()` is idiomatic and fine for
almost everything except void methods. **On a spy**, default to the
`do*().when()` family to avoid ever invoking real code as a side effect of
stubbing.

## Next

→ [9. Best practices & anti-patterns](09-best-practices.md)
