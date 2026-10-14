# Internal user management

Users with the `POWERUSER` role can open the administration page at `/users`. It manages database-backed email/password accounts only; it does not manage Azure identities or Azure role assignments.

The current page supports:

- Creating an internal user
- Enabling or disabling an account
- Resetting an account password
- Deleting an account

Every internal account is created with the `VISITOR` role. The UI requires passwords of at least six characters, but the API's current validation only rejects missing or blank values; do not treat the UI constraint as server-side password policy.

## Authorization and API

The page is protected by `POWERUSER` authorization. The `/api/users` controller also enforces `POWERUSER` authorization for its endpoints:

| Method and path | Action |
| --- | --- |
| `GET /api/users` | List internal accounts |
| `POST /api/users` | Create a VISITOR account |
| `PUT /api/users/{id}/disable` | Disable an account |
| `PUT /api/users/{id}/enable` | Enable an account |
| `PUT /api/users/{id}/reset-password` | Reset an account password |
| `DELETE /api/users/{id}` | Delete an account |

Passwords are stored using BCrypt. However, the current controller returns `User` entities directly for list, create, enable, and disable responses, and the entity's password getter is serializable. Those responses can therefore expose password hashes to an authorized POWERUSER. This is a known security defect; do not mistake role protection for safe response serialization.

For role mapping and current authentication limitations, see [Authentication and roles](AUTHENTICATION_AND_ROLES.md).
