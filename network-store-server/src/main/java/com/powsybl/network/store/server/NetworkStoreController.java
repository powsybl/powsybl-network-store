/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server;

import com.powsybl.network.store.model.*;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@RestController
@RequestMapping(value = "/" + NetworkStoreApi.VERSION + "/networks")
@Api(value = "Network store")
public class NetworkStoreController {

    @Autowired
    private NetworkStoreRepository repository;

    private <T extends IdentifiableAttributes> ResponseEntity<TopLevelDocument<T>> get(Supplier<Optional<Resource<T>>> f) {
        return f.get()
                .map(resource -> ResponseEntity.ok(TopLevelDocument.of(resource)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(TopLevelDocument.empty()));
    }

    private <T extends IdentifiableAttributes> ResponseEntity<Void> create(Consumer<Resource<T>> f, List<Resource<T>> resources) {
        for (Resource<T> resource : resources) {
            f.accept(resource);
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private <T extends IdentifiableAttributes> ResponseEntity<Void> createAll(Consumer<List<Resource<T>>> f, List<Resource<T>> resources) {
        f.accept(resources);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private <T extends IdentifiableAttributes> ResponseEntity<TopLevelDocument<T>> getAll(Supplier<List<Resource<T>>> resourcesSupplier, Integer limit) {
        List<Resource<T>> resources = resourcesSupplier.get();
        List<Resource<T>> limitedResources;
        if (limit == null || resources.size() < limit) {
            limitedResources = resources;
        } else {
            limitedResources = resources.stream().limit(limit).collect(Collectors.toList());
        }
        TopLevelDocument<T> document = TopLevelDocument.of(limitedResources)
                .addMeta("totalCount", Integer.toString(resources.size()));
        return ResponseEntity.ok()
                .body(document);
    }

    // network

    @RequestMapping(method = GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get network list", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get network list"))
    public TopLevelDocument<NetworkAttributes> getNetworks() {
        return TopLevelDocument.of(repository.getNetworks());
    }

    @GetMapping(value = "/{networkId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a network by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get network"),
            @ApiResponse(code = 404, message = "Network has not been found")
        })
    public ResponseEntity<TopLevelDocument<NetworkAttributes>> getNetwork(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID id) {
        return get(() -> repository.getNetwork(id));
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Create networks")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create networks"))
    public ResponseEntity<Void> createNetworks(@ApiParam(value = "Network resources", required = true) @RequestBody List<Resource<NetworkAttributes>> networkResources) {
        return createAll(repository::createNetworks, networkResources);
    }

    @DeleteMapping(value = "/{networkId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete a network by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully delete network"),
            @ApiResponse(code = 404, message = "Network has not been found")
        })
    public ResponseEntity<Void> deleteNetwork(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID id) {
        repository.deleteNetwork(id);
        return ResponseEntity.ok().build();
    }

    // substation

    @GetMapping(value = "/{networkId}/substations", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get substations", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get substation list"))
    public ResponseEntity<TopLevelDocument<SubstationAttributes>> getSubstations(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                 @ApiParam(value = "Max number of substation to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getSubstations(networkId), limit);
    }

    @GetMapping(value = "/{networkId}/substations/{substationId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a substation by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get substation"),
            @ApiResponse(code = 404, message = "Substation has not been found")
        })
    public ResponseEntity<TopLevelDocument<SubstationAttributes>> getSubstation(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                @ApiParam(value = "Substation ID", required = true) @PathVariable("substationId") String substationId) {
        return get(() -> repository.getSubstation(networkId, substationId));
    }

    @PostMapping(value = "/{networkId}/substations")
    @ApiOperation(value = "Create substations")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully substations"))
    public ResponseEntity<Void> createSubstations(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                  @ApiParam(value = "Substation resources", required = true) @RequestBody List<Resource<SubstationAttributes>> substationResources) {
        return createAll(resource -> repository.createSubstations(networkId, resource), substationResources);
    }

    // voltage level

    @GetMapping(value = "/{networkId}/voltage-levels", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get voltage levels", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get voltage level list"))
    public ResponseEntity<TopLevelDocument<VoltageLevelAttributes>> getVoltageLevels(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                     @ApiParam(value = "Max number of voltage level to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getVoltageLevels(networkId), limit);
    }

    @GetMapping(value = "/{networkId}/voltage-levels/{voltageLevelId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a voltage level by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get voltage level"),
            @ApiResponse(code = 404, message = "Voltage level has not been found")
        })
    public ResponseEntity<TopLevelDocument<VoltageLevelAttributes>> getVoltageLevel(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                    @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return get(() -> repository.getVoltageLevel(networkId, voltageLevelId));
    }

    @PostMapping(value = "/{networkId}/voltage-levels")
    @ApiOperation(value = "Create voltage levels")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create voltage levels"))
    public ResponseEntity<Void> createVoltageLevels(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                    @ApiParam(value = "Voltage level resources", required = true) @RequestBody List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        return createAll(resource -> repository.createVoltageLevels(networkId, resource), voltageLevelResources);
    }

    @GetMapping(value = "/{networkId}/substations/{substationId}/voltage-levels", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get voltage levels for a substation", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get voltage level list for a substation"))
    public ResponseEntity<TopLevelDocument<VoltageLevelAttributes>> getVoltageLevels(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                     @ApiParam(value = "Substation ID", required = true) @PathVariable("substationId") String substationId) {
        return getAll(() -> repository.getVoltageLevels(networkId, substationId), null);
    }

    @GetMapping(value = "/{networkId}/voltage-levels/{voltageLevelId}/busbar-sections", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get busbar sections connected to voltage level", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get busbar sections connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<BusbarSectionAttributes>> getVoltageLevelBusbarSections(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                   @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelBusbarSections(networkId, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/voltage-levels/{voltageLevelId}/switches", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get switches connected to voltage level", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get busbar sections connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<SwitchAttributes>> getVoltageLevelSwitches(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                      @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelSwitches(networkId, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/voltage-levels/{voltageLevelId}/generators", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get generators connected to voltage level", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get generators connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<GeneratorAttributes>> getVoltageLevelGenerators(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                           @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelGenerators(networkId, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/voltage-levels/{voltageLevelId}/loads", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get loads connected to voltage level", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get loads connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<LoadAttributes>> getVoltageLevelLoads(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                 @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelLoads(networkId, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/voltage-levels/{voltageLevelId}/shunt-compensators", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get shunt compensators connected to voltage level", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get shunt compensators connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                         @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelShuntCompensators(networkId, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/voltage-levels/{voltageLevelId}/vsc-converter-stations", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get static VSC converter stations connected to voltage level", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get VSC converter stations connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                               @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelVscConverterStations(networkId, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/voltage-levels/{voltageLevelId}/lcc-converter-stations", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get static LCC converter stations connected to voltage level", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get LCC converter stations connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                               @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelLccConverterStations(networkId, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/voltage-levels/{voltageLevelId}/static-var-compensators", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get static var compensators connected to voltage level", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get static var compensators connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                             @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelStaticVarCompensators(networkId, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/voltage-levels/{voltageLevelId}/2-windings-transformers", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get 2 windings transformers connected to voltage level", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get 2 windings transformers connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                                     @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelTwoWindingsTransformers(networkId, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/voltage-levels/{voltageLevelId}/lines", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get lines connected to voltage level", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get lines connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<LineAttributes>> getVoltageLevelLines(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                 @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelLines(networkId, voltageLevelId), null);
    }

    // generator

    @PostMapping(value = "/{networkId}/generators")
    @ApiOperation(value = "Create generators")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create generators"))
    public ResponseEntity<Void> createGenerators(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                 @ApiParam(value = "Generator resources", required = true) @RequestBody List<Resource<GeneratorAttributes>> generatorResources) {
        return createAll(resource -> repository.createGenerators(networkId, resource), generatorResources);
    }

    @GetMapping(value = "/{networkId}/generators", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get generators", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get generator list"))
    public ResponseEntity<TopLevelDocument<GeneratorAttributes>> getGenerators(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                               @ApiParam(value = "Max number of generator to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getGenerators(networkId), limit);
    }

    @GetMapping(value = "/{networkId}/generators/{generatorId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a generator by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get generator"),
            @ApiResponse(code = 404, message = "Generator has not been found")
        })
    public ResponseEntity<TopLevelDocument<GeneratorAttributes>> getGenerator(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                              @ApiParam(value = "Generator ID", required = true) @PathVariable("generatorId") String generatorId) {
        return get(() -> repository.getGenerator(networkId, generatorId));
    }

    // load

    @PostMapping(value = "/{networkId}/loads")
    @ApiOperation(value = "Create loads")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create loads"))
    public ResponseEntity<Void> createLoads(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                            @ApiParam(value = "Load resources", required = true) @RequestBody List<Resource<LoadAttributes>> loadResources) {
        return createAll(resource -> repository.createLoads(networkId, resource), loadResources);
    }

    @GetMapping(value = "/{networkId}/loads", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get loads", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get load list"))
    public ResponseEntity<TopLevelDocument<LoadAttributes>> getLoads(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                     @ApiParam(value = "Max number of load to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getLoads(networkId), limit);
    }

    @GetMapping(value = "/{networkId}/loads/{loadId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a load by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get load"),
            @ApiResponse(code = 404, message = "Load has not been found")
        })
    public ResponseEntity<TopLevelDocument<LoadAttributes>> getLoad(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                    @ApiParam(value = "Load ID", required = true) @PathVariable("loadId") String loadId) {
        return get(() -> repository.getLoad(networkId, loadId));
    }

    // shunt compensator

    @PostMapping(value = "/{networkId}/shunt-compensators")
    @ApiOperation(value = "Create shunt compensators")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create shunt compensators"))
    public ResponseEntity<Void> createShuntCompensators(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                        @ApiParam(value = "Shunt compensator resources", required = true) @RequestBody List<Resource<ShuntCompensatorAttributes>> shuntResources) {
        return createAll(resource -> repository.createShuntCompensators(networkId, resource), shuntResources);
    }

    @GetMapping(value = "/{networkId}/shunt-compensators", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get shunt compensators", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get shunt compensator list"))
    public ResponseEntity<TopLevelDocument<ShuntCompensatorAttributes>> getShuntCompensators(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                             @ApiParam(value = "Max number of shunt compensator to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getShuntCompensators(networkId), limit);
    }

    @GetMapping(value = "/{networkId}/shunt-compensators/{shuntCompensatorId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a shunt compensator by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get shunt compensator"),
            @ApiResponse(code = 404, message = "Shunt compensator has not been found")
        })
    public ResponseEntity<TopLevelDocument<ShuntCompensatorAttributes>> getShuntCompensator(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                            @ApiParam(value = "Shunt compensator ID", required = true) @PathVariable("shuntCompensatorId") String shuntCompensatorId) {
        return get(() -> repository.getShuntCompensator(networkId, shuntCompensatorId));
    }

    // VSC converter station

    @PostMapping(value = "/{networkId}/vsc-converter-stations")
    @ApiOperation(value = "Create VSC converter stations")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create VSC converter stations"))
    public ResponseEntity<Void> createVscConverterStations(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                           @ApiParam(value = "VSC converter station resources", required = true) @RequestBody List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        return createAll(resource -> repository.createVscConverterStations(networkId, resource), vscConverterStationResources);
    }

    @GetMapping(value = "/{networkId}/vsc-converter-stations", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get VSC converter stations", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get VSC converter stations list"))
    public ResponseEntity<TopLevelDocument<VscConverterStationAttributes>> getVscConverterStations(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                   @ApiParam(value = "Max number of VSC converter stations to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getVscConverterStations(networkId), limit);
    }

    @GetMapping(value = "/{networkId}/vsc-converter-stations/{vscConverterStationId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a VSC converter station by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get VSC converter station"),
            @ApiResponse(code = 404, message = "VSC converter station has not been found")
        })
    public ResponseEntity<TopLevelDocument<VscConverterStationAttributes>> getVscConverterStation(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                  @ApiParam(value = "VSC converter station ID", required = true) @PathVariable("vscConverterStationId") String vscConverterStationId) {
        return get(() -> repository.getVscConverterStation(networkId, vscConverterStationId));
    }

    // LCC converter station

    @PostMapping(value = "/{networkId}/lcc-converter-stations")
    @ApiOperation(value = "Create LCC converter stations")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create LCC converter stations"))
    public ResponseEntity<Void> createLccConverterStations(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                           @ApiParam(value = "LCC converter station resources", required = true) @RequestBody List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        return createAll(resource -> repository.createLccConverterStations(networkId, resource), lccConverterStationResources);
    }

    @GetMapping(value = "/{networkId}/lcc-converter-stations", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get LCC converter stations", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get LCC converter stations list"))
    public ResponseEntity<TopLevelDocument<LccConverterStationAttributes>> getLccConverterStations(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                   @ApiParam(value = "Max number of LCC converter stations to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getLccConverterStations(networkId), limit);
    }

    @GetMapping(value = "/{networkId}/lcc-converter-stations/{lccConverterStationId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a LCC converter station by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get LCC converter station"),
            @ApiResponse(code = 404, message = "LCC converter station has not been found")
        })
    public ResponseEntity<TopLevelDocument<LccConverterStationAttributes>> getLccConverterStation(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                  @ApiParam(value = "LCC converter station ID", required = true) @PathVariable("lccConverterStationId") String lccConverterStationId) {
        return get(() -> repository.getLccConverterStation(networkId, lccConverterStationId));
    }

    // static var compensator

    @PostMapping(value = "/{networkId}/static-var-compensators")
    @ApiOperation(value = "Create static var compensators")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create static var compensators"))
    public ResponseEntity<Void> createStaticVarCompensators(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                            @ApiParam(value = "Static var compensator resources", required = true) @RequestBody List<Resource<StaticVarCompensatorAttributes>> staticVarCompenstatorResources) {
        return createAll(resource -> repository.createStaticVarCompensators(networkId, resource), staticVarCompenstatorResources);
    }

    @GetMapping(value = "/{networkId}/static-var-compensators", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get static var compensators", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get static var compensator list"))
    public ResponseEntity<TopLevelDocument<StaticVarCompensatorAttributes>> getStaticVarCompensators(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                     @ApiParam(value = "Max number of static var compensators to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getStaticVarCompensators(networkId), limit);
    }

    @GetMapping(value = "/{networkId}/static-var-compensators/{staticVarCompensatorId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a static var compensator by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get static var compensator"),
            @ApiResponse(code = 404, message = "Static var compensator has not been found")
        })
    public ResponseEntity<TopLevelDocument<StaticVarCompensatorAttributes>> getStaticVarCompensator(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                    @ApiParam(value = "Static var compensator ID", required = true) @PathVariable("staticVarCompensatorId") String staticVarCompensatorId) {
        return get(() -> repository.getStaticVarCompensator(networkId, staticVarCompensatorId));
    }

    // busbar section

    @PostMapping(value = "/{networkId}/busbar-sections")
    @ApiOperation(value = "Create busbar sections")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create busbar sections"))
    public ResponseEntity<Void> createBusbarSections(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                     @ApiParam(value = "Busbar section resources", required = true) @RequestBody List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        return createAll(resource -> repository.createBusbarSections(networkId, resource), busbarSectionResources);
    }

    @GetMapping(value = "/{networkId}/busbar-sections", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get busbar sections", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get busbar section list"))
    public ResponseEntity<TopLevelDocument<BusbarSectionAttributes>> getBusbarSections(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                       @ApiParam(value = "Max number of busbar section to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getBusbarSections(networkId), limit);
    }

    @GetMapping(value = "/{networkId}/busbar-sections/{busbarSectionId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a busbar section by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get busbar section"),
            @ApiResponse(code = 404, message = "Busbar section has not been found")
        })
    public ResponseEntity<TopLevelDocument<BusbarSectionAttributes>> getBusbarSection(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                      @ApiParam(value = "Busbar section ID", required = true) @PathVariable("busbarSectionId") String busbarSectionId) {
        return get(() -> repository.getBusbarSection(networkId, busbarSectionId));
    }

    // switch

    @PostMapping(value = "/{networkId}/switches")
    @ApiOperation(value = "Create switches")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create switches"))
    public ResponseEntity<Void> createSwitches(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                               @ApiParam(value = "Switch resource", required = true) @RequestBody List<Resource<SwitchAttributes>> switchResources) {
        return createAll(resources -> repository.createSwitches(networkId, resources), switchResources);
    }

    @GetMapping(value = "/{networkId}/switches", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get switches", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get switch list"))
    public ResponseEntity<TopLevelDocument<SwitchAttributes>> getSwitches(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                          @ApiParam(value = "Max number of switch to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getSwitches(networkId), limit);
    }

    @GetMapping(value = "/{networkId}/switch/{switchId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a switch by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get switch"),
            @ApiResponse(code = 404, message = "Switch has not been found")
        })
    public ResponseEntity<TopLevelDocument<SwitchAttributes>> getSwitch(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                        @ApiParam(value = "Switch ID", required = true) @PathVariable("switchId") String switchId) {
        return get(() -> repository.getSwitch(networkId, switchId));
    }

    // 2 windings transformer

    @PostMapping(value = "/{networkId}/2-windings-transformers")
    @ApiOperation(value = "Create 2 windings transformers")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create 2 windings transformers"))
    public ResponseEntity<Void> createTwoWindingsTransformers(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                              @ApiParam(value = "2 windings transformer resources", required = true) @RequestBody List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        return createAll(resource -> repository.createTwoWindingsTransformers(networkId, resource), twoWindingsTransformerResources);
    }

    @GetMapping(value = "/{networkId}/2-windings-transformers", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get 2 windings transformers", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get 2 windings transformer list"))
    public ResponseEntity<TopLevelDocument<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                         @ApiParam(value = "Max number of 2 windings transformer to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getTwoWindingsTransformers(networkId), limit);
    }

    @GetMapping(value = "/{networkId}/2-windings-transformers/{twoWindingsTransformerId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a 2 windings transformer by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get 2 windings transformer"),
            @ApiResponse(code = 404, message = "2 windings transformer has not been found")
        })
    public ResponseEntity<TopLevelDocument<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                        @ApiParam(value = "2 windings transformer ID", required = true) @PathVariable("twoWindingsTransformerId") String twoWindingsTransformerId) {
        return get(() -> repository.getTwoWindingsTransformer(networkId, twoWindingsTransformerId));
    }

    // line

    @PostMapping(value = "/{networkId}/lines")
    @ApiOperation(value = "Create lines")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create lines"))
    public ResponseEntity<Void> createLines(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                            @ApiParam(value = "line resources", required = true) @RequestBody List<Resource<LineAttributes>> lineResources) {
        return createAll(resource -> repository.createLines(networkId, resource), lineResources);
    }

    @GetMapping(value = "/{networkId}/lines", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get lines", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get line list"))
    public ResponseEntity<TopLevelDocument<LineAttributes>> getLines(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                        @ApiParam(value = "Max number of line to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getLines(networkId), limit);
    }

    @GetMapping(value = "/{networkId}/lines/{lineId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a line by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get line"),
            @ApiResponse(code = 404, message = "line has not been found")
        })
    public ResponseEntity<TopLevelDocument<LineAttributes>> getLine(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                    @ApiParam(value = "Line ID", required = true) @PathVariable("lineId") String lineId) {
        return get(() -> repository.getLine(networkId, lineId));
    }
}
