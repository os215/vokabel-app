# Vokabel Trainer

A vocabulary-learning web application with a Spring Boot backend, a responsive browser client, and Azure OpenID Connect and email/password sign-in.

## Stack

- Java 17 and Spring Boot 4.1.1
- Maven
- MariaDB through Spring Data JPA
- Thymeleaf templates and vanilla JavaScript
- Azure OIDC sign-in and database-backed email/password accounts

## Run locally

Install Java 17 or newer, Maven, and a MariaDB instance. Configure these environment variables before starting the application:

| Variable | Purpose |
| --- | --- |
| `CLIENT_ID` | Azure app registration client ID |
| `CLIENT_SECRET` | Azure app registration client secret |
| `TENANT_ID` | Azure tenant ID |
| `DATABASE_URL` | MariaDB JDBC URL |
| `DATABASE_USER` | Database username |
| `DATABASE_PASSWORD` | Database password |

For example, set the values in your shell or IDE run configuration; do not put real credentials in source control. Then build and run:

```sh
mvn package
java -jar target/vokabel-server-0.0.1-SNAPSHOT.jar
```

The server listens on port `8080`. Open `http://localhost:8080`. To use Azure sign-in locally, configure the app registration redirect URI for the local callback:

```text
http://localhost:8080/login/oauth2/code/azure
```

## Features

- Create and manage vocabulary lists and words
- Practice in forward and reverse directions
- Store alternative answers and per-word practice counts
- Installable Progressive Web App interface
- POWERUSER-only user administration for internal accounts

## Authentication and access

Azure OIDC roles are mapped to `POWERUSER` and `VISITOR`; an Azure account without a recognized role defaults to `VISITOR`. Internal email/password accounts are always assigned `VISITOR`. See [Authentication and roles](docs/AUTHENTICATION_AND_ROLES.md) and [User management](docs/USER_MANAGEMENT.md).

**Current access-control limitation:** authenticated users can currently read every vocabulary list through `GET /api/vocab/lists` and `GET /api/vocab/lists/{id}`. Write operations are role- and owner-checked, but read access is not scoped by owner. Do not rely on this application to keep different users' vocabulary data private until this is addressed.

## API overview

The vocabulary API is under `/api/vocab`; the internal account-management API is under `/api/users`. All API routes require authentication. Vocabulary write operations and all user-management operations require `POWERUSER`. The API and exact current behavior are implemented in `VocabApiController` and `UserManagementApiController`.

## Deployment

See the [Azure Container Apps guide](README_AZURE.md) for deployment considerations. The application expects its database and Azure OAuth settings to be supplied as environment variables.

## Tests

```sh
mvn test
```
