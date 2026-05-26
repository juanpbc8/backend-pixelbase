package com.pixelbase.backend.common.config;

import com.pixelbase.backend.common.exception.ApiError;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    private static final String API_ERROR_SCHEMA_REF = "#/components/schemas/ApiError";
    private static final String JSON_MEDIA_TYPE = org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

    @Bean
    public OpenAPI customOpenAPI() {
        Components components = new Components()
            .addSecuritySchemes(
                SECURITY_SCHEME_NAME,
                new SecurityScheme()
                    .name(SECURITY_SCHEME_NAME)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT"));

        registerApiErrorSchema(components);

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
            .addOpenApiCustomizer(globalErrorCustomizer())
            .build();
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
            .group("Auth API")
            .pathsToMatch("/api/v1/auth/**")
            .addOpenApiCustomizer(globalErrorCustomizer())
            .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
            .group("Admin API")
            .pathsToMatch("/api/v1/admin/**")
            .addOpenApiCustomizer(globalErrorCustomizer())
            .addOpenApiCustomizer(adminSecurityCustomizer())
            .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
            .group("Account API")
            .pathsToMatch("/api/v1/account/**")
            .addOpenApiCustomizer(globalErrorCustomizer())
            .addOpenApiCustomizer(userSecurityCustomizer())
            .build();
    }

    private OpenApiCustomizer globalErrorCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }

            ensureApiErrorSchema(openApi);

            openApi.getPaths().forEach((path, pathItem) -> pathItem.readOperationsMap().forEach(
                (httpMethod, operation) -> {
                    addOrMergeErrorResponse(operation, "500", "Error interno del servidor.");

                    if (isBodyMethod(httpMethod)) {
                        addOrMergeErrorResponse(
                            operation,
                            "400",
                            "La petición contiene campos inválidos, nulos o malformados."
                        );
                    }

                    if (path.contains("{") && path.contains("}")) {
                        addOrMergeErrorResponse(
                            operation,
                            "404",
                            "El recurso solicitado no fue encontrado en el sistema."
                        );
                    }
                }));
        };
    }

    private OpenApiCustomizer adminSecurityCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }

            openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
                operation.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
                addOrMergeErrorResponse(
                    operation,
                    "401",
                    "No autenticado. El token JWT es inválido, expiró o no fue proporcionado."
                );
                addOrMergeErrorResponse(
                    operation,
                    "403",
                    "Acceso denegado. El usuario está autenticado pero no posee el rol administrativo"
                );
            }));
        };
    }

    private OpenApiCustomizer userSecurityCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }

            openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
                operation.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
                addOrMergeErrorResponse(
                    operation,
                    "401",
                    "No autenticado. Debe iniciar sesión con una cuenta válida para gestionar su "
                        + "información de perfil."
                );
            }));
        };
    }

    private void addOrMergeErrorResponse(Operation operation, String responseCode, String description) {
        ApiResponses responses = operation.getResponses();
        if (responses == null) {
            responses = new ApiResponses();
            operation.setResponses(responses);
        }

        ApiResponse response = responses.get(responseCode);
        if (response == null) {
            responses.addApiResponse(responseCode, createErrorResponse(description));
            return;
        }

        if (response.getDescription() == null || response.getDescription().isBlank()) {
            response.setDescription(description);
        }

        if (response.getContent() == null || response.getContent().isEmpty()) {
            response.setContent(createJsonErrorContent());
        }
    }

    private boolean isBodyMethod(HttpMethod httpMethod) {
        return httpMethod == HttpMethod.POST
            || httpMethod == HttpMethod.PUT
            || httpMethod == HttpMethod.PATCH;
    }

    private ApiResponse createErrorResponse(String description) {
        return new ApiResponse()
            .description(description)
            .content(createJsonErrorContent());
    }

    private Content createJsonErrorContent() {
        return new Content().addMediaType(
            JSON_MEDIA_TYPE,
            new MediaType().schema(new Schema<>().$ref(API_ERROR_SCHEMA_REF))
        );
    }

    private void ensureApiErrorSchema(OpenAPI openApi) {
        if (openApi.getComponents() == null) {
            openApi.setComponents(new Components());
        }

        registerApiErrorSchema(openApi.getComponents());
    }

    private void registerApiErrorSchema(Components components) {
        ResolvedSchema resolvedSchema = ModelConverters.getInstance().resolveAsResolvedSchema(
            new AnnotatedType(ApiError.class)
        );

        if (resolvedSchema == null || resolvedSchema.schema == null) {
            return;
        }

        components.addSchemas("ApiError", resolvedSchema.schema);
        resolvedSchema.referencedSchemas.forEach(components::addSchemas);
    }
}
