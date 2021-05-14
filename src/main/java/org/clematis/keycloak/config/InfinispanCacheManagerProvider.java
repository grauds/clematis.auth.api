package org.clematis.keycloak.config;

import javax.naming.NamingException;

import org.infinispan.manager.DefaultCacheManager;
import org.keycloak.Config;
import org.keycloak.cluster.ManagedCacheManagerProvider;
import org.springframework.jndi.JndiTemplate;

import com.google.auto.service.AutoService;
/**
 * @author Thomas Darimont
 */
@AutoService(ManagedCacheManagerProvider.class)
public class InfinispanCacheManagerProvider implements ManagedCacheManagerProvider {

    @Override
    @SuppressWarnings("unchecked")
    public <C> C getCacheManager(Config.Scope config) {
        try {
            return (C) new JndiTemplate().lookup(DynamicJndiContextFactoryBuilder.JNDI_CACHE_MANAGAER,
                    DefaultCacheManager.class);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }
}
