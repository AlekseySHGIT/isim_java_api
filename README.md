# ISIM REST API Client Library

A Java library for interacting with IBM Security Identity Manager (ISIM) 6.0.0.26 REST API.

## 🚀 Features

- Seamless authentication with ISIM REST endpoints
- Session management with LtpaToken2 support
- CSRF token handling for secure API access
- Modern Java HTTP client implementation
- Compatible with ISIM 6.0.0.26

## 📋 Prerequisites

- Java 11 or higher
- ISIM 6.0.0.26
- Spring Boot 2.x or higher

## 🔧 Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.ashitikov</groupId>
    <artifactId>isimserver</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 🎯 Quick Start

```java
// Initialize the client with ISIM server URL
String isimUrl = "http://your-isim-server:9080";
String username = "your-username";
String password = "your-password";

// Create an instance of ISIMClient
ISIMClient client = new ISIMClient(isimUrl, username, password);

// The client automatically handles:
// 1. Authentication
// 2. Session management
// 3. CSRF token retrieval
```

## 🔐 Authentication Flow

The library implements the exact authentication flow required by ISIM 6.0.0.26:

1. Session initialization
2. Security check with credentials
3. CSRF token retrieval
4. Automatic cookie management (LtpaToken2, JSESSIONID)

## ⚙️ Configuration

The library supports the following configuration options:

- Custom server URL
- Authentication credentials
- Session management

## 🛡️ Security Features

- Secure cookie handling
- CSRF protection
- URL encoding for form data
- Session management

## 🔑 CSRF Token Usage

The library's primary function is to obtain a CSRF token, which is **required for all subsequent ISIM REST API calls**. This token ensures secure communication with the ISIM server.

### Using the CSRF Token

1. Obtain the token through authentication
2. Include the token in the header of all subsequent REST API calls
3. Token is required for operations such as:
   - User management
   - Role management
   - Access requests
   - Workflow operations
   - And more...

Example header usage:
```http
X-CSRF-Token: YOUR_CSRF_TOKEN
```

## 📝 Example Usage

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, 
                                 @RequestParam String password,
                                 @RequestParam(required = false) String serverUrl) {
        try {
            ISIMClient client = new ISIMClient(serverUrl, username, password);
            String csrfToken = client.getCSRF();
            // Use the client for further ISIM operations
            return ResponseEntity.ok()
                    .header("CSRFToken", csrfToken)
                    .body(Map.of("message", "Login successful"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
```

## 🚧 Upcoming Features

The following REST API methods will be implemented in future releases:

- User Management
  - Create/Read/Update/Delete users
  - Search users
  - Manage user attributes

- Role Management
  - Role assignments
  - Role hierarchy management
  - Access policy management

- Service Management
  - Service catalog operations
  - Service provisioning
  - Service access management

- Workflow Operations
  - Workflow initiation
  - Task management
  - Approval processes

Stay tuned for updates! Follow our release schedule for new features.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🔗 Links

- [ISIM Documentation](https://www.ibm.com/docs/en/identity-manager/6.0.0)
- [REST API Reference](https://www.ibm.com/docs/en/identity-manager/6.0.0?topic=reference-rest-api)

## ⚠️ Compatibility Note

This library is specifically tested and compatible with IBM Security Identity Manager (ISIM) version 6.0.0.26. While it may work with other versions, full compatibility is only guaranteed with this specific version.
