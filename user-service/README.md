# user-service — HireConnect Day 2

Combines **Auth** and **Profile** into a single Spring Boot microservice.

## Port
`8081` (accessed via API Gateway on `8080`)

## Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8 running on localhost:3306
- Eureka Server running on localhost:8761

## Setup

### 1. MySQL database
The database `hireconnect_users` is created automatically on first run
(`createDatabaseIfNotExist=true` in the datasource URL).

Update credentials in `src/main/resources/application.yml` if needed:
```yaml
spring:
  datasource:
    username: root
    password: root
```

### 2. GitHub OAuth (optional)
Replace the placeholders in `application.yml`:
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          github:
            client-id: YOUR_GITHUB_CLIENT_ID
            client-secret: YOUR_GITHUB_CLIENT_SECRET
```
Register a GitHub OAuth App at https://github.com/settings/developers
- Homepage URL: `http://localhost:8081`
- Callback URL: `http://localhost:8081/login/oauth2/code/github`

### 3. Run
```bash
mvn spring-boot:run
```

## API Endpoints

All requests should go through the API Gateway (`localhost:8080`).

### Auth
| Method | URL | Body | Auth |
|--------|-----|------|------|
| POST | `/api/auth/register` | `{"email","password","role"}` | None |
| POST | `/api/auth/login` | `{"email","password"}` | None |
| GET  | `/api/auth/validate?token=...` | — | None |
| POST | `/api/auth/refresh?token=...` | — | None |
| DELETE | `/api/auth/{userId}` | — | Bearer |

### Candidate Profile
| Method | URL | Auth |
|--------|-----|------|
| POST   | `/api/profile/candidate` | Bearer |
| GET    | `/api/profile/candidate/user/{userId}` | Bearer |
| GET    | `/api/profile/candidate/{profileId}` | Bearer |
| PUT    | `/api/profile/candidate/user/{userId}` | Bearer |
| DELETE | `/api/profile/candidate/user/{userId}` | Bearer |
| GET    | `/api/profile/candidates` | Bearer |

### Recruiter Profile
| Method | URL | Auth |
|--------|-----|------|
| POST   | `/api/profile/recruiter` | Bearer |
| GET    | `/api/profile/recruiter/user/{userId}` | Bearer |
| GET    | `/api/profile/recruiter/{profileId}` | Bearer |
| PUT    | `/api/profile/recruiter/user/{userId}` | Bearer |
| DELETE | `/api/profile/recruiter/user/{userId}` | Bearer |
| GET    | `/api/profile/recruiters` | Bearer |

## Sample curl commands

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@test.com","password":"pass123","role":"CANDIDATE"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@test.com","password":"pass123"}'

# Create candidate profile (replace TOKEN)
curl -X POST http://localhost:8080/api/profile/candidate \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"fullName":"Alice","email":"alice@test.com","skills":["Java","Spring Boot"],"experience":2,"summary":"Backend developer"}'

# Get candidate profile
curl http://localhost:8080/api/profile/candidate/user/1 \
  -H "Authorization: Bearer TOKEN"
```

## Project structure

```
user-service/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/hireconnect/user/
    │   │   ├── UserServiceApplication.java
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java
    │   │   │   ├── OAuth2SuccessHandler.java
    │   │   │   └── GlobalExceptionHandler.java
    │   │   ├── entity/
    │   │   │   ├── UserCredential.java
    │   │   │   ├── CandidateProfile.java
    │   │   │   ├── RecruiterProfile.java
    │   │   │   └── Address.java
    │   │   ├── repository/
    │   │   │   ├── UserCredentialRepository.java
    │   │   │   ├── CandidateProfileRepository.java
    │   │   │   └── RecruiterProfileRepository.java
    │   │   ├── service/
    │   │   │   ├── AuthService.java
    │   │   │   ├── AuthServiceImpl.java
    │   │   │   ├── ProfileService.java
    │   │   │   └── ProfileServiceImpl.java
    │   │   ├── resource/
    │   │   │   ├── AuthResource.java
    │   │   │   └── ProfileResource.java
    │   │   └── util/
    │   │       └── JwtUtil.java
    │   └── resources/
    │       └── application.yml
    └── test/
        ├── java/com/hireconnect/user/
        │   └── UserServiceApplicationTests.java
        └── resources/
            └── application-test.yml
```
