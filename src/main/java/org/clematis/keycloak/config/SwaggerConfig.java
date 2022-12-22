package org.clematis.keycloak.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
/**
 *
 * @author Anton Troshin
 */
@Configuration
public class SwaggerConfig {

    private final BuildProperties buildProperties;

    public SwaggerConfig(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(getInfo());
    }

    public Info getInfo() {
        return new Info().title("Clematis Keycloak Auth API")
                .description("")
                .version(buildProperties.getVersion())
                .description(buildProperties.getName());
    }

    @Bean
    public GroupedOpenApi api() {
        return GroupedOpenApi.builder()
                .group("Authentication")
                .pathsToMatch("/**")
                .build();
    }
}
