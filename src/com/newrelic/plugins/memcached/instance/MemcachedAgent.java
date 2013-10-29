package com.newrelic.plugins.memcached.instance;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.spy.memcached.MemcachedClient;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.binding.Context;
import com.newrelic.metrics.publish.processors.EpochCounter;

public class MemcachedAgent extends Agent {

    private static final String GUID = "com.newrelic.plugins.memcached";
    private static final String VERSION = "0.1.0";

    private String name;
    private String host;
    private Integer port;

    // Processors
    private EpochCounter rusageUserCounter;
    private EpochCounter rusageSystemCounter;
    private EpochCounter totalConnectionsCounter;
    private EpochCounter cmdGetCounter;
    private EpochCounter cmdSetCounter;
    private EpochCounter cmdFlushCounter;
    private EpochCounter getHitsCounter;
    private EpochCounter getMissesCounter;
    private EpochCounter deleteHitsCounter;
    private EpochCounter deleteMissesCounter;
    private EpochCounter incrHitsCounter;
    private EpochCounter incrMissesCounter;
    private EpochCounter decrHitsCounter;
    private EpochCounter decrMissesCounter;
    private EpochCounter casHitsCounter;
    private EpochCounter casMissesCounter;
    private EpochCounter casBadValCounter;
    private EpochCounter bytesReadCounter;
    private EpochCounter bytesWrittenCounter;
    private EpochCounter totalItemsCounter;
    private EpochCounter evictionsCounter;
    private EpochCounter reclaimsCounter;

    final Logger logger; // Convenience variable for working with the logs

    public MemcachedAgent(String name, String host, Integer port) {
        super(GUID, VERSION);

        this.name = name;
        this.host = host;
        this.port = port;

        // Initialize Counters
        this.rusageUserCounter = new EpochCounter();
        this.rusageSystemCounter = new EpochCounter();
        this.totalConnectionsCounter = new EpochCounter();
        this.cmdGetCounter = new EpochCounter();
        this.cmdSetCounter = new EpochCounter();
        this.cmdFlushCounter = new EpochCounter();
        this.getHitsCounter = new EpochCounter();
        this.getMissesCounter = new EpochCounter();
        this.deleteHitsCounter = new EpochCounter();
        this.deleteMissesCounter = new EpochCounter();
        this.incrHitsCounter = new EpochCounter();
        this.incrMissesCounter = new EpochCounter();
        this.decrHitsCounter = new EpochCounter();
        this.decrMissesCounter = new EpochCounter();
        this.casHitsCounter = new EpochCounter();
        this.casMissesCounter = new EpochCounter();
        this.casBadValCounter = new EpochCounter();
        this.bytesReadCounter = new EpochCounter();
        this.bytesWrittenCounter = new EpochCounter();
        this.totalItemsCounter = new EpochCounter();
        this.evictionsCounter = new EpochCounter();
        this.reclaimsCounter = new EpochCounter();

        logger = Context.getLogger();				    				// Set logging to current Context
        logger.fine("Memcached Agent initialized: " + formatAgentParams(name, host, port));
    }

    @Override
    public String getComponentHumanLabel() {
        return name;
    }

    @Override
    public void pollCycle() {
        logger.fine("Gathering Memcached metrics. " + formatAgentParams(name, host, port));
        Map<String, String> metrics = getMetricsFromMemcached();

        if(metrics.size() == 0) {
            logger.severe("Could not fetch metrics from Memcached server.");
            return;
        }

        // Using the 'convertToXXXWithoutException helpers in case future versions of Memcached do not support a specific metric
        reportMetric("Rusage/User", "CPU", rusageUserCounter.process(convertStringToFloatWithoutException(metrics.get("rusage_user"))));
        reportMetric("Rusage/System", "CPU", rusageSystemCounter.process(convertStringToFloatWithoutException(metrics.get("rusage_system"))));
        reportMetric("Connections/Rate", "Connections/Seconds", totalConnectionsCounter.process(convertStringToFloatWithoutException(metrics.get("total_connections"))));
        reportMetric("Connections/Current", "Connections", convertStringToLongWithoutException(metrics.get("curr_connections")));
        reportMetric("Cmd/Get", "Commands/Seconds", cmdGetCounter.process(convertStringToFloatWithoutException(metrics.get("cmd_get"))));
        reportMetric("Cmd/Set", "Commands/Seconds", cmdSetCounter.process(convertStringToFloatWithoutException(metrics.get("cmd_set"))));
        reportMetric("Cmd/Flush", "Commands/Seconds", cmdFlushCounter.process(convertStringToFloatWithoutException(metrics.get("cmd_flush"))));
        reportMetric("Get/Hits", "Commands/Seconds", getHitsCounter.process(convertStringToFloatWithoutException(metrics.get("get_hits"))));
        reportMetric("Get/Misses", "Commands/Seconds", getMissesCounter.process(convertStringToFloatWithoutException(metrics.get("get_misses"))));
        reportMetric("Delete/Hits", "Commands/Seconds", deleteHitsCounter.process(convertStringToFloatWithoutException(metrics.get("delete_hits"))));
        reportMetric("Delete/Misses", "Commands/Seconds", deleteMissesCounter.process(convertStringToFloatWithoutException(metrics.get("delete_misses"))));
        reportMetric("Incr/Hits", "Commands/Seconds", incrHitsCounter.process(convertStringToFloatWithoutException(metrics.get("incr_hits"))));
        reportMetric("Incr/Misses", "Commands/Seconds", incrMissesCounter.process(convertStringToFloatWithoutException(metrics.get("incr_misses"))));
        reportMetric("Decr/Hits", "Commands/Seconds", decrHitsCounter.process(convertStringToFloatWithoutException(metrics.get("decr_hits"))));
        reportMetric("Decr/Misses", "Commands/Seconds", decrMissesCounter.process(convertStringToFloatWithoutException(metrics.get("decr_misses"))));
        reportMetric("Cas/Hits", "Commands/Seconds", casHitsCounter.process(convertStringToFloatWithoutException(metrics.get("cas_hits"))));
        reportMetric("Cas/Misses", "Commands/Seconds", casMissesCounter.process(convertStringToFloatWithoutException(metrics.get("cas_misses"))));
        reportMetric("Cas/Badval", "Commands/Seconds", casBadValCounter.process(convertStringToFloatWithoutException(metrics.get("cas_badval"))));
        reportMetric("Memory/Used", "Bytes", convertStringToLongWithoutException(metrics.get("bytes")));
        reportMetric("Memory/MaxAvailable", "Bytes", convertStringToLongWithoutException(metrics.get("limit_maxbytes")));
        reportMetric("Bytes/Read", "Bytes/Seconds", bytesReadCounter.process(convertStringToFloatWithoutException(metrics.get("bytes_read"))));
        reportMetric("Bytes/Written", "Bytes/Seconds", bytesWrittenCounter.process(convertStringToFloatWithoutException(metrics.get("bytes_written"))));
        reportMetric("Threads", "Threads", convertStringToLongWithoutException(metrics.get("threads")));
        reportMetric("Items/Current", "Items", convertStringToLongWithoutException(metrics.get("curr_items")));
        reportMetric("Items/Rate", "Items/Seconds", totalItemsCounter.process(convertStringToFloatWithoutException(metrics.get("total_items"))));
        reportMetric("Items/Evictions", "Evictions/Seconds", evictionsCounter.process(convertStringToFloatWithoutException(metrics.get("evictions"))));
        reportMetric("Items/Reclaims", "Reclaims/Seconds", reclaimsCounter.process(convertStringToFloatWithoutException(metrics.get("reclaimed"))));
    }

    private Map<String, String> getMetricsFromMemcached() {
        Map<String, String> map = new HashMap<String, String>();

        try {
            MemcachedClient client = new MemcachedClient(new InetSocketAddress(host, port));

            Map<SocketAddress, Map<String, String>> memcacheStats = client.getStats();

            if(memcacheStats.keySet().size() == 1) { // There should only be one server per client, but they only return stats through this API
                SocketAddress key = (SocketAddress)memcacheStats.keySet().toArray()[0];
                map = memcacheStats.get(key);
            }
        } catch(IOException e) {
            logger.severe("Error occured while trying to fetch metrics from the Memcached server at: " + host + ":" + port);
            e.printStackTrace();
        }

        return map;
    }

    private Number convertStringToLongWithoutException(String number) {
        try {
            Number longVal = convertStringToLongWithoutException(number);
            return longVal;
        }
        catch(NumberFormatException nfe) {
            return null;
        }
    }

    private Number convertStringToFloatWithoutException(String number) {
        try {
            Number floatVal = convertStringToFloatWithoutException(number);
            return floatVal;
        }
        catch(NumberFormatException nfe) {
            return null;
        }
    }

    /**
     * Format Agent parameters for logging
     * @param name
     * @param host
     * @param user
     * @param properties
     * @param metrics
     * @return A formatted String representing the Agent parameters
     */
    private String formatAgentParams(String name, String host, Integer port) {
        StringBuilder builder = new StringBuilder();
        builder.append("name: ").append(name).append(" | ");
        builder.append("host: ").append(host).append(":").append(port).append(" | ");
        return builder.toString();
    }
}
