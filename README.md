# Vokabel Trainer 📚

A modern vocabulary learning application built with Spring Boot and Progressive Web App technology. Features OAuth2 authentication, multi-list management, practice modes, and offline capability.

## Overview

Vokabel Trainer is a full-stack web application designed for vocabulary learning with the following key features:

- **Multi-user support** with Azure AD OAuth2 authentication
- **Multiple vocabulary lists** per user
- **Interactive practice mode** with forward and reverse learning
- **Alternative answers** support for flexible word matching
- **Progress tracking** with statistics (correct/attempts per word)
- **Progressive Web App** - installable on mobile devices with offline support
- **Responsive design** optimized for mobile-first experience

## Architecture

### Backend
- **Framework**: Spring Boot 4.0.2
- **Language**: Java 17
- **Security**: Spring Security with OAuth2 (Azure AD)
- **Database**: JPA/Hibernate with Microsoft SQL Server
- **API**: RESTful endpoints for vocab management

### Frontend
- **Technology**: Vanilla JavaScript (no framework dependencies)
- **UI**: Mobile-first responsive design
- **PWA**: Service Worker for offline caching
- **Templates**: Thymeleaf for server-side rendering

### Database Model
```
VocabList (id, name, ownerEmail, createdAt, updatedAt)
  └─> VocabWord (id, word, translation, correct, attempts)
        └─> VocabAlternative (id, text)
```

## Prerequisites

- **Java**: 17 or higher
- **Maven**: 3.6+
- **Database**: Microsoft SQL Server or Azure SQL Database
- **OAuth2**: Azure AD App Registration (for authentication)

## Environment Variables

The application requires the following environment variables:

| Variable | Description | Example |
|----------|-------------|---------|
| `CLIENT_ID` | Azure AD App Registration Client ID | `abc123...` |
| `CLIENT_SECRET` | Azure AD App Registration Client Secret | `secret123...` |
| `TENANT_ID` | Azure AD Tenant ID | `tenant-uuid` |
| `DATABASE_USER` | SQL Server username | `dbuser` |
| `DATABASE_PASSWORD` | SQL Server password | `password123` |

## Local Development

### 1. Clone the Repository
```bash
git clone https://github.com/os215/vokabel-app.git
cd vokabel-app
```

### 2. Configure Environment Variables
Create environment variables or update `application.yml`:

```bash
export CLIENT_ID="your-azure-client-id"
export CLIENT_SECRET="your-azure-client-secret"
export TENANT_ID="your-azure-tenant-id"
export DATABASE_USER="your-db-username"
export DATABASE_PASSWORD="your-db-password"
```

### 3. Build and Run
```bash
# Build the application
mvn clean package

# Run the application
java -jar target/vokabel-server-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

### 4. Access the Application
- Open your browser and navigate to `http://localhost:8080`
- You'll be redirected to Azure AD login
- After authentication, you can start managing your vocabulary lists

## API Reference

### Vocabulary Lists

#### Get All Lists
```http
GET /api/vocab/lists
```
Returns all vocabulary lists for the authenticated user, ordered by most recently updated.

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "name": "German-English",
    "ownerEmail": "user@example.com",
    "words": [...],
    "createdAt": "2026-01-15T10:30:00",
    "updatedAt": "2026-01-29T14:20:00"
  }
]
```

#### Create List
```http
POST /api/vocab/lists
Content-Type: application/json

{
  "name": "Spanish Vocabulary"
}
```

**Response**: `200 OK` - Returns the created list object

#### Get Single List
```http
GET /api/vocab/lists/{id}
```

**Response**: `200 OK` - Returns list with all words, or `404 Not Found`

#### Update List
```http
PUT /api/vocab/lists/{id}
Content-Type: application/json

{
  "name": "Updated List Name"
}
```

**Response**: `200 OK` - Returns updated list, or `404 Not Found`

#### Delete List
```http
DELETE /api/vocab/lists/{id}
```

**Response**: `204 No Content` or `404 Not Found`

### Vocabulary Words

#### Add Word to List
```http
POST /api/vocab/lists/{listId}/words
Content-Type: application/json

{
  "word": "Hund",
  "translation": "dog"
}
```

**Response**: `200 OK` - Returns the created word object

#### Update Word
```http
PUT /api/vocab/words/{id}
Content-Type: application/json

{
  "word": "Hund",
  "translation": "dog",
  "alternatives": "doggy, puppy"
}
```

**Response**: `200 OK` - Returns updated word

#### Delete Word
```http
DELETE /api/vocab/words/{id}
```

**Response**: `204 No Content`

#### Submit Practice Answer
```http
POST /api/vocab/words/{id}/answer
Content-Type: application/json

{
  "answer": "dog",
  "isCorrect": true
}
```

**Response**: `200 OK` - Updates word statistics (correct/attempts)

## Deployment

### Docker

#### Build Docker Image
```bash
# Build the JAR first
mvn clean package -DskipTests

# Build Docker image
docker build -t vokabel-server:latest .
```

#### Run with Docker
```bash
docker run -p 8080:8080 \
  -e CLIENT_ID="your-client-id" \
  -e CLIENT_SECRET="your-client-secret" \
  -e TENANT_ID="your-tenant-id" \
  -e DATABASE_USER="your-db-user" \
  -e DATABASE_PASSWORD="your-db-password" \
  vokabel-server:latest
```

### Azure Container Apps

For detailed Azure deployment instructions, see [README_AZURE.md](README_AZURE.md).

**Quick deployment steps:**

1. **Build and push to Azure Container Registry**
   ```bash
   az acr login -n yourregistry
   docker tag vokabel-server:latest yourregistry.azurecr.io/vokabel-server:latest
   docker push yourregistry.azurecr.io/vokabel-server:latest
   ```

2. **Deploy to Container Apps**
   ```bash
   az containerapp create \
     --name vokabel-app \
     --resource-group your-rg \
     --environment your-env \
     --image yourregistry.azurecr.io/vokabel-server:latest \
     --ingress external \
     --target-port 8080
   ```

3. **Configure environment variables** in Azure Portal or via CLI

4. **Set up Azure AD App Registration** with redirect URI pointing to your Container App URL

## Features

### 📝 Vocabulary Management
- Create multiple vocabulary lists
- Add words with translations
- Support for alternative correct answers
- Delete individual words or entire lists
- Rename lists

### ✏️ Practice Mode
- Interactive flash card system
- Forward practice (word → translation)
- Reverse practice (translation → word)
- Real-time answer checking
- Progress tracking per word
- Visual feedback for correct/incorrect answers

### 📊 Statistics
- Track correct answers and total attempts per word
- Calculate success rate for each word
- View overall list statistics
- Color-coded progress indicators

### 📱 Progressive Web App
- Installable on iOS and Android
- Offline support with Service Worker
- App-like experience
- Responsive mobile-first design
- Safe area support for notched devices

### 🔒 Security
- OAuth2 authentication with Azure AD
- User isolation (users only see their own data)
- CSRF protection (disabled for API endpoints)
- Secure session management
- HTTPS recommended for production

## Health Checks

The application includes Spring Boot Actuator health checks:

- **Liveness probe**: `/actuator/health/liveness`
- **Readiness probe**: `/actuator/health/readiness`
- **General health**: `/actuator/health`

These endpoints are used by the Docker healthcheck and Kubernetes/Container Apps for monitoring.

## Browser Support

- **Chrome/Edge**: Full support
- **Safari/iOS**: Full support with PWA installation
- **Firefox**: Full support
- **Mobile browsers**: Optimized for mobile use

## Development

### Running Tests
```bash
mvn test
```

### Building for Production
```bash
mvn clean package -DskipTests
```

### Hot Reload (Development)
```bash
mvn spring-boot:run
```

## Troubleshooting

### Authentication Issues
- Verify Azure AD App Registration redirect URIs match your deployment URL
- Check that `CLIENT_ID`, `CLIENT_SECRET`, and `TENANT_ID` are correctly set
- Ensure the Azure AD app has the correct API permissions

### Database Connection Issues
- Verify database credentials (`DATABASE_USER`, `DATABASE_PASSWORD`)
- Check firewall rules allow connections to SQL Server
- Confirm connection string format in `application.yml`

### Build Issues
- Ensure Java 17+ is installed: `java -version`
- Clear Maven cache: `mvn clean`
- Check for dependency conflicts: `mvn dependency:tree`

## Contributing

Contributions are welcome! Please feel free to submit issues and pull requests.

## License

This project is available for educational and personal use.

## Related Documentation

- [Azure Deployment Guide](README_AZURE.md) - Detailed Azure Container Apps deployment
- [Local Development Guide](README_RUN.md) - Quick start for local development

## Support

For issues and questions, please open an issue on the GitHub repository.

---

**Version**: 0.0.1-SNAPSHOT  
**Last Updated**: January 2026
