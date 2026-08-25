# 11. Pull request guide

## Before opening the PR

- [ ] The branch was created off an up-to-date `main`, and contains **only**
      the one change it's meant to (see [10. Git workflow](10-git-workflow.md)).
- [ ] `mvn test` passes locally, with no skipped-except-intentional or
      newly-flaky tests.
- [ ] New/changed test methods have clear, behavior-describing names and
      (where non-obvious) a `@DisplayName`.
- [ ] Each new test follows Arrange-Act-Assert and checks one behavior.
- [ ] No leftover debug output (`System.out.println` used only
      deliberately, e.g. lifecycle demos like in `JUnit5FeaturesTest`),
      no commented-out code, no stray TODOs without a linked issue.
- [ ] If you added a new mocking pattern not already covered by the docs,
      consider whether it belongs in one of the `docs/*.md` chapters too —
      keep the guide and the code in sync.

## Writing the PR description

State **what** changed and **why**, not just what — the diff already
shows what. For a test-focused PR specifically:

```markdown
## Summary
Adds ArgumentCaptor coverage for OrderService.placeOrder, asserting the
saved Order has status=PAID and a generated id — previously only the
return value's presence was checked, not its actual field values.

## Why
Caught a real bug in review where `order.setStatus` was called after
`orderRepository.save`, so persisted orders were saved with stale status.
This test would have failed before the fix.

## Test plan
- `mvn -Dtest=VerificationTest test` — all pass
- Confirmed the test fails if `setStatus` is moved back after `save`
  (temporarily reverted the fix locally to check)
```

That last point — **prove the test can fail** — is one of the most
valuable things you can do before asking for review. A test that can't
fail (e.g. it would pass even against buggy code) isn't verifying
anything. Temporarily break the production code, watch the test go red,
then put the fix back.

## Review etiquette

**As an author:**
- Respond to every comment — even "done" or "good catch, fixed in
  <commit>" — so reviewers can tell what's addressed vs. still open.
- Push follow-up commits rather than force-pushing over history mid-review,
  so reviewers can see what changed since their last pass. Squash/clean up
  right before merge if your team's convention wants a tidy history.
- Don't take "why does this test do X" as criticism — it's often a sign
  the test needs a clearer name, a comment on a non-obvious assertion, or
  actually does have a bug.

**As a reviewer, for test PRs specifically, check:**
- Does the test actually test the stated behavior, or just that *a* call
  happened (over-use of `any()` everywhere, see chapter 9)?
- Is the assertion strict enough to have caught the bug it's meant to
  guard against?
- Mock vs. spy chosen correctly (chapter 7) — is a spy used where a plain
  mock would be simpler and safer?
- `verify()` used for real behavioral checks, not just to pad the test
  (chapter 6 — is `verifyNoMoreInteractions` earning its brittleness here?).
- Would this test survive a harmless refactor, or does it assert on
  internal implementation details that could change without changing
  behavior?

## CI expectations

- All tests must pass — a red CI run blocks merge, full stop. Don't merge
  with `--no-verify` or by disabling a failing test to get green.
- If CI fails and the failure looks unrelated to your change, verify that
  by re-running the specific failing test locally on your branch and on
  `main` — don't assume "it's just flaky" without checking.
- Keep the PR's CI run fast: this is exactly why the FIRST principles
  (chapter 9) matter operationally, not just philosophically — slow test
  suites make every PR slower to land.

## Merging

- Prefer squash-merge for PRs with many small "fix typo" / "address
  review comment" commits, so `main`'s history stays one meaningful commit
  per change — but follow whatever convention your repository already
  uses.
- Delete the branch after merge. It already did its one job (see the
  golden rule in chapter 10) — the next change gets a fresh branch, not
  more commits on this one.

## Back to the start

← [README](../README.md)
