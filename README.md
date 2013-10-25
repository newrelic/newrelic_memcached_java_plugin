Memcached Java Plugin for New Relic
========================================

Prerequisites
-------------
1. A New Relic account. Signup for a free account at http://newrelic.com
2. A configured Java Developer Kit (JDK) - version 1.6 or better

Running the Agent
----------------------------------
	
1. Download the latest `newrelic_memcached_plugin-X.Y.Z.tar.gz` from https://github.com/newrelic-platform/newrelic_memcached_java_plugin/tree/master/dist by clicking on the file name and selecting `Raw` from the gray menu bar
2. Extract the downloaded archive to the location you want to run the example agent from
3. Copy `config/template_newrelic.properties` to `config/newrelic.properties`
4. Edit `config/newrelic.properties` and replace "YOUR_LICENSE_KEY_HERE" with your New Relic license key
5. Copy `config/template_memcached.hosts.json` to `config/memcached.hosts.json`
6. Edit `config/memcached.hosts.json` to point to your instances of Memcached. You can add as many hosts as you'd like If your Memcached instances are bound to an external IP, use that value for the host field.  If you omit the 'port' field it will default to '11211'
5. From your shell run: `java -jar newrelic_memcached_plugin-*.jar`
6. Wait a few minutes for New Relic to begin processing the data sent from your agent.
6. Login into your New Relic account at http://newrelic.com and click on `Memcached` on the left hand nav bar to start seeing your Memcached metrics

Source Code
-----------

This plugin can be found at https://github.com/newrelic-platform/newrelic_memcached_java_plugin