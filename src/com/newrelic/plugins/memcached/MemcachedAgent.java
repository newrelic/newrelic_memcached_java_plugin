package com.newrelic.plugins.memcached;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.spy.memcached.MemcachedClient;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.binding.Context;
import com.newrelic.metrics.publish.processors.EpochCounter;

public class MemcachedAgent extends Agent {

    private static final String GUID = "com.newrelic.plugins.memcached";
    private static final String VERSION = "1.0.1";

    private String name;
    private String host;
    private Integer port;

    // Processors
    private EpochCounter userCpuUsageCounter;
    private EpochCounter systemCpuUsageCounter;
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
        this.userCpuUsageCounter = new EpochCounter();
        this.systemCpuUsageCounter = new EpochCounter();
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

        logger = Context.getLogger(); // Set logging to current Context
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

        if(metrics.isEmpty()) {
            logger.severe("Could not fetch metrics from Memcached server.");
            return;
        }

        // Using the 'convertToXXXWithoutException helpers in case future versions of Memcached do not support a specific metric
        // System metrics
        Float userCpu = convertStringToFloatWithoutException(metrics.get("rusage_user"));
        Float systemCpu = convertStringToFloatWithoutException(metrics.get("rusage_system"));
        reportMetric("System/CPU/User", "seconds", userCpuUsageCounter.process(userCpu));
        reportMetric("System/CPU/System", "seconds", systemCpuUsageCounter.process(systemCpu));

        Long bytesUsed = convertStringToLongWithoutException(metrics.get("bytes"));
        Long bytesAvailable = convertStringToLongWithoutException(metrics.get("limit_maxbytes"));
        reportMetric("System/Summary/Memory/Bytes/Used", "bytes", bytesUsed);
        reportMetric("System/Summary/Memory/Bytes/MaxAvailable", "bytes", bytesAvailable);

        if(bytesAvailable != null && bytesUsed != null && bytesAvailable > 0) {
            reportMetric("System/Summary/Memory/Percent", "percent", (bytesUsed.floatValue() / bytesAvailable) * 100);
        }

        reportMetric("System/Threads", "threads", convertStringToLongWithoutException(metrics.get("threads")));
        reportMetric("System/Connections/Creation Rate", "connections/seconds", totalConnectionsCounter.process(convertStringToFloatWithoutException(metrics.get("total_connections"))));
        reportMetric("System/Summary/Connections/Count", "connections", convertStringToLongWithoutException(metrics.get("curr_connections")));
        reportMetric("System/Bytes/Read", "bytes/seconds", bytesReadCounter.process(convertStringToFloatWithoutException(metrics.get("bytes_read"))));
        reportMetric("System/Bytes/Written", "bytes/seconds", bytesWrittenCounter.process(convertStringToFloatWithoutException(metrics.get("bytes_written"))));

        // CacheUse metrics
        reportMetric("CacheUse/Cmd/Gets", "commands/seconds", cmdGetCounter.process(convertStringToFloatWithoutException(metrics.get("cmd_get"))));
        reportMetric("CacheUse/Cmd/Sets", "commands/seconds", cmdSetCounter.process(convertStringToFloatWithoutException(metrics.get("cmd_set"))));
        reportMetric("CacheUse/Cmd/Flushes", "commands/seconds", cmdFlushCounter.process(convertStringToFloatWithoutException(metrics.get("cmd_flush"))));

        Float getHits = convertStringToFloatWithoutException(metrics.get("get_hits"));
        Float getMisses = convertStringToFloatWithoutException(metrics.get("get_misses"));
        reportCacheUseMetrics("Get", getHits, getMisses, getHitsCounter, getMissesCounter);

        Float deleteHits = convertStringToFloatWithoutException(metrics.get("delete_hits"));
        Float deleteMisses = convertStringToFloatWithoutException(metrics.get("delete_misses"));
        reportCacheUseMetrics("Delete", deleteHits, deleteMisses, deleteHitsCounter, deleteMissesCounter);

        Float incrHits = convertStringToFloatWithoutException(metrics.get("incr_hits"));
        Float incrMisses = convertStringToFloatWithoutException(metrics.get("incr_misses"));
        reportCacheUseMetrics("Incr", incrHits, incrMisses, incrHitsCounter, incrMissesCounter);

        Float decrHits = convertStringToFloatWithoutException(metrics.get("decr_hits"));
        Float decrMisses = convertStringToFloatWithoutException(metrics.get("decr_misses"));
        reportCacheUseMetrics("Decr", decrHits, decrMisses, decrHitsCounter, decrMissesCounter);

        Float casHits = (Float)casHitsCounter.process(convertStringToFloatWithoutException(metrics.get("cas_hits")));
        Float casMisses = (Float)casMissesCounter.process(convertStringToFloatWithoutException(metrics.get("cas_misses")));
        Float casBadVal = (Float)casBadValCounter.process(convertStringToFloatWithoutException(metrics.get("cas_badval")));
        reportMetric("CacheUse/Cas/Actions/Hits", "commands/seconds", casHits);
        reportMetric("CacheUse/Cas/Actions/Misses", "commands/seconds", casMisses);
        reportMetric("CacheUse/Cas/Actions/Badval", "commands/seconds", casBadVal);

        if(casHits != null && casMisses != null && casBadVal != null) {
            Float casMissesAndBadVal = (casHits > 0 || casMisses > 0 || casBadVal > 0) ? ((casMisses + casBadVal) / (casHits + casMisses + casBadVal)) * 100 : 0;
            reportMetric("CacheUse/Summary/Cas/MissedBadval", "percent", casMissesAndBadVal);
        }

        // Item metrics
        Long currentItems = convertStringToLongWithoutException(metrics.get("curr_items"));
        reportMetric("Items/Count", "items", currentItems);
        reportMetric("Items/AvgSize", "bytes", (currentItems != null && currentItems > 0) ? bytesUsed / currentItems : 0);
        reportMetric("Items/Rate", "items/seconds", totalItemsCounter.process(convertStringToFloatWithoutException(metrics.get("total_items"))));
        reportMetric("Items/Actions/Evictions", "evictions/seconds", evictionsCounter.process(convertStringToFloatWithoutException(metrics.get("evictions"))));
        reportMetric("Items/Actions/Reclaims", "reclaims/seconds", reclaimsCounter.process(convertStringToFloatWithoutException(metrics.get("reclaimed"))));
    }

    private void reportCacheUseMetrics(String name, Float hits, Float misses, EpochCounter hitCounter, EpochCounter missCounter) {
        Float processedHits = (Float)hitCounter.process(hits);
        Float processedMisses = (Float)missCounter.process(misses);

        reportMetric(String.format("CacheUse/%s/Actions/Hits", name), "commands/seconds", processedHits);
        reportMetric(String.format("CacheUse/%s/Actions/Misses", name), "commands/seconds", processedMisses);

        if(processedHits != null && processedMisses != null) {
            Float percentMisses = (processedHits > 0 || processedMisses > 0) ? (processedMisses / (processedHits + processedMisses)) * 100 : 0;
            reportMetric(String.format("CacheUse/Summary/%s/Missed", name), "percent", percentMisses);
        }
    }

    private Map<String, String> getMetricsFromMemcached() {
        Map<String, String> map = new HashMap<String, String>();
        MemcachedClient client = null;

        try {
            client = new MemcachedClient(new InetSocketAddress(host, port));
            Map<SocketAddress, Map<String, String>> memcacheStats = client.getStats();
            if(memcacheStats.keySet().size() == 1) { // There should only be one server per client, but they only return stats through this API
                SocketAddress key = (SocketAddress)memcacheStats.keySet().toArray()[0];
                map = memcacheStats.get(key);
            }
        } catch(IOException e) {
            logger.log(Level.SEVERE, "Error occured while trying to fetch metrics from the Memcached server at: " + host + ":" + port, e);
            e.printStackTrace();
        } catch(IllegalStateException e) {
            logger.log(Level.SEVERE, "Error occured while trying to fetch metrics from the Memcached server at: " + host + ":" + port, e);
            e.printStackTrace();
        }
        finally {
            if(client != null) {
                client.shutdown();
            }
        }

        return map;
    }

    private Long convertStringToLongWithoutException(String number) {
        try {
            // This will safeguard against metrics that vary per version
            if(number == null) {
                return null;
            }

            Long longVal = Long.parseLong(number);
            return longVal;
        }
        catch(NumberFormatException nfe) {
            return null;
        }
    }

    private Float convertStringToFloatWithoutException(String number) {
        try {
            // This will safeguard against metrics that vary per version
            if(number == null) {
                return null;
            }

            Float floatVal = Float.parseFloat(number);
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
