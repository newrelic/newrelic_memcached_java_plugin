## New Relic Platform Memcached Plugin Change Log ##

### v2.0.0 - April 7, 2014 ###

**Features**

* Support for the New Relic Platform Installer CLI tool `npi`

**Changes**

* Configuring the Memcached hosts that will be monitored has moved from the `memcached.hosts.json` file to the new standard `plugin.json` file.
* Setting the New Relic license key is now done in the new standard `newrelic.json` file
* Logging configuration has been simplified and is done in the `newrelic.json` file

### v1.0.1 - December 13, 2013 ###

** Bug Fixes **

* Modified 'percent miss' calculations to be more accurate for a time interval.

### v1.0.0 - November 25, 2013 ###

**Initial Release**

* Ability to monitor several key metrics from the Memcached service's 'getStats' API call.