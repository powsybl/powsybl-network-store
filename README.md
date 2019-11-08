# PowSyBl Network Store

![](https://github.com/powsybl/powsybl-network-store/workflows/CI/badge.svg)
[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)
[![Join the community on Spectrum](https://withspectrum.github.io/badge/badge.svg)](https://spectrum.chat/powsybl)

PowSyBl (**Pow**er **Sy**stem **Bl**ocks) is an open source framework written in Java, that makes it easy to write complex
software for power systems’ simulations and analysis. Its modular approach allows developers to extend or customize its
features.

PowSyBl is part of the LF Energy Foundation, a project of The Linux Foundation that supports open source innovation projects
within the energy and electricity sectors.

<p align="center">
<img src="https://raw.githubusercontent.com/powsybl/powsybl-gse/master/gse-spi/src/main/resources/images/logo_lfe_powsybl.svg?sanitize=true" alt="PowSyBl Logo" width="50%"/>
</p>

Read more at https://www.powsybl.org !

This project and everyone participating in it is governed by the [PowSyBl Code of Conduct](https://github.com/powsybl/.github/blob/master/CODE_OF_CONDUCT.md).
By participating, you are expected to uphold this code. Please report unacceptable behavior to [powsybl-tsc@lists.lfenergy.org](mailto:powsybl-tsc@lists.lfenergy.org).

## PowSyBl vs PowSyBl Network Store

PowSyBl Network Store is an alternative implementation of PowSyBl Core Network API that persists
in a [Cassandra database](http://cassandra.apache.org/).

## Getting started

### Build

```bash
cd powsybl-network-store
mvn clean install
```

### Cassandra install

```bash
cd $HOME
wget http://www.apache.org/dyn/closer.lua/cassandra/3.11.4/apache-cassandra-3.11.4-bin.tar.gz
tar xvfz apache-cassandra-3.11.4-bin.tar.gz
cd apache-cassandra-3.11.4
bin/cqlsh
```

Copy paste network-store-server/src/main/resources/iidm.cql in the cql shell to create the iidm keyspace and all necessary tables.


### Start network store server

In an other shell: 

```bash
cd powsybl-network-store/network-store-server/target/
java -jar powsybl-network-store-server-1.0.0-SNAPSHOT-exec.jar
```

Spring boot server should start and connect to Cassandra database (localhost hardcoded...)

### Import a network in the database

In your preferred IDE, create a project with following dependencies:

```xml
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-network-store-client</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-iidm-test</artifactId>
    <version>3.0.0</version>
</dependency>
```

Run the Java code to import in IIDM store a programmatic test node/breaker network:

```java
public static void main(String[] args) throws Exception {
    String baseUrl = "http://localhost:8080/";
    try (NetworkStoreService service = new NetworkStoreService(baseUrl, PreloadingStrategy.NONE)) {
    	Network network = NetworkTest1Factory.create(service.getNetworkFactory());
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

### List  voltage levels from a stored network

```java
public static void main(String[] args) throws Exception {
    String baseUrl = "http://localhost:8080/";
    try (NetworkStoreService service = new NetworkStoreService(baseUrl, PreloadingStrategy.COLLECTION)) {
        Network network = service.getNetwork("network1");
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
    public List<String> getSubstations(String networkId) {
        Network network = service.getNetwork(networkId, PreloadingStrategy.COLLECTION);
        return network.getSubstationStream().map(Identifiable::getId).collect(Collectors.toList());
    }
}
```

Network store service could be configured using application.yml like this:

```yaml
network-store-server:
    base-uri: http://localhost:8080/
    preloading-strategy: COLLECTION
```

List of available  variables:

| Variable                                 | Description                     | Optional | Default vallue               |
| ---------------------------------------- | ------------------------------- | -------- | ---------------------------- |
| network-store-server.base-uri            | URL of the network store server | Yes      | http://network-store-server/ |
| network-store-server.preloading-strategy | Preloading strategy             | Yes      | NONE                         |
