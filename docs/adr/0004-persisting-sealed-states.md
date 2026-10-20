# 0004. Persist policy states as columns and claim states as an event table

- **Status:** Accepted
- **Date:** 2026-10-20

## Context

The sealed states of [0003](0003-sealed-state-machines-instead-of-enums.md) do
not map onto a relational table, where every row has the same columns. Three
shapes were on the table:

- **Columns for every possible value**, most of them null most of the time.
- **A status column plus a JSON blob** holding whatever that state needs.
- **A separate table with one row per transition.**

The two aggregates are not in the same situation. A policy has four states and
almost no state-specific data: a cancellation date and a reason. A claim has
five, carries money, and its detail screen has to show a timeline of what
happened and when, which is a requirement of the user interface and not a
nicety: an insurer is expected to be able to say who approved what, and when.

## Decision

We will use a different shape for each aggregate, chosen on its own merits.

**Policies** keep their state in the `policies` table: a `status` column plus
`cancelled_on` and `cancellation_reason`, guarded by a check constraint so a
policy cannot be cancelled without a date and a reason.

**Claims** keep a `claims` table for the facts that never change (policy,
incident date, amount claimed, the insured sum and excess copied in at opening)
and a `claim_events` table with one row per transition, carrying only the data
that transition produced. The `claims` table also holds a denormalised
`current_status` column so the list screen can filter without reconstructing
every claim from its events.

## Alternatives considered

- **The same shape for both,** for consistency. Rejected: it would either force
  an event table onto an aggregate that does not need one, or deny the claim the
  timeline its screen already requires.
- **JSON for the state data.** Rejected: it trades the database's own type
  checking and constraints for a flexibility this domain does not need, and it
  makes an ordinary question like "how much have we approved this month"
  unnecessarily awkward. In an insurance domain the data is the product.
- **Deriving the claim status from its events instead of storing it.** Purer,
  and tempting. Rejected because every list query would then have to rebuild
  state for every row. The denormalisation is deliberate and is written down
  here so that nobody later mistakes it for an oversight.

## Consequences

- Two mapping styles in one codebase. This is a decision, not an inconsistency,
  and the reason is written here.
- `current_status` can drift from the events if a transition is ever written
  outside the repository. Both are written in the same transaction, from one
  place, and an integration test asserts they agree.
- The claim timeline comes for free, and so does an audit trail.
- Adding a claim state means a migration for the new event type, not a new
  column on a wide table.
