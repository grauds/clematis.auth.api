package org.clematis.keycloak.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import lombok.Getter;
import lombok.Setter;
/**
 * @author Anton Troshin
 */
@ConfigurationProperties(prefix = "keycloak.server")
@Getter
@Setter
public class KeycloakServerProperties {

    private final AdminUser adminUser = new AdminUser();

    private final Infinispan infinispan = new Infinispan();

    private String contextPath = "/auth";

    /**
     * @author Anton Troshin
     */
    @Getter
    @Setter
    public static class Infinispan {

        Resource configLocation = new ClassPathResource("infinispan.xml");
    }

    /**
     * @author Anton Troshin
     */
    @Getter
    @Setter
    public static class AdminUser {
        String username = "admin";
        String password = "admin!";
    }
}
