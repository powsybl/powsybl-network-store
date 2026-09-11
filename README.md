# PowSyBl Network Store Client

[![Actions Status](https://github.com/powsybl/powsybl-network-store/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/powsybl/powsybl-network-store/actions)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=com.powsybl%3Apowsybl-network-store&metric=coverage)](https://sonarcloud.io/component_measures?id=com.powsybl%3Apowsybl-network-store&metric=coverage)
[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)
[![Slack](https://img.shields.io/badge/slack-powsybl-blueviolet.svg?logo=slack)](https://join.slack.com/t/powsybl/shared_invite/zt-36jvd725u-cnquPgZb6kpjH8SKh~FWHQ)

PowSyBl (**Pow**er **Sy**stem **Bl**ocks) is an open source framework written in Java, that makes it easy to write complex
software for power systems’ simulations and analysis. Its modular approach allows developers to extend or customize its
features.

PowSyBl is part of the LF Energy Foundation, a project of The Linux Foundation that supports open source innovation projects
within the energy and electricity sectors.

<p align="center">
<img src="https://raw.githubusercontent.com/powsybl/powsybl-gse/main/gse-spi/src/main/resources/images/logo_lfe_powsybl.svg?sanitize=true" alt="PowSyBl Logo" width="50%"/>
</p>

Read more at https://www.powsybl.org !

This project and everyone participating in it is governed by the [PowSyBl Code of Conduct](https://github.com/powsybl/.github/blob/main/CODE_OF_CONDUCT.md).
By participating, you are expected to uphold this code. Please report unacceptable behavior to [powsybl-tsc@lists.lfenergy.org](mailto:powsybl-tsc@lists.lfenergy.org).

## Table of Contents

- [Description](#description)
- [Network store client](#network-store-client)
  - [REST client](#rest-client)
  - [Buffering](#buffering)
  - [Caching](#caching)
  - [Preloading strategies](#preloading-strategies)
  - [Thread-safety](#thread-safety)
- [Getting started](#getting-started)
  - [Requirements](#requirements)
  - [Build](#build)
  - [Import a network in the database](#import-a-network-in-the-database)
  - [Import a network from a file in the database](#import-a-network-from-a-file-in-the-database)
  - [List voltage levels from a stored network](#list-voltage-levels-from-a-stored-network)
  - [Injection network store service in a Spring controller](#injection-network-store-service-in-a-spring-controller)
  - [Standalone / PlatformConfig configuration](#standalone--platformconfig-configuration)
  - [Run integration tests](#run-integration-tests)
- [Tips for performance debugging](#tips-for-performance-debugging)

## Description

This repository provides the client-side implementation of the PowSyBl network store, an alternative backend for
storing and retrieving power network (IIDM) models through a remote network store server, instead of relying on
in-memory or file-based storage.

It contains the following modules:
- **network-store-model**: the data model shared between client and server, describing the network attributes exchanged over the REST API.
- **network-store-iidm-impl**: an implementation of the PowSyBl IIDM API backed by the network store model, allowing networks to be manipulated through the standard IIDM interfaces.
- **network-store-client**: the Java client (`NetworkStoreService`) used to connect to a network store server, import/export networks, and retrieve stored networks with configurable preloading strategies.
- **network-store-client-distribution**: an aggregation module used for Sonar code coverage analysis across the client modules.

## Network store client

The client used to connect to a network store server is built as a stack of layers, each adding one capability on
top of a plain REST client. A fresh instance of this stack is created every time a network is retrieved (e.g. on each
`getNetwork()` call), so state such as caches and buffers is scoped to that single network-loading session and is
never shared globally across networks.

### REST client

The lowest layer translates every read/write operation into HTTP calls to the network store server. The server base
URI is configurable (see the `base-uri` property below). To keep request sizes bounded, bulk creations, removals and
extension/limits-group deletions are split into fixed-size chunks of 1000 resources before being sent (this chunk
size is currently not configurable), and a transient network error (`ResourceAccessException`) during one of these
calls is retried exactly once, immediately and without backoff.

### Buffering

Write operations (create, update, remove) are not sent to the server immediately: they are accumulated in memory and
only pushed to the REST layer when the client is flushed. On flush, calls are grouped by resource type and sent in
parallel to reduce the total round-trip time; extension attributes and operational limits groups removals are
flushed first, since equipment updates can depend on them.

### Caching

On top of buffering, a read-through cache avoids fetching the same data twice: once a collection of resources (or a
sub-collection attached to a container, e.g. the equipments of a voltage level) has been loaded from the server, it
is kept in memory and reused for subsequent accesses. The cache is invalidated purely by local mutations (creates,
updates and removes update it in place) — there is no time-based expiration. As an additional optimization, if many
individual equipments are looked up one by one, the client automatically switches to loading the remaining ones in
bulk instead of issuing one REST call per equipment.

### Preloading strategies

The `PreloadingStrategy` controls how eagerly data is fetched ahead of use:

| Strategy                              | Behavior                                                                                                                                                                                                  |
| -------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `NONE`                                 | No eager loading. Data is fetched lazily, one collection at a time, only when it is actually accessed.                                                                                                   |
| `COLLECTION`                           | The first time any resource of a given type is accessed (e.g. any load), the whole collection of that type is fetched in a single call; other resource types are still loaded independently on demand.  |
| `ALL_COLLECTIONS_NEEDED_FOR_BUS_VIEW`  | Same as `COLLECTION`, but as soon as one of the resource types needed to compute the bus/breaker and bus view topology is touched, all of them are preloaded together in parallel. This trades extra upfront loading for fewer round trips when the full network topology is needed. |

This strategy can be selected when creating a `NetworkStoreService` programmatically, or configured through the
`preloading-strategy` property described below. It can also be overridden on a per-call basis by passing an
explicit `PreloadingStrategy` argument to `getNetwork()`, `createNetwork()`, `importNetwork()`, or
`getNetworkFactory()` — the argument takes precedence over the service-wide default for that call only. Note that
`networkExists()` always uses `NONE` regardless of any configured default or argument.

Once a `Network` instance has been created, its preloading strategy is fixed for the lifetime of that instance:
there is no way to change it afterwards. To use a different strategy, call `getNetwork()` again to obtain a new
`Network` instance built with a fresh client/cache/buffer stack.

### Thread-safety

Each call to `getNetwork()` builds an independent client stack (cache + buffer) scoped to that single `Network`
instance — different `Network` objects never share mutable state, so loading multiple networks concurrently (e.g.
one per thread) is safe.

However, **a single `Network` instance is not thread-safe**: the underlying cache and buffer use plain,
unsynchronized collections. Concurrent reads and writes on the same `Network` object from multiple threads can
result in race conditions, inconsistent state, or lost updates. If a network needs to be accessed from multiple
threads, external synchronization is the caller's responsibility (or load a separate `Network` instance per thread
via a new `getNetwork()` call).


## Getting started

### Requirements

- Java 25+
- Maven

### Build

```bash
mvn clean install
```

### Import a network in the database

In your preferred IDE, create a project with the following dependencies.

```xml
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-network-store-client</artifactId>
    <version>1.52.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-iidm-test</artifactId>
    <!-- version managed by your project's powsybl-dependencies BOM -->
</dependency>
```

Run the Java code to import in IIDM store a programmatic test node/breaker network:

```java
public static void main(String[] args) throws Exception {
    String baseUrl = "http://localhost:8080/";
    try (NetworkStoreService service = new NetworkStoreService(baseUrl, PreloadingStrategy.NONE)) {
    	Network network = NetworkTest1Factory.create(service.getNetworkFactory(), "network1");
    }
}
```

### Import a network from a file in the database

```java
public static void main(String[] args) throws Exception {
    String baseUrl = "http://localhost:8080/";
    try (NetworkStoreService service = new NetworkStoreService(baseUrl, PreloadingStrategy.NONE)) {
        Network network = service.importNetwork(Paths.get("/tmp/network1.xiidm"));
    }
}
```

### List voltage levels from a stored network

```java
public static void main(String[] args) throws Exception {
    String baseUrl = "http://localhost:8080/";
    try (NetworkStoreService service = new NetworkStoreService(baseUrl, PreloadingStrategy.COLLECTION)) {
        Network network = service.getNetwork(UUID.fromString("11111111-2222-3333-4444-555555555555"));
        for (VoltageLevel vl : network.getVoltageLevels()) {
            System.out.println(vl.getId());
        }
   }
}
```

### Injection network store service in a Spring controller

```java
@RestController
@RequestMapping(value = "/test")
@ComponentScan(basePackageClasses = {NetworkStoreService.class})
public class TestController {

    @Autowired
    private NetworkStoreService service;

    @RequestMapping(method = GET, produces = APPLICATION_JSON_VALUE)
    public List<String> getSubstations(UUID networkId) {
        Network network = service.getNetwork(networkId, PreloadingStrategy.COLLECTION);
        return network.getSubstationStream().map(Identifiable::getId).collect(Collectors.toList());
    }
}
```

The network store service can be configured using application.yml like this:

```yaml
powsybl:
    services:
        network-store-server:
            base-uri: http://localhost:8080/
            preloading-strategy: COLLECTION
```

List of available variables:

| Variable                                                  | Description                     | Optional | Default value                |
| --------------------------------------------------------- | ------------------------------- | -------- | ---------------------------- |
| powsybl.services.network-store-server.base-uri            | URL of the network store server | Yes      | http://network-store-server/ |
| powsybl.services.network-store-server.preloading-strategy | Preloading strategy             | Yes      | NONE                         |

### Standalone / PlatformConfig configuration

Outside of a Spring context, the client can also be configured through PowSyBl's `PlatformConfig` mechanism (e.g. in
`~/.itools/config.yml`), using the `network-store` module. This is an alternative to the Spring properties above and
is independent from them — the two configuration mechanisms do not share values, so only one needs to be set
depending on how the client is used:

```yaml
network-store:
    base-url: http://localhost:8080/
    preloading-strategy: COLLECTION
```

| Variable                          | Description                     | Optional | Default value            |
| ---------------------------------- | -------------------------------- | -------- | ------------------------ |
| network-store.base-url             | URL of the network store server | Yes      | http://localhost:8080/   |
| network-store.preloading-strategy  | Preloading strategy             | Yes      | NONE                     |

This configuration is loaded via `NetworkStoreConfig.load()` and can be used to create a `NetworkStoreService`
without relying on Spring:

```java
NetworkStoreService service = NetworkStoreService.create(NetworkStoreConfig.load());
```

### Run integration tests

You can run the integration tests:
```bash
$ mvn verify
```


## Tips for performance debugging

- **Choose the right preloading strategy for your access pattern.** `NONE` is best when only a few resources are
  accessed; `COLLECTION` avoids many small round trips when iterating over a whole resource type;
  `ALL_COLLECTIONS_NEEDED_FOR_BUS_VIEW` front-loads everything needed for topology computation and is best when the
  bus/breaker view will be used extensively. Using the wrong strategy is a common source of unexpectedly slow or
  "chatty" network access.
- **Watch out for repeated single-resource lookups.** Looking up many individual equipments one by one is much
  slower than accessing a whole collection; if your code pattern does this, consider switching to
  `COLLECTION`/`ALL_COLLECTIONS_NEEDED_FOR_BUS_VIEW`, or restructure the code to iterate over collections instead.
- **Batch your writes.** Creating, updating or removing many resources in a loop is buffered locally and only sent
  to the server on flush, so performance issues are more likely tied to the size/frequency of flushes than to the
  number of individual calls — avoid flushing more often than necessary.
- **Enable INFO-level logs for the client** to see the timing of every REST call (create/load/update/delete), buffer
  flush duration, and preloading duration — this is the most direct way to identify which operation is slow.
- **Watch for retries in the logs.** Repeated "retry" messages indicate transient network/server issues rather than
  a client-side performance problem.
- **Remember state is per network-load session.** Since caches and buffers are not shared across `getNetwork()`
  calls, reloading the same network multiple times discards any previously built cache — avoid unnecessary repeated
  loads of the same network if performance matters.
