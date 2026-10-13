# Prompt log — JamunaConnect, Team 59

Every AI-assisted change below is logged against the commit it produced, with
the commit body recording the decision. Prompts are shown as used, trimmed of
pasted logs and code context. Standing context sent with every session: Spring
Boot 3.3 / Java 21 / JPA / PostgreSQL + `pg_trgm` / Flyway / Thymeleaf,
module boundaries from design-doc §2, "touch as few files as possible".
Earlier work used Claude Code, Copilot and Gemini; evaluation-window work
uses Gemini Pro only.

## 1. Scaffold — `892699c` (19 Sep, Dipayan)

> Scaffold a Spring Boot 3.3 Maven project, Java 21: MVC + Thymeleaf, Data
> JPA, Security, Validation, Actuator, Flyway, PostgreSQL driver, H2 for a
> local profile. Add a Dockerfile, `.dockerignore`, `.env.example`, and a
> GitHub Actions workflow that runs `mvn -B verify` against a PostgreSQL 16
> service container then packages the jar. Deliver the file list plus the
> exact local-run command. No feature code.

Result: project skeleton, CI green on an empty app, `local` profile documented
in README.

## 2. Auth, schema, repositories (D) — `03f2db5` (20 Sep, Saathvik)

> Respect these contracts, do not redesign them: none yet — you are creating
> them. Task: Spring Security with three levels (public pages open,
> OFFICE_STAFF triage + transitions, WARDEN read-only oversight), form login
> at /staff/login plus HTTP Basic for API clients. Six entities — Room (key:
> room number, block, floor), Resident, Contact, Complaint, complaint history,
> StaffUser with BCrypt hashes — as Flyway V1, with one repository interface
> per entity. Acceptance: anonymous GET of a staff URL redirects to login;
> public pages load freely. Deliver the minimal diff, the tests, and a 3-line
> note on what you rejected.

Result: `SecurityConfig`, six entities, V1 migration, repository interfaces
every later module builds on.

## 3. Complaints + contacts API (E) — `ab02aa5` (22 Sep, Saathvik)

> Task: filing, listing by status, patching status by id under
> /api/complaints, contacts under /api/contacts. You may touch only: new
> files under web/api, web/dto, service. Must use D's repositories, Bean
> Validation at the boundary (room-format pattern, required fields, length
> caps), per-IP rate limit, client-token idempotency, and the five-state
> machine OPEN → ACKNOWLEDGED → IN_PROGRESS → RESOLVED → CLOSED with illegal
> jumps rejected. Acceptance (design-doc §3): two identical submits make one
> record; bad room code is a 400; one patch appends exactly one ordered
> history row. Deliver the diff, the tests, and why this shape.

Result: `ComplaintsApiController`, DTOs, `ComplaintService` state machine,
`RateLimiter`, `ApiExceptionHandler` (400/404/409).

## 4. Notifications + escalation (F) — `a9a962b` (22 Sep, Saathvik)

> Task: async mail on complaint submit (Spring @Async — the resident must
> never wait on SMTP) plus a daily @Scheduled scan escalating complaints
> stuck in OPEN/ACKNOWLEDGED past 72h to Gensec/Warden. One shared
> notification service for both paths; threshold and cron from properties.
> Acceptance: inbox receives a filing as Open within a minute; a 72h-stale
> complaint escalates untouched; with mail disabled, filing and listing still
> succeed (mail is notification-only). Deliver the diff and the fallback
> behaviour in words.

Result: `MailNotificationService`, `EscalationScheduler`, in-app queue
fallback on `MailException`.

## 5. Public pages (A) — `fb68810` (24 Sep, Dipayan)

> Task: server-rendered Thymeleaf pages — contacts, room lookup, map,
> complaint form — sharing one layout fragment, no frontend build step.
> Category dropdown filled from the category enum via the model. Contact page
> must pick up a one-row office-bearer change with no code edit; blank and
> malformed submits refused with readable messages. Small static scripts may
> fetch the JSON API. Deliver templates + controllers only.

Result: `PublicPageController`, five templates, layout fragment, page scripts.

## 6. Fuzzy search + map (B) — `9fca593` (25 Sep, Dipayan)

> Task: one GET /api/rooms endpoint over pg_trgm used by BOTH the search box
> and the Leaflet/GeoJSON map popup, so both share one source of truth.
> Rank by trigram similarity of room number and occupants, top 25; plain
> substring fallback for H2 local runs behind a flag. Acceptance: misspelt
> names and wrong block prefixes still top the right row; a map click and a
> typed query for the same room agree exactly.

Result: `RoomRepository.searchFuzzy/searchLike`, `RoomSearchService` with the
grouped two-query residents fetch.

## 7. Staff dashboard (C) — `534c30c` (26 Sep, Dipayan)

> Task: filterable complaint list (status, category, age) with
> status-transition buttons, read-only Warden oversight view, resident-visible
> audit timeline per complaint. Acceptance: combined filters show only the
> matching slice; moving a complaint appends a timeline entry, never a silent
> overwrite.

Result: `StaffController`, dashboard template, filter/transition scripting.

## 8. Test suites — `ebe5df8` (27 Sep, Dipayan)

> Write failing-first tests for: idempotent submit, 400 on bad room, one
> ordered history row per patch, illegal jumps rejected, per-IP limits,
> login redirect vs API 401, trigram ranking on real PostgreSQL — never H2
> for SQL behaviour. Frameworks: JUnit 5 + Mockito for unit, MockMvc for API,
> Testcontainers for integration. One assertion idea per test, named after
> the criterion.

Result: six test classes; `mvn verify` green in CI.

## 9. Deploy files — `742892b` (28 Sep, Dipayan)

> Task: EC2 user-data script (Ubuntu 24.04, Java 21 JRE, PostgreSQL +
> pg_trgm, jamuna DB/user with generated password in /opt/jamuna/app.env
> mode 0600), systemd unit running /opt/jamuna/app.jar with restart, nginx
> proxy 80 → 127.0.0.1:8080 forwarding X-Forwarded-For. Secrets from
> environment only. Deliver the three files plus a README deploy section.

Result: `deploy/` with user-data, service unit, nginx conf, README.

## 10. Static frontend mirror — `17af985` (30 Sep, Saathvik)

> Task: plain static mirror of the public pages (index, contacts, lookup,
> map, complaint form + four scripts + GeoJSON) deployable to Vercel with NO
> build step, proxying /api to the AWS backend via vercel.json. Category
> dropdown hardcoded to the enum values. No frameworks.

Result: `frontend/` site live on Vercel.

## 11. CI auto-deploy — `283cc7d` (30 Sep, Saathvik)

> Task: extend ci.yml — after green build, deploy frontend/ to Vercel
> production and ship the jar to EC2 (S3 upload + SSM remote restart, wait
> for active). Backend deploy over SSM, no inbound SSH. Secrets only.

Result: `deploy-frontend` + `deploy-backend` jobs (later fixed in 13–14).

## 12. word_similarity fix — `668b947` (2 Oct, Saathvik)

> Symptom: searching "Arjn" for occupant "Arjun Nair" returns nothing.
> Suspect: RoomRepository trigram query. Do NOT rewrite the module. (1)
> State the single most likely cause with the measurement confirming it
> (a psql query). (2) Smallest fix as a diff. (3) The regression test that
> would have caught this on day one.

Result: diagnosis (similarity 0.23 vs cutoff 0.25), two-line
`word_similarity` fix (0.6), integration test on real names (`6049f80`).

## 13–14. Deploy fixes — `468e266`, `8bec6b7` (4 Oct, Saathvik)

> Symptom 1: Vercel production 404s though the workflow is green.
> Symptom 2: OIDC role assumption fails despite a correct-looking trust
> policy. For each: most likely cause, smallest fix, how to verify the
> artifact not the workflow.

Result: upload-the-directory-as-is deploy; scoped access-key CI user.

## 15. Real allocation data — `c80d3c8` (5 Oct, Dipayan)

> Task: Flyway V4 joining the hostel allotment spreadsheet (roll → name,
> Jamuna only) with the Phase-1 room map (room → roll): ~350 rooms and
> residents, retiring demo seeds. Add HHS/Tech/Sports/Manager contact roles
> plus the Elec Maintenance line from the hostel groups. Rewrite complaint
> categories to match how residents actually report (water, power, LAN,
> cleaning, maintenance, pests, lost & found) and link the CC LAN/router
> guides residents keep re-posting. New migration only — never edit old ones.

Result: V4 migration, `ComplaintCategory` rewrite, guide links.

## 16. Auth + dashboard + perf fixes — `e94aff5`, `ab1acc3`, `155d688`, `9ee51f1` (5 Oct, Dipayan)

> Symptom: every staff login dies / dashboard needs stat cards, modal
> timeline, stale highlighting / lookups feel slow / directory shows
> unverified numbers. Same debugging contract: cause with evidence, minimal
> diff, locking test. Constraints: rooms carry roll numbers in the existing
> grouped query (no new round trips), debounce + abort stale + cap cards,
> short public Cache-Control, heap capped for the 1GB box, unverified
> contact values become NULL (render `-'), never guesses.

Result: CSRF + entry-point fix with `StaffLoginTest`; dashboard polish;
grouped roll numbers, caching, Minty theme; contact cleanup (V5).
