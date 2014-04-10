Memcached Java Plugin for New Relic
========================================

Prerequisites
-------------
- A New Relic account. Signup for a free account at [http://newrelic.com](http://newrelic.com)
- A server running Memcached v1.4 or greater. Download the latest version of Memcached for free [here](https://code.google.com/p/memcached/downloads/list).
- A configured Java Runtime (JRE) environment Version 1.6 or better

Installation
-------------

The Memcached plugin can be [installed manually](#running-the-agent) or automatically with [Chef](http://www.getchef.com) and [Puppet](http://puppetlabs.com) or with the [New Relic Platform Installer](new-relic-platform-installer-beta). For Chef and Puppet support see the New Relic plugin's [Chef Cookbook](http://community.opscode.com/cookbooks/newrelic_plugins) and [Puppet Module](https://forge.puppetlabs.com/newrelic/newrelic_plugins).

Additional information on using Chef and Puppet with New Relic is available in New Relic's [documentation](https://docs.newrelic.com/docs/plugins/plugin-installation-with-chef-and-puppet).

Running the Agent
----------------------------------
  
1. Download the latest `newrelic_memcached_plugin-X.Y.Z.tar.gz` from [https://github.com/newrelic-platform/newrelic_memcached_java_plugin/tree/master/dist](https://github.com/newrelic-platform/newrelic_memcached_java_plugin/tree/master/dist)
2. Extract the downloaded archive to the location you want to run the example agent from
3. Copy `config/newrelic.template.json` to `config/newrelic.json`
4. Edit `config/newrelic.json` and replace "YOUR_LICENSE_KEY_HERE" with your New Relic license key
5. Copy `config/plugin.template.json` to `config/plugin.json`
6. Edit `config/plugin.json` to point to your instances of Memcached. You can add as many hosts as you'd like If your Memcached instances are bound to an external IP, use that value for the host field.  If you omit the 'port' field it will default to '11211'
5. From your shell run: `java -jar plugin.jar`
6. Wait a few minutes for New Relic to begin processing the data sent from your agent.
6. Log into your New Relic account at [http://newrelic.com](http://newrelic.com) and click on `Memcached` on the left hand nav bar to start seeing your Memcached metrics

## New Relic Platform Installer (Beta)

The New Relic Platform Installer (NPI) is a simple, lightweight command line tool that helps you easily download, configure and manage New Relic Platform Plugins.  If you're interested in participating in our public beta, simply go to [our forum category](https://discuss.newrelic.com/category/platform-plugins/platform-installer-beta) and checkout the ['Getting Started' section](https://discuss.newrelic.com/t/getting-started-for-the-platform-installer-beta/842).  If you have any questions, concerns or feedback, please do not hesitate to reach out through the forums as we greatly appreciate your feedback!

Once you've installed the NPI tool, run the following command:

	./npi install com.newrelic.plugins.memcached


Source Code
-----------

This plugin can be found at [https://github.com/newrelic-platform/newrelic_memcached_java_plugin](https://github.com/newrelic-platform/newrelic_memcached_java_plugin)
