package com.hrms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI Configuration for HR Management System.
 * 
 * This configuration sets up comprehensive API documentation with HTTP-only cookie-based JWT authentication.
 * The documentation includes:
 * - Complete API endpoint documentation
 * - HTTP-only cookie authentication scheme
 * - Interactive API testing capabilities
 * - Server configurations for different environments
 * - Comprehensive contact and license information
 * 
 * Security Features:
 * - HTTP-only cookie-based JWT authentication
 * - Enhanced XSS and CSRF protection
 * - Automatic cookie handling in Swagger UI
 * - Clear security scheme documentation
 * 
 * @author HR Management System Team
 * @version 3.0 - Cookie-based Authentication
 * @since 2024-09-18
 */
@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI hrManagementSystemOpenAPI() {
        // Configure servers for different environments
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Local development server");
        
        Server dockerServer = new Server();
        dockerServer.setUrl("http://localhost:8080");
        dockerServer.setDescription("Docker development server");
        
        Server productionServer = new Server();
        productionServer.setUrl("https://your-production-domain.com");
        productionServer.setDescription("Production server");
        
        // Configure contact information
        Contact contact = new Contact();
        contact.setName("HR Management System API Support");
        contact.setEmail("support@hrms.com");
        contact.setUrl("https://github.com/your-repo/hr-management-system");
        
        // Configure license
        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");
        
        // Configure API information
        Info info = new Info()
                .title("HR Management System API")
                .version("3.0.0")
                .contact(contact)
                .description("A comprehensive Employee/HR Management System API built with Spring Boot. " +
                           "This API provides secure endpoints for managing employees, departments, leave requests, " +
                           "payroll records, and user authentication with full CRUD operations and advanced search capabilities. " +
                           "\n\n**🔐 Cookie-Based Authentication**: This API uses HTTP-only cookies for JWT authentication, providing enhanced security against XSS attacks. " +
                           "To access protected endpoints:\n" +
                           "1. Use the `/api/auth/login` endpoint with your credentials\n" +
                           "2. JWT tokens will be automatically stored in secure HTTP-only cookies\n" +
                           "3. All subsequent API requests will automatically include authentication cookies\n" +
                           "4. No manual token handling required - cookies are managed automatically\n" +
                           "5. Use `/api/auth/logout` to clear authentication cookies\n\n" +
                           "**🛡️ Security Features**:\n" +
                           "- HTTP-only cookies prevent XSS attacks\n" +
                           "- SameSite=Strict prevents CSRF attacks\n" +
                           "- Automatic token refresh functionality\n" +
                           "- Secure cookie transmission over HTTPS\n\n" +
                           "**👥 Default Accounts** (for testing):\n" +
                           "- Admin: username=`admin`, password=`Admin@123`\n" +
                           "- HR Manager: username=`hr.manager`, password=`Admin@123`\n" +
                           "- Employee: username=`demo.user`, password=`Admin@123`\n\n" +
                           "**📝 Note**: When testing in Swagger UI, use the login endpoint first. " +
                           "The browser will automatically handle cookies for subsequent requests.")
                .termsOfService("https://example.com/terms")
                .license(license);
        
        // Configure components - Cookie-based auth doesn't need explicit security schemes
        // Authentication is handled automatically via HTTP-only cookies
        Components components = new Components()
                .addSecuritySchemes("cookieAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name("hrms_access_token")
                        .description("Authentication via HTTP-only cookies. " +
                                   "Use the /api/auth/login endpoint to authenticate. " +
                                   "Cookies are automatically managed by the browser. " +
                                   "No manual authorization required in Swagger UI."));
        
        // Create security requirement for cookie auth
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("cookieAuth");
        
        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer, dockerServer, productionServer))
                .components(components)
                .addSecurityItem(securityRequirement);
    }
}