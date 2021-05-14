package org.clematis.keycloak;

import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
/**
 * @author Anton Troshin
 */
@SpringBootApplication(exclude = LiquibaseAutoConfiguration.class, proxyBeanMethods = false)
public class ClematisAuthorizationApp {
    public static void main(String[] args){
        BasicConfigurator.configure();
        SpringApplication.run(ClematisAuthorizationApp.class, args);
    }
}
