package org.clematis.keycloak.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Anton Troshin
 */
@Configuration
public class SwaggerConfig {

    public SwaggerConfig() {
    }

    @Bean
    public GroupedOpenApi api() {
        return GroupedOpenApi.builder()
                .group("Clematis-Auth-API")
                .pathsToMatch("/**")
                .build();
    }
}
