package org.clematis.keycloak.config;

import java.util.Hashtable;
import java.util.Optional;

import javax.naming.CompositeName;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;
/**
 * @author Thomas Darimont
 */
public class KeycloakInitialContext extends InitialContext {

    private final Hashtable<Object, Object> environment = new Hashtable<>();

    public KeycloakInitialContext(Hashtable<?, ?> environment) throws NamingException {
        super(environment);
        this.environment.putAll(environment);
    }

    @Override
    public Object lookup(Name name) throws NamingException {
        return lookup(name.toString());
    }

    @Override
    public Object lookup(String name) throws NamingException {
        return Optional.ofNullable(environment.get(name))
                .orElseThrow(() -> new NamingException("Name " + name + " not found"));
    }

    @Override
    public NameParser getNameParser(String name) {
        return CompositeName::new;
    }
}
