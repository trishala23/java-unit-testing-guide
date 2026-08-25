# 9. Best practices & anti-patterns

## FIRST principles

Good unit tests are:

- **F**ast — milliseconds, not seconds. No sleeping, no real network/DB/filesystem.
- **I**ndependent — any test can run alone, or in any order, and pass.
  Never rely on another test's leftover state.
- **R**epeatable — same result every run, on any machine. No dependency on
  the current date/time, random values, or external services unless
  those are explicitly controlled (see `MockedStatic<Instant>` in chapter 8).
- **S**elf-validating — the test itself says pass/fail. No "check the log
  output manually."
- **T**imely — written close to (ideally alongside, or before, per TDD)
  the code it tests, not bolted on weeks later.

## What to mock — and what not to

**Mock:**
- Interfaces/classes representing I/O boundaries: repositories, HTTP
  clients, payment gateways, email/notification senders, file systems.
- Slow or non-deterministic collaborators (clocks, random generators,
  external services).

**Don't mock:**
- The class under test itself. If you're mocking it, you're not testing it.
- Simple value objects / data classes (`Order`, `User` in this project) —
  just construct real instances.
- Types you don't own when a real, fast, in-memory alternative exists
  (e.g. use a real `ArrayList` instead of mocking `List`).
- Everything, reflexively. A test that mocks every single collaborator of
  a large object graph is usually testing wiring, not behavior — consider
  whether an integration test would answer the real question better.

## Test one thing

Each test should verify one behavior. If a test's name needs "and" to
describe what it checks (`savesUserAndSendsEmailAndLogsAudit`), it's
probably three tests wearing a trenchcoat. Split them — failures become
immediately diagnosable, and each test stays meaningful in isolation when
the others are deleted or changed.

## Don't assert on implementation details

Verify *outcomes* (return values, side effects on collaborators that
matter) rather than *how* the code achieves them internally. Over-eager
`verifyNoMoreInteractions` on every mock, or asserting on private helper
call counts, makes tests brittle — they break on harmless refactors that
don't change observable behavior. Reserve strict "nothing else happened"
verification for the mocks where that's actually part of the contract
(see chapter 6's `verifyNoInteractions(paymentGateway)` example — the
domain has a real invariant there: no charge without reserved stock).

## Strict stubbing is a feature, not friction

Mockito's default strict-stubbing mode (via `MockitoExtension`) fails
tests with unused stubs. When you hit `UnnecessaryStubbingException`,
resist reaching straight for `lenient()` — first ask whether the stub is
actually dead code (delete it) or belongs in a different test (move it).
`lenient()` is for the genuine remaining cases: a shared `@BeforeEach`
stub not every test method exercises.

## Avoid over-specifying with matchers

`verify(mock).method(any(), any(), any())` on every argument, everywhere,
loses the value of the assertion — it degrades into "was this method
called at all," which `verify(mock, times(1)).method(any(), any(), any())`
says more honestly. Use exact/`eq()` values wherever the value actually
matters to the behavior you're testing; reserve `any()` for arguments that
are genuinely irrelevant to this particular test.

## Coverage is a signal, not a goal

100% line coverage with weak assertions (`assertThat(result).isNotNull()`
on everything) is worse than 80% coverage with tests that actually pin
down behavior. Coverage tools tell you what's *never run*; they say
nothing about whether what runs is checked meaningfully.

## Keep test data realistic but minimal

Use the smallest input that exercises the behavior — a `BigDecimal` of
`"49.99"` and a `quantity` of `2`, not a giant realistic-looking fixture
with a dozen irrelevant fields. Minimal input keeps the reader's attention
on what matters to *this* test.

## Next

→ [10. Git workflow for test changes](10-git-workflow.md)
