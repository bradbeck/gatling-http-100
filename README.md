<!--

    Copyright (c) 2019-present Sonatype, Inc. All rights reserved.

    This program is licensed to you under the Apache License Version 2.0,
    and you may not use this file except in compliance with the Apache License Version 2.0.
    You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.

    Unless required by applicable law or agreed to in writing,
    software distributed under the Apache License Version 2.0 is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.

-->
# Table of Contents
* [Description](#description)
* [NXRM3 Direct](#nxrm3-direct)
* [NXRM3 Proxy](#nxrm3-proxy)
* [NXRM3 Direct Manual](#nxrm3-direct-manual)
* [NXRM3 Proxy Manual](#nxrm3-proxy-manual)

## Description
This project is meant to exhibit some observed behavior when running Gatling 3 tests against Nexus Repository Manager 3 (NXRM3) directly and when NXRM3 is behind a reverse proxy (i.e. nginx).

The behavior of particular interest is the handling of HTTP 100 responses when a reverse proxy is in play. Similar simulations in Gatling 2 resulted in the expected behavior, but with Gatling 3 we now see many request failures of the following form:
```
12:58:46.568 [gatling-http-1-5][WARN ][StatsProcessor.scala:114] i.g.h.e.r.DefaultStatsProcessor - Request 'PUT release jar' failed for user 4: status.find.is(201), but actually found 100
```

A number of Gatling simulations have written to exhibit behavior both directly against NXRM3 and when NXRM3 is behind nginx. The tests rely and starting docker containers to create the appropriate application environment to be tested. As such, there are several simulation implementations to allow different configurations to be tested independently.

There is also the option of provisioning the docker containers as part of the simulation via the `testcontainers` library. Or, the containers can be provisioned manually with `docker-compose` and the simulation can be run against this manual configuation.

The following sections will outline how to execute the various scenarios individual.

## NXRM3 Direct
This scenario (`MavenDirectSimulation`) provisions an NXRM3 docker container as part of the simulation and then executes a Maven deploy workload against that instance. The docker container will be stopped and removed after the simulation has completed.

The results should show that the deployment requests complete successfully.

This simulation can be executed with the following Maven command:
```
mvn verify -P directly
```

## NXRM3 Proxy
This scenario (`MavenProxySimulation`) provisions an NXRM3 container and an nginx container. Nginx acts as a reverse proxy for NXRM3 and simulation requests are sent to nginx with forwards them onto NXRM3. The same Maven deploy workload is used for this simulation. The docker containers will both be stopped and removed after the simulation has completed.

The results should show many request failures of the following form:
```
12:58:46.654 [gatling-http-1-5][WARN ][StatsProcessor.scala:114] i.g.h.e.r.DefaultStatsProcessor - Request 'PUT release pom' failed for user 4: status.find.is(201), but actually found 100
```

This simulation can be executed with the following Maven command:
```
mvn verify -P Proxy
```

This setup currently takes advantage of observed docker port number to correctly configure the nginx proxy. It's not clear that this behavior is universal and may not work in all environments. Basically it assumes that the mapped port for the nginx container with be 1 more than the mapped port for the NXRM3 container.

If this does not work in your environment, the following manual provision options has been provided.

## NXRM3 Direct Manual
This scenario assumes that you have manually provisioned NXRM3 with the docker-compose configuration provided (`direct/docker-compose.yml`).

The results of this simulation should be the same as the "NXRM3 Direct" scenario.

The execution of this simulation involves 3 steps:
1. Provision NXRM3 via docker-compose
In a separate terminal execute the following in the `direct` subdirectory:
```
docker-compose up
```
Then wait for NXRM3 to completely startup. This will be evidenced by the following banner in docker-compose logging:
```
nxrm    | -------------------------------------------------
nxrm    |
nxrm    | Started Sonatype Nexus OSS 3.15.1-01
nxrm    |
nxrm    | -------------------------------------------------
```
2. Execute the MavenManualSimulation
In a separate terminal execute the following Maven command:
```
mvn verify -P manual
```
3. Tear Down NXRM3 via docker-compose
In a separate terminal execute the following in the `direct` subdirectory:
```
docker-compose stop
docker-compose rm -f
```
This will stop and remove the NXRM3 docker container.

## NXRM3 Proxy Manual
This scenario assumes that you have manually provisioned NXRM3 and nginx with the docker-compose configuration provided (`proxy/docker-compose.yml`).

The results of this simulation should be the same as the "NXRM3 Proxy" scenario.

The execution of this simulation involves 3 steps:
1. Provision NXRM3 and nginx via docker-compose
In a separate terminal execute the following in the `proxy` subdirectory:
```
docker-compose up
```
Then wait for NXRM3 and nginx to completely startup. This will be evidenced by the following in docker-compose logging:
```
nxrm     | -------------------------------------------------
nxrm     |
nxrm     | Started Sonatype Nexus OSS 3.15.1-01
nxrm     |
nxrm     | -------------------------------------------------
...
nginx    | 2019/01/18 19:57:46 Received 200 from http://nxrm:8081
```
2. Execute the MavenManualSimulation
In a separate terminal execute the following Maven command:
```
mvn verify -P manual
```
3. Tear Down NXRM3 via docker-compose
In a separate terminal execute the following in the `proxy` subdirectory:
```
docker-compose stop
docker-compose rm -f
```
This will stop and remove the NXRM3 and nginx docker containers.
