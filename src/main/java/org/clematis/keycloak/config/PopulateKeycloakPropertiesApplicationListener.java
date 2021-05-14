package org.clematis.keycloak.config;

import java.io.IOException;
import java.util.List;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;

import lombok.extern.slf4j.Slf4j;
/**
 * Loads the default keycloak configuration into the environment.
 * @author Thomas Darimont
 */
@Slf4j
public class PopulateKeycloakPropertiesApplicationListener implements ApplicationListener<ApplicationEvent> {

    public static final String KEYCLOAK_DEFAULTS_YML = "keycloak-defaults.yml";

    @Override
    public void onApplicationEvent(@NonNull ApplicationEvent event) {

        if (event instanceof ApplicationEnvironmentPreparedEvent) {

            ApplicationEnvironmentPreparedEvent envEvent = (ApplicationEnvironmentPreparedEvent) event;
            ConfigurableEnvironment env = envEvent.getEnvironment();

            try {
                Resource resource = new ClassPathResource(KEYCLOAK_DEFAULTS_YML);

                if (!resource.exists()) {
                    return;
                }

                log.info("Loading default keycloak properties configuration from: {}", resource.getURI());

                YamlPropertySourceLoader sourceLoader = new YamlPropertySourceLoader();
                List<PropertySource<?>> yamlTestProperties = sourceLoader.load(KEYCLOAK_DEFAULTS_YML, resource);
                if (!yamlTestProperties.isEmpty()) {
                    env.getPropertySources().addLast(yamlTestProperties.get(0));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
