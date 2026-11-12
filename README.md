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
  - [Identifiable preloading mode](#identifiable-preloading-mode)
  - [Thread-safety](#thread-safety)
- [IIDM TCK](#iidm-tck)
- [In-memory usage outside of tests](#in-memory-usage-outside-of-tests)
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

### Identifiable preloading mode

The `IdentifiablePreloadingMode` controls what happens when a single identifiable is fetched by id 
(e.g. `network.getIdentifiable(id)`). It is independent from the preloading strategy, which decides what is loaded when 
a collection is accessed:

| Mode         | Behavior                                                                                                                                                         |
| ------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `NONE`       | Only the requested identifiable is fetched. Collections are loaded later, when they are accessed.                                                                |
| `COLLECTION` | The requested identifiable is fetched, then the whole collection of its type is loaded, following the preloading strategy. Nothing else is loaded if the id is unknown. |

The result of a `getIdentifiable` call therefore depends on both settings:

| Preloading strategy                   | `NONE` mode                  | `COLLECTION` mode                                                     |
| ------------------------------------- | ---------------------------- | --------------------------------------------------------------------- |
| `NONE`                                | Only the identifiable        | Only the identifiable: the mode has no effect without preloading      |
| `COLLECTION`                          | Only the identifiable        | The identifiable and the collection of its type                       |
| `ALL_COLLECTIONS_NEEDED_FOR_BUS_VIEW` | Only the identifiable        | The identifiable and, if its type is needed for the bus view, all the collections needed for the bus view; otherwise the collection of its type |

Unlike the preloading strategy, this mode is set once for the whole `NetworkStoreService`, through the `identifiable-preloading-mode` property described below or the `NetworkStoreService` constructor. It cannot be overridden per call.

### Thread-safety

Each call to `getNetwork()` builds an independent client stack (cache + buffer) scoped to that single `Network`
instance — different `Network` objects never share mutable state, so loading multiple networks concurrently (e.g.
one per thread) is safe.

However, **a single `Network` instance is not thread-safe**: the underlying cache and buffer use plain,
unsynchronized collections. Concurrent reads and writes on the same `Network` object from multiple threads can
result in race conditions, inconsistent state, or lost updates. If a network needs to be accessed from multiple
threads, external synchronization is the caller's responsibility (or load a separate `Network` instance per thread
via a new `getNetwork()` call).


## IIDM TCK

The IIDM network model from powsybl-core is defined through **interfaces** (`Network`, `Line`,
`Generator`, etc., in `powsybl-iidm-api`). The TCK is a suite of tests, written solely against
these interfaces, that guarantees an IIDM implementation honors the expected contract (equipment
behavior, variant management, validations, exceptions...).

Powsybl-core provides this TCK through the `powsybl-iidm-tck` module, which exposes abstract test
classes (`AbstractLineTest`, `AbstractGeneratorTest`, ...) packaged as a *test-jar* and reusable by
any IIDM implementation.

### How the TCK is used in this repo

This repo contains `network-store-iidm-impl`, which implements the IIDM interfaces
(`LineImpl`, `GeneratorImpl`, ...) on top of an internal abstraction, `NetworkStoreClient`.

For testing, `network-store-iidm-impl` uses a `NetworkFactoryImpl()` configured with a fully
in-memory client (`OfflineNetworkStoreClient` / `CachedNetworkStoreClient`), with no server and no
network calls. Under `src/test/.../tck/`, classes such as `GeneratorTest extends
AbstractGeneratorTest` run the TCK directly against this object model, to quickly validate its
conformance to the IIDM contract, at the unit test level.

Switching to this implementation relies on powsybl-core's **SPI mechanism**
(`NetworkFactoryService`, discovered via `ServiceLoader`): `NetworkFactoryServiceImpl`, named
`"NetworkStore"`, exposes the `NetworkFactoryImpl()` provided by this repo.

Note: this repo does not cover `network-store-model` and `network-store-client` with the IIDM TCK
directly — `network-store-model` only defines data transfer objects (`Resource<XxxAttributes>`),
and `network-store-client` only provides a REST-based adapter (`RestNetworkStoreClient`) of
`NetworkStoreClient`, without redefining any IIDM interface itself.

A separate, end-to-end usage of the same TCK exists outside of this repo, in the
[`powsybl-network-store-server`](https://github.com/powsybl/powsybl-network-store-server) repo
(module `network-store-iidm-tck`), where the TCK is run through the full chain:
`RestNetworkStoreClient` → HTTP → the `network-store-server` Spring Boot application → database
persistence.

## In-memory usage outside of tests

While `network-store-iidm-impl` is primarily exercised by the TCK in-memory for unit testing (see
above), its default `NetworkFactoryImpl()` — backed by `OfflineNetworkStoreClient` wrapped in
`CachedNetworkStoreClient` — is also used directly in production code that needs to manipulate an
IIDM network purely in memory, without any network-store server or database.

It is worth noting that `OfflineNetworkStoreClient` itself does not store anything: every
create/update is a no-op and every read returns an empty result. The actual in-memory storage is
provided by `CachedNetworkStoreClient`, a layer originally designed as a **performance cache** on
top of a real REST client (to avoid repeated HTTP calls), here repurposed as the sole backing
store since its delegate never returns any data.

### A different variant model than powsybl-core

This in-memory implementation handles variants differently from powsybl-core's native
`iidm-impl`:

- In **powsybl-core**, only a few explicitly chosen attributes are variant-dependent, stored as
  small arrays indexed by variant number (e.g. a battery's current state of charge); most
  attributes (e.g. a line's `r`/`x`/`g1`/`b1`/`g2`/`b2`) are plain fields, shared identically across
  all variants. Cloning a variant is therefore cheap: it only extends those small per-variant
  arrays, while immutable/shared data is never duplicated. Making a new attribute variant-dependent
  requires a code change in the corresponding `*Impl` class.
- In **network-store**, `VariantManagerImpl.cloneVariant()` delegates to
  `NetworkStoreClient.cloneNetwork()`, which — in `CachedNetworkStoreClient` — clones the *entire*
  cached collection of every equipment type (via full JSON serialization/deserialization), for
  every attribute of every resource. As a result, **any attribute of any equipment can vary between
  variants**, without requiring any code change — a much more flexible model. This flexibility comes
  at the cost of higher memory usage and CPU cost on every `cloneVariant()` call, since whole
  objects are duplicated rather than a handful of small arrays.
- Another limitation: `VariantManagerImpl.allowVariantMultiThreadAccess()` throws
  `PowsyblException` in network-store ("Network store implementation does not support multi-thread
  access yet"), whereas powsybl-core's native implementation supports it.


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
            identifiable-preloading-mode: COLLECTION
```

List of available variables:

| Variable                                                           | Description                     | Optional | Default value                |
| ------------------------------------------------------------------ | ------------------------------- | -------- | ---------------------------- |
| powsybl.services.network-store-server.base-uri                     | URL of the network store server | Yes      | http://network-store-server/ |
| powsybl.services.network-store-server.preloading-strategy          | Preloading strategy             | Yes      | NONE                         |
| powsybl.services.network-store-server.identifiable-preloading-mode | Identifiable preloading mode    | Yes      | NONE                         |

### Standalone / PlatformConfig configuration

Outside of a Spring context, the client can also be configured through PowSyBl's `PlatformConfig` mechanism (e.g. in
`~/.itools/config.yml`), using the `network-store` module. This is an alternative to the Spring properties above and
is independent from them — the two configuration mechanisms do not share values, so only one needs to be set
depending on how the client is used:

```yaml
network-store:
    base-url: http://localhost:8080/
    preloading-strategy: COLLECTION
    identifiable-preloading-mode: COLLECTION
```

| Variable                                   | Description                     | Optional | Default value          |
| ------------------------------------------ | ------------------------------- | -------- | ---------------------- |
| network-store.base-url                     | URL of the network store server | Yes      | http://localhost:8080/ |
| network-store.preloading-strategy          | Preloading strategy             | Yes      | NONE                   |
| network-store.identifiable-preloading-mode | Identifiable preloading mode    | Yes      | NONE                   |

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
