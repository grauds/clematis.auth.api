package org.clematis.keycloak;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import static org.keycloak.exportimport.ExportImportConfig.STRATEGY;
import static org.keycloak.exportimport.Strategy.IGNORE_EXISTING;
import static org.springframework.util.StringUtils.hasText;
import org.clematis.keycloak.config.SpringBootConfigProvider;
import org.clematis.keycloak.config.properties.KeycloakServerProperties;
import org.keycloak.Config;
import org.keycloak.common.crypto.CryptoIntegration;
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
        CryptoIntegration.init(this.getClass().getClassLoader());
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
        importClematisRealm();
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
            if (hasText(adminUser.getPassword())) {
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

    protected void importClematisRealm() {

        KeycloakServerProperties.Migration imex = keycloakServerProperties.getMigration();
        Resource importLocation = imex.getImportLocation();

        if (!importLocation.exists()) {
            log.info("Could not find keycloak import file {}", importLocation);
            return;
        }

        try {

            log.info("Starting Keycloak realm configuration import from location: {}", importLocation);

            KeycloakSession session = getSessionFactory().create();

            ExportImportConfig.setAction("import");
            ExportImportConfig.setProvider(imex.getImportProvider());

            URL file = importLocation.getURL();
            ExportImportConfig.setFile(file.toString());

            System.setProperty(STRATEGY, IGNORE_EXISTING.toString());

            ExportImportManager manager = new ExportImportManager(session);
            manager.runImport();

            session.close();

            log.info("Keycloak realm configuration import finished.");

        } catch (IOException e) {
            log.error("Could not read keycloak import file {}", importLocation, e);
        }
    }
}
