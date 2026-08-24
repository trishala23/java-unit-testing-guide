# 10. Git workflow for test changes

A workflow for contributing to this guide (or applying the same habits to
tests in any Java project you work on).

## Golden rule: one branch per change

**Always create a new branch for each distinct piece of work.** Never
accumulate unrelated changes on one branch, and never keep committing new,
unrelated work onto a branch that already has an open (or merged) PR.

Why this matters specifically for test suites:

- A reviewer can actually evaluate "does this test correctly verify the
  behavior it claims to" when the PR is scoped to one class/feature — not
  when it's buried in an unrelated 40-file diff.
- If a new test turns out to be flaky or wrong, `git revert` on a
  single-purpose branch cleanly removes just that change.
- CI failures are attributable. A red build on a branch that only touches
  `VerificationTest.java` tells you exactly where to look.

## Branch naming

Use a short, descriptive, kebab-case name with a type prefix:

```
test/add-verification-examples
test/fix-flaky-spy-test
docs/mocking-styles-chapter
fix/order-service-null-check
```

Pick a convention (`type/description` above, or `type-description`,
`initials/description`, whatever your team already uses) and apply it
consistently — the specific scheme matters less than not mixing several
schemes in one repo.

## Step by step

```bash
# 1. Start from an up-to-date main
git checkout main
git pull origin main

# 2. Create a new branch for THIS change only
git checkout -b test/add-argument-captor-example

# 3. Make the change, run tests locally before committing
mvn test

# 4. Stage and commit — see "commit hygiene" below
git add src/test/java/com/example/guide/mockito/VerificationTest.java
git commit -m "Add ArgumentCaptor example for verifying saved Order state"

# 5. Push and open a PR
git push -u origin test/add-argument-captor-example
```

When the next, unrelated change comes along — **start from `main` again**,
not from the branch you just pushed:

```bash
git checkout main
git pull origin main
git checkout -b test/add-spy-gotcha-example
```

## Commit hygiene

- **Run the full suite before every commit**: `mvn test`. A commit that
  breaks the build should never exist, even transiently — it makes
  `git bisect` and history review unreliable.
- **Write commit messages that explain *why*, not just *what***: the diff
  already shows what changed. `Add null check` is weaker than
  `Guard against null email in registerUser — repository lookup throws NPE
  otherwise`.
- **Keep commits scoped**: one logical change per commit is easier to
  review and revert than one giant commit mixing a new test file, a
  refactor, and a typo fix in the README.
- **Don't commit generated output**: `target/` is already in
  `.gitignore` — never force-add build artifacts.

## Keeping a branch up to date

If `main` moves while your branch is open, prefer merging `main` into your
branch (safe on a branch only you are working on, and doesn't rewrite
history other people may have already pulled):

```bash
git checkout test/add-spy-gotcha-example
git fetch origin
git merge origin/main
```

Avoid `rebase`/force-push on a branch anyone else has already checked out
or reviewed — it invalidates their local copy and their in-progress review
comments.

## Next

→ [11. Pull request guide](11-pr-guide.md)
