package org.clematis.keycloak.config.properties;

import java.util.HashMap;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;
/**
 * @author Thomas Darimont
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakProperties extends HashMap<String, Object> {
}
