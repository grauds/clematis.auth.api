package org.clematis.keycloak.config;

import java.util.Map;

import org.jboss.resteasy.core.ResteasyContext;
import org.jboss.resteasy.spi.Dispatcher;
import org.keycloak.common.util.ResteasyProvider;

import com.google.auto.service.AutoService;

/**
 * Adapter for the upgraded version of resteasy
 * @author Thomas Darimont
 */
@AutoService(ResteasyProvider.class)
public class Resteasy4Provider implements ResteasyProvider {

    @Override
    public <R> R getContextData(Class<R> type) {
        return ResteasyContext.getContextData(type);
    }

    @Override
    public void pushDefaultContextObject(Class type, Object instance) {
        Dispatcher dispatcher = ResteasyContext.getContextData(Dispatcher.class);
        Map<Class, Object> defaultContextObjects = dispatcher.getDefaultContextObjects();
        defaultContextObjects.put(type, instance);
    }

    @Override
    public void pushContext(Class type, Object instance) {
        ResteasyContext.pushContext(type, instance);
    }

    @Override
    public void clearContextData() {
        ResteasyContext.clearContextData();
    }

}
