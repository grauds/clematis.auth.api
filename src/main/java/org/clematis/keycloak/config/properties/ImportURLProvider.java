package org.clematis.keycloak.config.properties;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.exportimport.ImportProvider;
import org.keycloak.exportimport.Strategy;
import org.keycloak.exportimport.singlefile.SingleFileImportProvider;
import org.keycloak.exportimport.util.ExportImportSessionTask;
import org.keycloak.exportimport.util.ImportUtils;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.util.JsonSerialization;

import lombok.extern.slf4j.Slf4j;

/**
 * Implements import of resources from the jar file
 *
 * @author Anton Troshin
 */
@Slf4j
public class ImportURLProvider implements ImportProvider {

    private static final Logger LOG = Logger.getLogger(SingleFileImportProvider.class);

    // Allows to cache representation per provider to avoid parsing them twice
    protected Map<String, RealmRepresentation> realmReps;

    private final URL url;

    public ImportURLProvider(URL url) {
        this.url = url;
    }

    @Override
    public void importModel(KeycloakSessionFactory factory, final Strategy strategy) throws IOException {
        LOG.infof("Full importing from URL %s", this.url.toString());
        checkRealmReps();

        KeycloakModelUtils.runJobInTransaction(factory, new ExportImportSessionTask() {

            @Override
            protected void runExportImportTask(KeycloakSession session) {
                ImportUtils.importRealms(session, realmReps.values(), strategy);
            }

        });
    }

    @Override
    public boolean isMasterRealmExported() throws IOException {
        checkRealmReps();
        return (realmReps.containsKey(Config.getAdminRealm()));
    }

    protected void checkRealmReps() throws IOException {
        if (realmReps == null) {
            try (InputStream is = url.openStream()) {
                realmReps = ImportUtils.getRealmsFromStream(JsonSerialization.mapper, is);
            }
        }
    }

    @Override
    public void importRealm(KeycloakSessionFactory factory, String realmName, Strategy strategy) throws IOException {
        importModel(factory, strategy);
    }

    @Override
    public void close() {

    }
}
