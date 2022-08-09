package org.clematis.keycloak.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jmx.support.ConnectorServerFactoryBean;

/**
 * JMXMP configuration in the Spring Boot app
 * @author Anton Troshin
 */
@Configuration
public class ConnectorServiceFactoryBeanProvider {

    @Value("${spring.jmx.url}")
    private String url;

    @Bean
    public ConnectorServerFactoryBean connectorServerFactoryBean() {
        final ConnectorServerFactoryBean connectorServerFactoryBean = new ConnectorServerFactoryBean();
        connectorServerFactoryBean.setServiceUrl(url);
        return connectorServerFactoryBean;
    }
}
