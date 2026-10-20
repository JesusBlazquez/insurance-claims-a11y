# Accessibility findings

Every accessibility problem found while building this application, and what was done about it.

**How this log is produced.** The interface is built against the WCAG 2.2 AA checklist from the
first component, and every screen is audited with axe DevTools before moving on to the next. This
log is the real output of that process: things still slip through, and that is exactly what is
recorded here. Nothing was broken on purpose to make the list longer.

| # | Screen | WCAG criterion | Severity | What was wrong | Fix | Commit |
|---|---|---|---|---|---|---|
| _(the first entries arrive with phase 3)_ | | | | | | |

## Audits

Reports live in `reports/`, named `YYYY-MM-DD-<screen>.html`. Manual keyboard and screen reader
runs are written up in `manual/`.
