package com.mycompany.server;

import com.mycompany.server.resources.BaseResource;
import com.mycompany.server.resources.ImageResource;
import com.mycompany.server.resources.PingResource;
import com.mycompany.server.resources.UserResource;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Configures JAX-RS for the application.
 *
 * @author Juneau
 */
@ApplicationPath("api")
public class Configuration extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("jersey.config.server.provider.classnames", "org.glassfish.jersey.media.multipart.MultiPartFeature");
        return properties;
    }


    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(com.mycompany.server.resources.AdminResource.class);
        resources.add(com.mycompany.server.resources.ImageResource.class);
        resources.add(com.mycompany.server.resources.PingResource.class);
        resources.add(com.mycompany.server.resources.UserResource.class);
    }

}
