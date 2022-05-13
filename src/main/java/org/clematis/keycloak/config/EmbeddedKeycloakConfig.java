package org.clematis.keycloak.config;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.clematis.keycloak.EmbeddedKeycloakApplication;
import org.clematis.keycloak.EmbeddedKeycloakServer;
import org.clematis.keycloak.config.properties.KeycloakProperties;
import org.clematis.keycloak.config.properties.KeycloakServerProperties;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.manager.DefaultCacheManager;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters;
import org.keycloak.platform.Platform;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Anton Troshin
 */
@Slf4j
@Configuration
public class EmbeddedKeycloakConfig {

    public static final String PATH_WILD_CARD = "/*";
    public static final int THREADS = 5;

    @Bean
    @ConditionalOnMissingBean(name = "embeddedKeycloakServer")
    protected EmbeddedKeycloakServer embeddedKeycloakServer(ServerProperties serverProperties) {
        return new EmbeddedKeycloakServer(serverProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "springBootPlatform")
    protected SpringBootPlatformProvider springBootPlatform() {
        return (SpringBootPlatformProvider) Platform.getPlatform();
    }

    @Bean
    @ConditionalOnMissingBean(name = "springBootConfigProvider")
    protected SpringBootConfigProvider springBootConfigProvider(KeycloakProperties keycloakProperties) {
        return new SpringBootConfigProvider(keycloakProperties);
    }

    @Bean("fixedThreadPool")
    public ExecutorService fixedThreadPool() {
        return Executors.newFixedThreadPool(THREADS);
    }

    @Bean
    @ConditionalOnMissingBean(name = "springBeansJndiContextFactory")
    protected DynamicJndiContextFactoryBuilder
                 springBeansJndiContextFactory(DataSource dataSource,
                                               DefaultCacheManager cacheManager,
                                               @Qualifier("fixedThreadPool") ExecutorService executorService) {
        return new DynamicJndiContextFactoryBuilder(dataSource, cacheManager, executorService);
    }

    @Bean
    @ConditionalOnMissingBean(name = "keycloakInfinispanCacheManager")
    protected DefaultCacheManager keycloakInfinispanCacheManager(KeycloakServerProperties keycloakServerProperties)
            throws IOException {

        KeycloakServerProperties.Infinispan infinispan = keycloakServerProperties.getInfinispan();
        Resource configLocation = infinispan.getConfigLocation();
        log.info("Using infinispan configuration from {}", configLocation.getURI());

        ConfigurationBuilderHolder configBuilder = new ParserRegistry().parse(configLocation.getURL());
        DefaultCacheManager defaultCacheManager = new DefaultCacheManager(configBuilder, false);
        defaultCacheManager.start();
        return defaultCacheManager;
    }

    @Bean
    @ConditionalOnMissingBean(name = "keycloakJaxRsApplication")
    ServletRegistrationBean<HttpServlet30Dispatcher>
        keycloakJaxRsApplication(KeycloakServerProperties keycloakServerProperties) {

        ServletRegistrationBean<HttpServlet30Dispatcher> servlet
                = new ServletRegistrationBean<>(new HttpServlet30Dispatcher());

        servlet.addInitParameter("javax.ws.rs.Application", EmbeddedKeycloakApplication.class.getName());

        servlet.addInitParameter("resteasy.allowGzip", Boolean.FALSE.toString());
        servlet.addInitParameter("keycloak.embedded", Boolean.TRUE.toString());
        servlet.addInitParameter(ResteasyContextParameters.RESTEASY_EXPAND_ENTITY_REFERENCES, Boolean.FALSE.toString());
        servlet.addInitParameter(ResteasyContextParameters.RESTEASY_SECURE_PROCESSING_FEATURE, Boolean.TRUE.toString());
        servlet.addInitParameter(ResteasyContextParameters.RESTEASY_DISABLE_DTDS, Boolean.TRUE.toString());
        servlet.addInitParameter(ResteasyContextParameters.RESTEASY_SERVLET_MAPPING_PREFIX,
                keycloakServerProperties.getContextPath());
        servlet.addInitParameter(ResteasyContextParameters.RESTEASY_USE_CONTAINER_FORM_PARAMS,
                Boolean.FALSE.toString());
        servlet.addInitParameter(ResteasyContextParameters.RESTEASY_DISABLE_HTML_SANITIZER, Boolean.TRUE.toString());
        servlet.addUrlMappings(keycloakServerProperties.getContextPath() + PATH_WILD_CARD);

        servlet.setLoadOnStartup(2);
        servlet.setAsyncSupported(true);

        return servlet;
    }

    @Bean
    @ConditionalOnMissingBean(name = "keycloakSessionManagement")
    FilterRegistrationBean<EmbeddedKeycloakRequestFilter> keycloakSessionManagement(
            KeycloakServerProperties keycloakServerProperties) {

        FilterRegistrationBean<EmbeddedKeycloakRequestFilter> filter = new FilterRegistrationBean<>();
        filter.setName("Keycloak Session Management");
        filter.setFilter(new EmbeddedKeycloakRequestFilter());
        filter.addUrlPatterns(keycloakServerProperties.getContextPath() + PATH_WILD_CARD);

        return filter;
    }
}
