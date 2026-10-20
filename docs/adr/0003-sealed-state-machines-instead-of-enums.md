# 0003. Model policy and claim states as sealed hierarchies, not enums

- **Status:** Accepted
- **Date:** 2026-10-20

## Context

A policy moves through draft, active, cancelled and expired. A claim moves
through open, under review, approved, rejected and paid. Some of those states
carry data that only exists in that state: an approved claim has an amount, a
rejected one has a reason, a cancelled policy has a cancellation date.

With a plain enum, that data has to live on the aggregate as fields that are
null most of the time, and nothing stops code from reading an approved amount
from a rejected claim.

## Decision

We will model both state machines as sealed interfaces whose implementations are
records, each carrying exactly the data its state needs. Transitions are methods
on the aggregate that reject any move the business does not allow, and code that
inspects state uses an exhaustive `switch` with no `default` branch.

## Alternatives considered

- **Enum plus nullable fields on the aggregate:** the usual approach. Cheaper to
  persist, but it pushes the invariants out of the type system and into code
  review.
- **A state field plus a map of attributes:** flexible and unchecked, which is
  the worst combination for money.

## Consequences

- Adding a state is a compile error everywhere it matters, instead of a runtime
  surprise. The compiler takes over work that would otherwise fall on tests.
- Persistence is harder, because a database row has fixed columns and these
  states do not. That cost is paid deliberately and is the subject of
  [0004](0004-persisting-sealed-states.md).
- Reading a state's data requires a pattern match rather than a getter, which is
  unfamiliar to anyone who has not used Java 21 features.
