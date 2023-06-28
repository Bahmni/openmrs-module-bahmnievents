OpenMRS Module Appointments Backend
=================================
This repository has the advice written on OpenMRSObject Services which can further used to generate and publish event to the configured JMS.
We are using JNDI JMS resource to connect.
We have tested against activeMQ JMS implementation as of now.
The format of the event payload is in OpenMRS format.

## Packaging
```mvn clean package```

### Prerequisite
    JDK 1.8
    ruby 2.2+
    RubyGems
    Compass 1.0.3 (gem install compass)

## Deploy

Copy ```openmrs-module-bahmnievents/omod/target/bahmni-events-1.0.0-SNAPSHOT.omod``` into OpenMRS modules directory and restart OpenMRS