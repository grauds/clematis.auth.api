package org.clematis.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

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

    private String contextPath = "/auth";
    private String realmImportFile = "clematis-realm.json";

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
