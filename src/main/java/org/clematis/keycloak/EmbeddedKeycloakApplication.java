package org.clematis.keycloak;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.clematis.keycloak.config.SpringBootConfigProvider;
import org.clematis.keycloak.config.properties.KeycloakServerProperties;
import org.keycloak.Config;
import org.keycloak.exportimport.ExportImportConfig;
import org.keycloak.exportimport.ExportImportManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakTransactionManager;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.ApplianceBootstrap;
import org.keycloak.services.resources.KeycloakApplication;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Anton Troshin
 */
@Slf4j
public class EmbeddedKeycloakApplication extends KeycloakApplication {

    private final KeycloakServerProperties keycloakServerProperties;

    public EmbeddedKeycloakApplication(@Context ServletContext context) {
        this.keycloakServerProperties = WebApplicationContextUtils
                .getRequiredWebApplicationContext(context)
                .getBean(KeycloakServerProperties.class);
    }

    protected void loadConfig() {
        Config.init(SpringBootConfigProvider.getInstance());
    }

    @Override
    protected ExportImportManager bootstrap() {
        ExportImportManager exportImportManager = super.bootstrap();
        createMasterRealmAdminUser();
        importTestRealm();
        return exportImportManager;
    }

    private void createMasterRealmAdminUser() {

        KeycloakServerProperties.AdminUser adminUser = keycloakServerProperties.getAdminUser();

        String username = adminUser.getUsername();
        if (!(StringUtils.hasLength(username) || StringUtils.hasText(username))) {
            return;
        }

        KeycloakSession session = getSessionFactory().create();
        KeycloakTransactionManager transaction = session.getTransactionManager();
        try {
            transaction.begin();

            boolean randomPassword = false;
            String password = adminUser.getPassword();
            if (StringUtils.isEmpty(adminUser.getPassword())) {
                password = UUID.randomUUID().toString();
                randomPassword = true;
            }
            new ApplianceBootstrap(session).createMasterRealmUser(username, password);
            if (randomPassword) {
                log.info("Generated admin password: {}", password);
            }
            ServicesLogger.LOGGER.addUserSuccess(username, Config.getAdminRealm());

            transaction.commit();
        } catch (IllegalStateException e) {
            transaction.rollback();
            ServicesLogger.LOGGER.addUserFailedUserExists(username, Config.getAdminRealm());
        } catch (Throwable t) {
            transaction.rollback();
            ServicesLogger.LOGGER.addUserFailed(t, username, Config.getAdminRealm());
        } finally {
            session.close();
        }
    }

    protected void importTestRealm() {

        KeycloakServerProperties.Migration imex = keycloakServerProperties.getMigration();
        Resource importLocation = imex.getImportLocation();

        if (!importLocation.exists()) {
            log.info("Could not find keycloak import file {}", importLocation);
            return;
        }

        File file;
        try {
            file = importLocation.getFile();

            log.info("Starting Keycloak realm configuration import from location: {}", importLocation);

            KeycloakSession session = getSessionFactory().create();

            ExportImportConfig.setAction("import");
            ExportImportConfig.setProvider(imex.getImportProvider());
            ExportImportConfig.setFile(file.getAbsolutePath());

            ExportImportManager manager = new ExportImportManager(session);
            manager.runImport();

            session.close();

            log.info("Keycloak realm configuration import finished.");

        } catch (IOException e) {
            log.error("Could not read keycloak import file {}", importLocation, e);
        }
    }
}
