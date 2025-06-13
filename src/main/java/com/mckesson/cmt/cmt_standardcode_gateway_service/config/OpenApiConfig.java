package com.mckesson.cmt.cmt_standardcode_gateway_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${okta.config.baseurl}")
    private String oktaBaseUrl;

    @Value("${okta.config.authserverid}")
    private String authServerId;

    @Bean
    public OpenAPI customOpenAPI() {
        final String oauth2SchemeName = "oauth2";
        final String bearerSchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Standard Codes Gateway API")
                        .description(
                                "API for managing standard codes in the Content Management Tool. **Authentication is required for all endpoints including this documentation.**")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("McKesson Support")
                                .email("support@mckesson.com"))
                        .license(new License()
                                .name("McKesson License")
                                .url("https://mckesson.com")))
                // Add security requirements - Bearer token is preferred for API testing
                .addSecurityItem(new SecurityRequirement().addList(bearerSchemeName))
                .addSecurityItem(new SecurityRequirement().addList(oauth2SchemeName))
                .components(new Components()
                        // Bearer Token Security Scheme (primary - easier for testing)
                        .addSecuritySchemes(bearerSchemeName, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Authorization header using the Bearer scheme. " +
                                        "Obtain token from Okta and enter as: Bearer {your-jwt-token}"))
                        // OAuth2 Security Scheme (secondary)
                        .addSecuritySchemes(oauth2SchemeName, new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .description("Okta OAuth2 authentication")
                                .flows(new OAuthFlows()
                                        .clientCredentials(new OAuthFlow()
                                                .tokenUrl(oktaBaseUrl + "/oauth2/" + authServerId + "/v1/token")
                                                .scopes(new io.swagger.v3.oas.models.security.Scopes()
                                                        .addString("ccmt:api", "CMT API access")
                                                        .addString("openid", "OpenID Connect")))
                                        .authorizationCode(new OAuthFlow()
                                                .authorizationUrl(
                                                        oktaBaseUrl + "/oauth2/" + authServerId + "/v1/authorize")
                                                .tokenUrl(oktaBaseUrl + "/oauth2/" + authServerId + "/v1/token")
                                                .scopes(new io.swagger.v3.oas.models.security.Scopes()
                                                        .addString("ccmt:api", "CMT API access")
                                                        .addString("openid", "OpenID Connect")
                                                        .addString("profile", "User profile")
                                                        .addString("email", "User email"))))));
    }
}