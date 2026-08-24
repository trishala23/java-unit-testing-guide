# 6. Verification

Companion code: [`VerificationTest.java`](../src/test/java/com/example/guide/mockito/VerificationTest.java)

Stubbing (chapter 4) controls what a mock *returns*. Verification checks
what a mock was *called with* — essential when the method under test
returns nothing useful to assert on, but has an important side effect
(charge a card, send an email, save a record).

The example class for this chapter, `OrderService`, has four
collaborators (`InventoryService`, `PaymentGateway`, `OrderRepository`,
`NotificationService`) orchestrated in sequence — a realistic shape for
verification-heavy tests.

## The basic call: `verify(mock).method(args)`

```java
verify(paymentGateway).charge("buyer@example.com", new BigDecimal("49.99"));
```

Fails the test if `paymentGateway.charge(...)` was never called with
exactly those arguments. Equivalent to `verify(mock, times(1)).method(...)`.

## Call counts

```java
verify(orderRepository, times(2)).save(any(Order.class));
verify(paymentGateway, never()).charge(anyString(), any(BigDecimal.class));
verify(inventoryService, atLeastOnce()).reserveStock(anyString(), anyInt());
verify(inventoryService, atLeast(1)).reserveStock(anyString(), anyInt());
verify(inventoryService, atMost(5)).reserveStock(anyString(), anyInt());
```

`never()` is the most valuable of these in practice: it proves a side
effect that *must not* happen, didn't — e.g. payment must never be
attempted if stock reservation already failed.

## Argument matchers

Use matchers (`any()`, `anyString()`, `anyInt()`, `eq()`) instead of
literal values when the exact value doesn't matter, or to mix "don't
care" arguments with ones you do care about:

```java
verify(paymentGateway).charge(eq("buyer@example.com"), any(BigDecimal.class));
```

**Rule: if you use a matcher for one argument in a call, you must use
matchers for *all* arguments in that call** (mix `eq(...)` in for the
literals) — Mockito throws `InvalidUseOfMatchersException` otherwise.

For custom conditions, `argThat`:

```java
verify(paymentGateway).charge(anyString(),
        argThat(amount -> amount.compareTo(new BigDecimal("49.99")) == 0));
```

## Order matters: `InOrder`

Plain `verify()` calls don't check relative order. When the sequence
itself is the behavior under test:

```java
InOrder inOrder = inOrder(inventoryService, paymentGateway, orderRepository, notificationService);
inOrder.verify(inventoryService).reserveStock(anyString(), anyInt());
inOrder.verify(paymentGateway).charge(anyString(), any(BigDecimal.class));
inOrder.verify(orderRepository).save(any(Order.class));
inOrder.verify(notificationService).sendOrderConfirmation(anyString(), anyString());
```

This confirms inventory is reserved *before* payment is charged, which is
charged *before* the order is saved, etc. — catching bugs where the right
calls happen but in the wrong sequence (e.g. notifying the customer before
the payment actually succeeds).

## Capturing arguments for deeper assertions: `ArgumentCaptor`

Sometimes a matcher isn't enough — you want to inspect the actual object
passed to the mock:

```java
@Captor
private ArgumentCaptor<Order> orderCaptor;

...

verify(orderRepository).save(orderCaptor.capture());
Order captured = orderCaptor.getValue();
assertThat(captured.getStatus()).isEqualTo(Order.Status.PAID);
assertThat(captured.getId()).isNotBlank();
```

`@Captor` (paired with `@ExtendWith(MockitoExtension.class)`) is
equivalent to `ArgumentCaptor.forClass(Order.class)` — use whichever reads
better in context. Reach for a captor when you need to assert on multiple
fields of a passed object, something plain matchers can't express well.

## Proving nothing unexpected happened

```java
verifyNoInteractions(paymentGateway); // mock was never called AT ALL

verify(inventoryService).reserveStock("SKU-1", 2);
verifyNoMoreInteractions(inventoryService); // no calls beyond the ones already verified
```

`verifyNoMoreInteractions` is powerful but brittle if overused — it fails
if *any* future change adds a legitimate new call to that mock, even an
unrelated one. Use it selectively, on mocks where "nothing else should
happen" is actually part of the contract you're testing (like the
payment gateway in a failed-stock-reservation scenario), not as a blanket
habit on every mock in every test.

## Verification vs. stubbing — the same object, two different phases

```java
when(inventoryService.reserveStock("SKU-1", 2)).thenReturn(true); // ARRANGE
orderService.placeOrder(order);                                    // ACT
verify(paymentGateway).charge(...);                                 // ASSERT (via verify)
```

Verification is a form of assertion — it belongs in the "Assert" section
of the Arrange-Act-Assert structure, after the action, alongside (or
instead of) `assertThat(...)`.

## Next

→ [7. Spies (partial mocking)](07-spies.md)
