package org.clematis.keycloak.config;

import org.clematis.keycloak.config.properties.KeycloakProperties;
import org.clematis.keycloak.config.properties.KeycloakServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Anton Troshin
 */
@Configuration
@EnableConfigurationProperties({KeycloakProperties.class, KeycloakServerProperties.class})
@ComponentScan(basePackageClasses = EmbeddedKeycloakConfig.class)
public class EmbeddedSpringKeycloakAutoConfiguration {
}
