package org.clematis.keycloak.service;

import java.net.MalformedURLException;
import java.net.URL;

import org.clematis.keycloak.config.properties.ImportURLProvider;
import org.keycloak.Config;
import org.keycloak.exportimport.ExportImportConfig;
import org.keycloak.exportimport.ImportProvider;
import org.keycloak.exportimport.ImportProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * Factory to register jar importer
 *
 * @author Anton Troshin
 */
public class ImportURLProviderFactory implements ImportProviderFactory {

    public static final String PROVIDER_ID = "jarFile";

    @Override
    public ImportProvider create(KeycloakSession session) {
        String fileName = ExportImportConfig.getFile();
        if (fileName == null) {
            throw new IllegalArgumentException("Property " + ExportImportConfig.FILE + " needs to be provided!");
        }
        try {
            return new ImportURLProvider(new URL(fileName));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(fileName + " is not a valid URL");
        }
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
