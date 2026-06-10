# Inventra Auth - Complete Authentication System

A complete Spring Boot authentication system with JWT tokens, email verification, and password reset functionality.

## 🚀 Features

- **User Registration** with email verification
- **Email OTP Verification** (6-digit code)
- **JWT Authentication** with secure token generation
- **Login System** with credential validation
- **Password Reset** via email link
- **Secure Password Storage** with BCrypt encryption
- **Modern UI** with responsive design
- **RESTful API** endpoints

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+ (or use H2 for testing)
- SMTP server for email (Gmail recommended)

## 🛠️ Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd inventra-auth
```

### 2. Configure Database

Edit `src/main/resources/application.properties`:

**For MySQL:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/inventra_auth?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
```

**For H2 (Testing):**
```properties
# Uncomment these lines in application.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
```

### 3. Configure Email Service

Update email settings in `application.properties`:

```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

**For Gmail:**
1. Enable 2-Factor Authentication
2. Generate an App Password: https://myaccount.google.com/apppasswords
3. Use the generated password in `spring.mail.password`

### 4. Build the Project

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

Or run the JAR:
```bash
java -jar target/inventra-auth-1.0.0.jar
```

The application will start on `http://localhost:8080`

## 📱 Usage

### Web Pages

- **Login:** http://localhost:8080/login
- **Sign Up:** http://localhost:8080/signup
- **Verify OTP:** http://localhost:8080/verify-otp
- **Forgot Password:** http://localhost:8080/forgot-password
- **Reset Password:** http://localhost:8080/reset-password
- **Home (Protected):** http://localhost:8080/home

### API Endpoints

#### Sign Up
```http
POST /api/auth/signup
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123"
}
```

#### Verify OTP
```http
POST /api/auth/verify-otp
Content-Type: application/json

{
  "email": "john@example.com",
  "otp": "123456"
}
```

#### Resend OTP
```http
POST /api/auth/resend-otp
Content-Type: application/json

{
  "email": "john@example.com"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123"
}
```

Response:
```json
{
  "success": true,
  "message": "Login successful!",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "john_doe",
  "email": "john@example.com"
}
```

#### Forgot Password
```http
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "john@example.com"
}
```

#### Reset Password
```http
POST /api/auth/reset-password
Content-Type: application/json

{
  "token": "reset-token-uuid",
  "password": "newpassword123"
}
```

### Using JWT Token

Include the JWT token in the Authorization header for protected endpoints:

```http
GET /home
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## 🏗️ Project Structure

```
inventra-auth/
├── src/
│   ├── main/
│   │   ├── java/com/inventra/auth/
│   │   │   ├── InventraAuthApplication.java    # Main application
│   │   │   ├── controller/
│   │   │   │   └── AuthPageController.java     # REST Controllers
│   │   │   ├── entity/
│   │   │   │   └── User.java                   # User entity
│   │   │   ├── repository/
│   │   │   │   └── UserRepository.java         # JPA repository
│   │   │   ├── security/
│   │   │   │   ├── JWTFilter.java              # JWT authentication filter
│   │   │   │   └── SecurityConfig.java         # Security configuration
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java            # Business logic
│   │   │   │   └── EmailService.java           # Email sending
│   │   │   └── util/
│   │   │       └── JWTUtility.java             # JWT token utility
│   │   └── resources/
│   │       ├── application.properties           # Configuration
│   │       └── templates/                       # HTML pages
│   │           ├── login.html
│   │           ├── signup.html
│   │           ├── verify-otp.html
│   │           ├── forgot-password.html
│   │           ├── reset-password.html
│   │           └── home.html
│   └── test/
│       └── java/com/inventra/auth/
│           └── InventraAuthApplicationTests.java
└── pom.xml                                      # Maven dependencies
```

## 🔐 Security Features

- **BCrypt Password Hashing**: Passwords are encrypted using BCrypt
- **JWT Tokens**: Secure token-based authentication
- **Email Verification**: Users must verify email before login
- **OTP Expiry**: OTP codes expire after 10 minutes
- **Password Reset Token**: Reset tokens expire after 1 hour
- **CSRF Protection**: Disabled for API endpoints (enable for production if using session-based auth)

## 🧪 Testing

Run tests:
```bash
mvn test
```

## 📝 Configuration Properties

Key configuration options in `application.properties`:

```properties
# Server
server.port=8080

# JWT
jwt.secret=your-secret-key-here
jwt.expiration=86400000  # 24 hours in milliseconds

# Database
spring.jpa.hibernate.ddl-auto=update  # Change to 'validate' in production

# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
```

## 🚀 Production Deployment

### Before deploying to production:

1. **Change JWT Secret**: Generate a strong secret key
2. **Database**: Use production-grade database (MySQL/PostgreSQL)
3. **Email**: Configure production email service
4. **HTTPS**: Enable SSL/TLS
5. **Environment Variables**: Use environment variables for sensitive data
6. **Logging**: Configure appropriate logging levels
7. **CSRF**: Re-enable CSRF protection if needed
8. **Rate Limiting**: Implement rate limiting for API endpoints

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 👤 Author

Inventra Auth Team
Angothu Adhisheshu

## 📧 Support

For support, email angothuadhisheshu@gmail.com or open an issue.

---

**Happy Coding! 🎉**
