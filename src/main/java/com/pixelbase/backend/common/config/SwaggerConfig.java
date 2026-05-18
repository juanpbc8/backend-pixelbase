package com.pixelbase.backend.common.config;

import com.pixelbase.backend.common.exception.ApiError;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    private static final String API_ERROR_SCHEMA_REF = "#/components/schemas/ApiError";
    private static final String JSON_MEDIA_TYPE = org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

    @Bean
    public OpenAPI customOpenAPI() {
        Components components = new Components()
            .addSecuritySchemes(SECURITY_SCHEME_NAME,
                new SecurityScheme()
                    .name(SECURITY_SCHEME_NAME)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT"));

        // Forzamos a Springdoc a registrar la estructura de 'ApiError' en el diccionario global
        Map<String, Schema> apiErrorSchemas = ModelConverters.getInstance().readAll(ApiError.class);
        apiErrorSchemas.forEach(components::addSchemas);

        return new OpenAPI()
            .info(new Info()
                .title("PixelBase API")
                .version("1.0")
                .description("Documentación de la API para el E-commerce PixelBase"))
            .components(components);
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
            .group("Public API")
            .pathsToMatch("/api/v1/public/**")
            .build();
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
            .group("Auth API")
            .pathsToMatch("/api/v1/auth/**")
            .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
            .group("Admin API")
            .pathsToMatch("/api/v1/admin/**")
            .addOpenApiCustomizer(adminSecurityCustomizer())
            .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
            .group("Account API")
            .pathsToMatch("/api/v1/account/**")
            .addOpenApiCustomizer(userSecurityCustomizer())
            .build();
    }

    /**
     * Automatiza la documentación de seguridad y respuestas de error para la sección de Administración.
     */
    private OpenApiCustomizer adminSecurityCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) return;

            openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(
                operation -> {
                    operation.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));

                    operation.getResponses().addApiResponse("401", createErrorResponse(
                        "No autenticado. El token JWT es inválido, expiró o no fue proporcionado."
                    ));

                    operation.getResponses().addApiResponse("403", createErrorResponse(
                        "Acceso denegado. El usuario está autenticado pero no posee el rol administrativo"
                    ));
                }));
        };
    }

    /**
     * Automatiza la documentación de seguridad para la sección de cuenta propia del usuario.
     */
    private OpenApiCustomizer userSecurityCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) return;

            openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(
                operation -> {
                    operation.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));

                    operation.getResponses().addApiResponse("401", createErrorResponse(
                        "No autenticado. Debe iniciar sesión con una cuenta válida para gestionar su " +
                            "información de perfil."
                    ));
                }));
        };
    }

    /**
     * Utilitario para construir de forma limpia y reutilizable las respuestas de error mapeadas a ApiError.
     */
    private ApiResponse createErrorResponse(String descripcion) {
        var mediaType = new io.swagger.v3.oas.models.media.MediaType()
            .schema(new Schema<>().$ref(API_ERROR_SCHEMA_REF));

        return new ApiResponse()
            .description(descripcion)
            .content(new Content().addMediaType(JSON_MEDIA_TYPE, mediaType));
    }
}
