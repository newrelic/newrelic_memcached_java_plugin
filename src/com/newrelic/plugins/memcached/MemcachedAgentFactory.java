package com.newrelic.plugins.memcached;

import java.util.Map;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;

public class MemcachedAgentFactory extends AgentFactory {
    /**
     * Construct an Agent Factory based on the default properties file
     */
    public MemcachedAgentFactory() {
        super("memcached.hosts.json");
    }

    @Override
    public Agent createConfiguredAgent(Map<String, Object> properties) {
        String name = (String) properties.get("name");
        String host = (String) properties.get("host");
        Object portObj = properties.get("port");

        Integer port = null;

        if(portObj != null) {
            port = ((Long)portObj).intValue();
        }
        else {
            port = 11211; // default port for Memcached
        }

        return new MemcachedAgent(name, host, port);
    }
}
