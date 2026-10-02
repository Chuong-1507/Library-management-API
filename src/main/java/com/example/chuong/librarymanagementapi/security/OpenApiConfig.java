package com.example.chuong.librarymanagementapi.security;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.hibernate.validator.constraints.ru.INN;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@OpenAPIDefinition(
        info = @Info(
                title = "Library Management API",
                version = "1.0",
                description = "REST API quản lý thư viện - mượn/trả sách, quản lý user, báo cáo thống kê"
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
public class OpenApiConfig{
}
