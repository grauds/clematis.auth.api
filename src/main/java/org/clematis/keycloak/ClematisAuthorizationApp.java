package org.clematis.keycloak;

import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

/**
 * @author Anton Troshin
 */
@SpringBootApplication(exclude = {
        LiquibaseAutoConfiguration.class,
        SecurityAutoConfiguration.class
    }, proxyBeanMethods = false)
public class ClematisAuthorizationApp {
    public static void main(String[] args){
        BasicConfigurator.configure();
        SpringApplication.run(ClematisAuthorizationApp.class, args);
    }
}
