# HR Management System

A comprehensive Employee/HR Management System built with **Spring Boot**, **MySQL**, and **Docker**. This system provides RESTful APIs for managing employees, departments, leave requests, and payroll records with advanced search capabilities and Swagger documentation.

## 🏗️ Tech Stack

- **Backend Framework**: Spring Boot 3.5.5
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA with Hibernate
- **API Documentation**: Swagger/OpenAPI 3
- **Containerization**: Docker & Docker Compose
- **Build Tool**: Maven 3.9+
- **Java Version**: 17+

## 📋 Features

### Core Modules
- **👥 Employee Management**: Complete CRUD operations with advanced search and filtering
- **🏢 Department Management**: Organize employees into departments
- **📅 Leave Request Management**: Submit, approve, and track leave requests
- **💰 Payroll Management**: Generate and manage employee payroll records

### Key Capabilities
- **RESTful API Design** with proper HTTP status codes
- **Comprehensive Validation** with custom error handling
- **Advanced Search & Filtering** by multiple criteria
- **Audit Tracking** with created/updated timestamps
- **Database Relationships** with proper foreign key constraints
- **Circular Reference Prevention** using DTOs and Jackson annotations
- **Optimized API Responses** with selective data loading
- **Professional DTO Pattern** with flexible, reusable data structures
- **Swagger UI** for API testing and documentation
- **Docker Support** for easy deployment
- **Health Checks** and monitoring endpoints

## 🚀 Quick Start

### Prerequisites
- Docker and Docker Compose installed
- Java 17+ (for local development)
- Maven 3.9+ (for local development)

### Option 1: Docker (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd hr-management-system
   ```

2. **Start the application with Docker Compose**
   ```bash
   docker-compose up -d
   ```

3. **Access the application**
   - API Base URL: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - phpMyAdmin: http://localhost:8081 (user: root, password: root123)

4. **Stop the application**
   ```bash
   docker-compose down
   ```

### Option 2: Local Development

1. **Prerequisites**
   - MySQL 8.0 running locally
   - Java 17+
   - Maven 3.9+

2. **Setup Database**
   ```sql
   CREATE DATABASE hr_management_db;
   ```

3. **Configure Database Connection**
   Update `src/main/resources/application.properties` with your MySQL credentials.

4. **Build and Run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

## 📊 Database Schema

### Tables
- **departments**: Department information
- **employees**: Employee records with department relationships
- **leave_requests**: Leave requests with approval workflow
- **payrolls**: Monthly payroll records with calculations

### Relationships
- Employee ↔ Department (Many-to-One)
- Employee ↔ Leave Request (One-to-Many)  
- Employee ↔ Payroll (One-to-Many)

## 🔌 API Endpoints

### Department Management
```
GET    /api/departments                         # Get all departments (with employee count)
GET    /api/departments/{id}                    # Get department by ID (with employee count)
GET    /api/departments/name/{name}             # Get department by name
POST   /api/departments                         # Create department
PUT    /api/departments/{id}                    # Update department
DELETE /api/departments/{id}                    # Delete department
GET    /api/departments/search?name={name}      # Search departments by name
GET    /api/departments/{id}/with-employees     # Get department with employees list
GET    /api/departments/employee-count          # Get all departments with employee count stats
```

### Employee Management
```
GET    /api/employees                           # Get all employees
GET    /api/employees/{id}                      # Get employee by ID
GET    /api/employees/email/{email}             # Get employee by email
POST   /api/employees                           # Create employee
PUT    /api/employees/{id}                      # Update employee
DELETE /api/employees/{id}                      # Delete employee
GET    /api/employees/department/{deptId}       # Get employees by department
GET    /api/employees/search                    # Advanced search
GET    /api/employees/salary-range              # Filter by salary range
```

### Leave Request Management
```
GET    /api/leave-requests                      # Get all leave requests
GET    /api/leave-requests/{id}                 # Get leave request by ID
POST   /api/leave-requests                      # Submit leave request
PUT    /api/leave-requests/{id}                 # Update leave request
PUT    /api/leave-requests/{id}/approve         # Approve leave request
PUT    /api/leave-requests/{id}/reject          # Reject leave request
DELETE /api/leave-requests/{id}                 # Delete leave request
GET    /api/leave-requests/pending              # Get pending requests
GET    /api/leave-requests/employee/{empId}     # Get requests by employee
```

### Payroll Management
```
GET    /api/payroll                             # Get all payroll records
GET    /api/payroll/{id}                        # Get payroll by ID
POST   /api/payroll                             # Create payroll record
PUT    /api/payroll/{id}                        # Update payroll record
DELETE /api/payroll/{id}                        # Delete payroll record
POST   /api/payroll/employee/{empId}/generate   # Generate payroll for employee
GET    /api/payroll/employee/{empId}            # Get payroll by employee
GET    /api/payroll/reports/total-cost          # Get total payroll cost
```

## � API Response Structure

### Department Responses (DTO-based)

The API now uses optimized DTOs to prevent circular references and improve performance:

#### Basic Department Response (List/Search endpoints)
```json
{
  "success": true,
  "message": "Departments retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Engineering",
      "description": "Software development team",
      "employeeCount": 15,
      "createdAt": "2025-01-01T10:00:00",
      "updatedAt": "2025-01-01T10:00:00"
    }
  ]
}
```

#### Department with Employees Response
```json
{
  "success": true,
  "message": "Department with employees retrieved successfully",
  "data": {
    "id": 1,
    "name": "Engineering",
    "description": "Software development team",
    "employeeCount": 2,
    "createdAt": "2025-01-01T10:00:00",
    "updatedAt": "2025-01-01T10:00:00",
    "employees": [
      {
        "id": 1,
        "name": "John Doe",
        "email": "john.doe@company.com",
        "phone": "+1234567890",
        "position": "Senior Developer",
        "dateOfJoining": "2023-01-15",
        "salary": 85000.00,
        "createdAt": "2023-01-15T09:00:00",
        "updatedAt": "2023-01-15T09:00:00",
        "department": {
          "id": 1,
          "name": "Engineering",
          "description": "Software development team"
        }
      },
      {
        "id": 2,
        "name": "Jane Smith",
        "email": "jane.smith@company.com",
        "phone": "+1234567891",
        "position": "Lead Developer",
        "dateOfJoining": "2022-05-10",
        "salary": 95000.00,
        "createdAt": "2022-05-10T08:30:00",
        "updatedAt": "2022-05-10T08:30:00",
        "department": {
          "id": 1,
          "name": "Engineering",
          "description": "Software development team"
        }
      }
    ]
  }
}
```

## �📝 Sample API Usage

### Create a Department
```bash
curl -X POST http://localhost:8080/api/departments \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Engineering",
    "description": "Software development and engineering teams"
  }'
```

### Create an Employee
```bash
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@company.com",
    "phone": "+1234567890",
    "position": "Senior Developer",
    "dateOfJoining": "2023-01-15",
    "salary": 85000.00,
    "department": {"id": 1}
  }'
```

### Submit Leave Request
```bash
curl -X POST http://localhost:8080/api/leave-requests \
  -H "Content-Type: application/json" \
  -d '{
    "startDate": "2024-12-25",
    "endDate": "2024-12-29",
    "leaveType": "VACATION",
    "reason": "Christmas holidays",
    "employee": {"id": 1}
  }'
```

### Department Specific Operations

#### Get Department with Employees
```bash
curl -X GET http://localhost:8080/api/departments/1/with-employees \
  -H "Content-Type: application/json"
```

#### Search Departments
```bash
curl -X GET "http://localhost:8080/api/departments/search?name=Engineering" \
  -H "Content-Type: application/json"
```

#### Get Department by Name
```bash
curl -X GET http://localhost:8080/api/departments/name/Engineering \
  -H "Content-Type: application/json"
```

## 🐳 Docker Configuration

### Services
- **hrms-app**: Spring Boot application (Port 8080)
- **mysql-db**: MySQL database (Port 3307)
- **phpmyadmin**: Database management UI (Port 8081)

### Environment Variables
```yaml
SPRING_PROFILES_ACTIVE: docker
SPRING_DATASOURCE_URL: jdbc:mysql://mysql-db:3306/hr_management_db
SPRING_DATASOURCE_USERNAME: root
SPRING_DATASOURCE_PASSWORD: root123
```

### Docker Commands
```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f hrms-app

# Scale application (if needed)
docker-compose up -d --scale hrms-app=2

# Stop all services
docker-compose down

# Remove volumes (data will be lost)
docker-compose down -v
```

## 🛠️ Development

### Project Structure
```
src/
├── main/java/com/hrms/
│   ├── application/          # Main application class
│   ├── controller/           # REST controllers
│   ├── service/              # Business logic layer
│   ├── repository/           # Data access layer
│   ├── entity/               # JPA entities
│   ├── dto/                  # Data transfer objects
│   ├── exception/            # Custom exceptions and handlers
│   └── config/               # Configuration classes
├── main/resources/
│   ├── application.properties           # Main configuration
│   └── application-docker.properties    # Docker configuration
└── test/                     # Unit tests
```

### Key Features Implemented
- **Layered Architecture**: Controller → Service → Repository → Entity
- **DTO Pattern**: Data Transfer Objects prevent circular references and optimize responses
- **Jackson Annotations**: `@JsonManagedReference` and `@JsonBackReference` for entity relationships
- **Selective Loading**: Employees loaded only when explicitly requested
- **Exception Handling**: Custom exceptions with global exception handler
- **Validation**: Bean validation with custom validators
- **Audit Trail**: Automatic timestamps for all entities
- **Health Checks**: Docker health checks and Spring actuator
- **API Documentation**: Comprehensive Swagger documentation

### Database Migration
The application uses JPA with `hibernate.ddl-auto=update` for automatic schema management. For production, consider using Flyway or Liquibase for versioned migrations.

## 📊 Sample Data

The application includes sample data for testing:
- 6 departments (HR, IT, Finance, Marketing, Operations, Sales)
- 8 employees across different departments
- Sample leave requests with different statuses
- Sample payroll records

## ⚡ Performance Considerations

- **Database Indexing**: Proper indexes on frequently queried columns
- **Lazy Loading**: JPA relationships configured with appropriate fetch types
- **Connection Pooling**: HikariCP for database connection management
- **Caching**: Ready for Redis/Hazelcast integration
- **Pagination**: Repository methods support Spring Data pagination

## 🔒 Security Notes

- **Input Validation**: Comprehensive validation on all endpoints
- **SQL Injection Prevention**: Using parameterized queries through JPA
- **CORS Configuration**: Configurable for different environments
- **Authentication**: Ready for Spring Security integration

## 🚀 Production Deployment

### Environment-specific Configuration
1. Create `application-prod.properties` for production settings
2. Use environment variables for sensitive data
3. Configure proper logging levels
4. Set up database connection pooling
5. Configure monitoring and health checks

### Recommended Infrastructure
- **Container Orchestration**: Kubernetes or Docker Swarm
- **Load Balancing**: Nginx or Application Load Balancer
- **Database**: MySQL cluster or Amazon RDS
- **Monitoring**: Prometheus + Grafana
- **Logging**: ELK Stack or centralized logging

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Support

For support or questions, please contact:
- **Email**: support@hrms.com
- **GitHub Issues**: [Create an issue](https://github.com/your-repo/hr-management-system/issues)

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA Guide](https://spring.io/guides/gs/accessing-data-jpa/)
- [Docker Documentation](https://docs.docker.com/)
- [MySQL Documentation](https://dev.mysql.com/doc/)
- [Swagger Documentation](https://swagger.io/docs/)