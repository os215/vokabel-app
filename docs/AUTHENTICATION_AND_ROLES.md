# Authentication and roles

The application supports Azure OpenID Connect sign-in and database-backed email/password sign-in. Spring Security applies the same application roles to both mechanisms, with an important restriction on internal accounts.

## Role assignment

| Sign-in method | Role behavior |
| --- | --- |
| Azure OIDC | `CustomOidcUserService` maps recognized values from the `roles` and `groups` claims. If no supported role is found, the account receives `VISITOR`. |
| Email/password | Internal users receive `VISITOR` only. `CustomUserDetailsService` filters any stored role other than `VISITOR`, and user creation always assigns `VISITOR`. |

Recognized Azure values are case-insensitive:

- `POWERUSER`, `PowerUser`, `VokabelPowerUser`
- `VISITOR`, `Visitor`, `VokabelVisitor`

Group object IDs are not mapped to application roles. Configure role names in Azure claims if role-based access is required.

## Current permissions

- Authenticated users can open the application and read vocabulary data.
- `POWERUSER` is required for vocabulary write operations and internal user administration.
- Vocabulary write operations also check list ownership.
- Internal user administration is available at `/users` and uses `/api/users`; newly created internal accounts are `VISITOR`.

The browser hides controls based on role, but backend authorization is authoritative. A hidden or disabled control is not a security boundary.

## Known access-control limitations

The current `GET /api/vocab/lists` and `GET /api/vocab/lists/{id}` handlers do not filter by owner. Any authenticated user can read all vocabulary lists, even though write operations are owner-checked. Do not assume that vocabulary data is private between users.

The user-management API currently serializes `User` entities directly. The entity includes the encoded password field, so user-management responses can expose password hashes to a POWERUSER. Treat this as a security issue, not expected functionality.

The security configuration also exempts `/api/**` from CSRF protection. Reassess this if changing the authentication model or exposing the API to browser-based clients.

These limitations describe current source behavior and should be revisited after the corresponding implementation changes.
