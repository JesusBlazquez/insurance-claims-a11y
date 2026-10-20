# 0002. One repository with a backend and a frontend section

- **Status:** Accepted
- **Date:** 2026-10-20

## Context

The project is a Spring Boot API and an Angular application that exist only to
serve each other. They are released together, they are demonstrated together,
and a reader who clones one without the other has nothing to run.

The project is also a portfolio piece: whoever opens it should be able to start
the whole thing from a single README.

## Decision

We will keep both halves in one repository, under `backend/` and `frontend/`,
with the Maven wrapper inside `backend/` and the shared material (README, ADRs,
accessibility reports, CI) at the root. The CI workflow runs one job per half.

## Alternatives considered

- **Two repositories:** the honest option when the two halves are deployed and
  versioned independently, which is not the case here. It would force a reader
  to find both, and it would make "the API changed, so the client changed"
  impossible to see in a single commit.
- **Frontend inside the backend jar (static resources):** simpler to deploy, but
  it buries the Angular project inside a Maven build and makes the accessibility
  tooling awkward to run.

## Consequences

- The build is invoked from `backend/`, not from the repository root. The
  verification script is called with `-Proyecto insurance-claims-a11y\backend`.
- Dependabot needs to be told where each ecosystem lives (`/backend` for Maven).
  This was found the hard way: the first run failed looking for a `pom.xml` at
  the root.
- A single CI workflow grows a job per half rather than two workflows drifting
  apart.
