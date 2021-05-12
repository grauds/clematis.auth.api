package org.clematis.auth.config;

import org.keycloak.platform.PlatformProvider;
import org.keycloak.services.ServicesLogger;
/**
 * @author Anton Troshin
 */
public class SimplePlatformProvider implements PlatformProvider {

    @Override
    public void onStartup(Runnable startupHook) {
        startupHook.run();
    }

    @Override
    public void onShutdown(Runnable shutdownHook) {

    }

    @Override
    public void exit(Throwable cause) {
        ServicesLogger.LOGGER.fatal(cause);
    }
}
