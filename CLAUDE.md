# frilance-os-backend — engineering guide

Spring Boot modular monolith implementing the Frilance OS SRS
(`Frilance_OS_Vymogy_SRS.docx` — 24 FRs, 18 NFRs for a Ukrainian
freelancer/SMM-agency operating system). This file is the source of truth
for how to write code in this repo. Read it before adding a module,
endpoint, or entity — most of it exists because getting it wrong here is
expensive to unwind later (tenant isolation, migrations, module boundaries).

## Current state

Implemented: FR-01 (freelancer register/login) and the Overview dashboard
screen end-to-end. Every other SRS module has a package with a
`package-info.java` describing which FRs it owns and that it's unbuilt —
see `src/main/java/com/frilanceos/backend/{tasks,notifications,targets,
smmstats,ai,analytics,reports,onboarding}`. Do not invent functionality for
those beyond what's in the SRS; check the doc first.

## Architecture: modular monolith, package-by-feature

One deployable Spring Boot app. Modules are Java packages, not JARs or
services — `com.frilanceos.backend.<module>` for `clients`, `contentplan`,
`finance`, `team`, `ledger`, `attention`, `dashboard`, `auth`, `agency`, plus
the unbuilt ones above. `common/` holds cross-cutting infrastructure
(security, tenant context, exceptions) — nothing domain-specific belongs
there.

**Module boundary rule:** a module may depend on another module's
repositories/entities directly (this is a monolith; that's fine and normal —
see `DashboardService` pulling from five other modules). What it must not do
is reach into another module's *internal* helpers or bypass its public
service methods when a domain invariant needs enforcing (e.g. don't
increment a client's post count by writing to the `post` table from the
`clients` package — go through `contentplan`'s repository, or add a service
method there).

This structure was chosen deliberately over real microservices for this
stage (see the user's own architecture decision) so that module boundaries
can be proven out with actual code before paying for service-per-module
infra (service discovery, network calls, distributed transactions). If/when
a module is split out into its own deployable: it should already be a clean
package with no reverse dependencies from other modules — that's the signal
it's ready to extract, not a calendar date.

## Tenant isolation — the one rule that must never be broken

This is NFR-04 and it is the single most important invariant in this
codebase: **data belonging to one agency/freelancer must never be visible to
another.**

- Every table holding tenant data has an `owner_id` column: an agency
  owner's user id for agency data, or a freelancer's own user id for solo
  data. This mirrors the SRS's `agency_owner_id` / `user_id` split, unified
  into one column since this backend uses a single shared database (see
  `UserAccount.tenantId()`).
- Every such entity extends `TenantScopedEntity` (`common/tenant/`).
- The tenant id for the current request is resolved **from the verified JWT
  claim, never from a request body or query parameter**, by
  `JwtAuthenticationFilter`, and made available via `TenantContext.currentTenantId()`.
- Every repository query against a `TenantScopedEntity` subtype MUST filter
  by `ownerId`. Spring Data derived queries should be named
  `findByOwnerId...` — grep for that prefix as a sanity check when reviewing
  a new repository method. There is no Hibernate filter or RLS enforcing
  this automatically at the DB level yet (the SRS itself flags this as a
  known risk of the original design, section 7) — it is enforced by
  convention and code review only, so a missing `ownerId` filter is a
  data-leak bug, not a style nit.
- Never accept a tenant/owner id as an API input field. If a DTO ever needs
  a field called `ownerId` or `agencyOwnerId`, that's a sign something is
  about to be built wrong — the tenant always comes from the authenticated
  principal.

## Security / auth

- JWT (HS256, `JwtService`) is the only auth mechanism implemented. Claims:
  `sub` (user id), `role`, `tenantId`. One access token, no refresh-token
  rotation yet — acceptable for local/demo use, not for anything real. If
  you add refresh tokens, rotate them server-side and store only a hash.
- `Role` and `RoleAccessPolicy` mirror the SRS's `ROLE_ACCESS` table
  (FR-03). When a feature needs a role check, add/read the flag on
  `RoleAccessPolicy.Access` rather than hardcoding `role == Role.OWNER`
  checks scattered through services — see `DashboardService.buildFinanceSummary`
  for the pattern (check `financeAccess()`, don't enumerate roles).
- Only FR-01 (freelancer email/password) is implemented in `AuthService`.
  FR-02 (agency owner/PM real login, SMM/targetolog shared team password
  verified server-side + anonymous session) is not — do not fake it by
  reusing the freelancer flow; build it properly in `agency`/`auth` when
  it's next up, following the SRS's actual flow (shared password checked on
  the server, never shipped to the client).
- Passwords: BCrypt via Spring Security's `PasswordEncoder` bean. Never log
  a password or token; never return a password hash in any DTO.

## Layering within a module

Controller → Service → Repository. Concretely:

- **Controller**: HTTP concerns only (`@RequestMapping`, status codes,
  `@Valid` on the request body). No business logic, no repository calls.
- **Service**: business logic and transaction boundaries
  (`@Transactional`, read-only where applicable). Reads `TenantContext`
  directly rather than having it passed down from the controller.
- **Repository**: Spring Data JPA interfaces only. Prefer derived query
  methods; drop to `@Query` only when a derived name would be unreadable.
- **Entity vs DTO**: entities never leave the service layer. Every
  controller returns a DTO (a `record`, see `dashboard/dto/`), even when it
  would be a 1:1 field mirror of the entity — this is what lets the entity
  shape change without breaking the API contract.
- Constructor injection only. No field injection (`@Autowired` on a field),
  no `@Lombok` (this project does not use Lombok — explicit getters/constructors
  keep generated bytecode and stack traces predictable without an
  annotation-processing dependency; revisit only with a real pain point, not
  preemptively).

## Database & migrations

- Flyway is the source of truth for schema; `spring.jpa.hibernate.ddl-auto`
  is `validate`, not `update` — Hibernate will refuse to start if an entity
  doesn't match the migrated schema. This is intentional: it forces every
  schema change through a reviewable SQL file.
- New migration = new `V<n>__description.sql` file in
  `src/main/resources/db/migration`. **Never edit a migration that may have
  already run anywhere** (including your own local Postgres) — Flyway
  checksums it. If you need to fix a mistake in an unreleased migration
  before anyone else has run it, that's the one time editing in place is
  fine; once it's shared, add a new migration instead.
- Every tenant-scoped table gets an index on `owner_id` (see `V1__init_schema.sql`)
  — every read path filters by it, so it must never be a sequential scan.
- Seed/demo data lives in its own migration (`V2__seed_dashboard_demo_data.sql`),
  separate from schema, so it's obvious what's structural vs. sample content.

## API conventions

- All endpoints under `/api/v1/...`. Bump to `/api/v2` only for a breaking
  change to an endpoint already in use, not preemptively.
- `GlobalExceptionHandler` + `ApiException` (`common/exception/`) is the only
  error path — throw `ApiException.notFound(...)` / `.badRequest(...)` /
  `.conflict(...)` etc. from services rather than returning nulls or
  ad-hoc `ResponseEntity.status(...)` from controllers.
- Bean Validation (`jakarta.validation`) annotations on request DTOs,
  checked via `@Valid` in the controller — don't hand-roll null checks for
  things `@NotBlank`/`@Email`/`@Size` already cover.
- Swagger UI is wired at `/docs` (`OpenApiConfig`) — no per-endpoint
  `@Operation` annotations required for this stage, but keep DTO field names
  self-explanatory since that's what shows up there.

## Testing

- Unit-test services with plain JUnit5 + Mockito where a repository can be
  stubbed cheaply (see the RoleAccessPolicy branch in `DashboardService`,
  for instance).
- Integration-test anything that touches the database or the full filter
  chain with **Testcontainers Postgres**, not H2 — H2's SQL dialect and
  Flyway compatibility diverge from real Postgres often enough that a
  green H2 test has repeatedly hidden real bugs on other projects; it is
  not a shortcut worth taking here. See `DashboardOverviewIntegrationTest`
  for the pattern (register a user, get a token, hit the endpoint, assert
  through the JWT + tenant filter, not around it).
- Testcontainers tests require a local Docker daemon. If Docker isn't
  available in your environment, say so explicitly rather than reporting
  tests as passing — don't skip/disable them silently to make a build green.
- A new feature is not "done" until: migration + entity match (`mvn verify`
  would catch a mismatch), a tenant-isolation test exists proving one
  tenant cannot see another's rows for the new table, and role-restricted
  data (if the FR has an RBAC rule) is asserted for at least one restricted
  role.

## Adding a new module (e.g. picking up FR-11 tasks next)

1. Read the relevant FR(s) in the SRS in full, including acceptance
   criteria and business rules — they're specific (e.g. FR-11's push
   notification side-effect on task assignment).
2. Delete the module's `package-info.java` placeholder and build the real
   package: `Entity extends TenantScopedEntity`, `Repository` with
   `findByOwnerId...` methods, `Service` with `@Transactional` boundaries,
   DTOs as records, `Controller` under `/api/v1/<module>`.
3. Add a Flyway migration for the new table(s), including the `owner_id`
   index.
4. Wire RBAC via `RoleAccessPolicy` if the FR has an access rule.
5. If the module should feed the dashboard (most will), extend
   `DashboardService`/`DashboardOverviewResponse` rather than having the
   frontend call two endpoints for one screen.
6. Tests per the Testing section above.

## Known simplifications (don't build on top of these without revisiting them)

- `attention_item` is a directly-seeded placeholder table. The real design
  is a computed signal derived from clients/finance/targets (silent lead,
  unpaid invoice, content not ready, budget near exhaustion) — see the
  Javadoc on `AttentionItem`. Don't add a "create attention item" endpoint;
  replace the table with a computed view/service when the modules it
  depends on exist.
- `team_member.loadPercent`/`clientCount` are stored values, not derived
  from `Client.assigneeId` counts. Once the Clients module is fully built,
  this should become a computed aggregate.
- No refresh-token rotation (see Security section).
- Only FR-01 login is implemented; FR-02 (agency roles) is not.
- No Row-Level Security at the Postgres level — isolation is enforced in
  the application layer only (see Tenant isolation section). The original
  SRS (section 7, Risks) flags this same gap and recommends RLS as
  hardening; worth revisiting once there's more than one module writing
  queries, since the more query call sites exist, the more likely one of
  them forgets the `ownerId` filter.