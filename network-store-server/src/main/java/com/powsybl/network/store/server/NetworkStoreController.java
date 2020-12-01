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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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

    private <T extends IdentifiableAttributes> ResponseEntity<Void> updateAll(Consumer<List<Resource<T>>> f, List<Resource<T>> resources) {
        f.accept(resources);
        return ResponseEntity.status(HttpStatus.OK).build();
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

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get network list", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get network list"))
    public TopLevelDocument<NetworkAttributes> getNetworks() {
        return TopLevelDocument.of(repository.getNetworks());
    }

    @GetMapping(value = "/{networkId}/{variantNum}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a network by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get network"),
            @ApiResponse(code = 404, message = "Network has not been found")
        })
    public ResponseEntity<TopLevelDocument<NetworkAttributes>> getNetwork(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID id,
                                                                          @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum) {
        return get(() -> repository.getNetwork(id, variantNum));
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

    @PutMapping(value = "/{networkId}/{variantNum}")
    @ApiOperation(value = "Update network")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully update network"))
    public ResponseEntity<Void> updateNetwork(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                              @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                              @ApiParam(value = "network resource", required = true) @RequestBody Resource<NetworkAttributes> networkResources) {

        return updateAll(resources -> repository.updateNetworks(resources), Collections.singletonList(networkResources));
    }

    // substation

    @GetMapping(value = "/{networkId}/{variantNum}/substations", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get substations", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get substation list"))
    public ResponseEntity<TopLevelDocument<SubstationAttributes>> getSubstations(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                 @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                 @ApiParam(value = "Max number of substation to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getSubstations(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/substations/{substationId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a substation by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get substation"),
            @ApiResponse(code = 404, message = "Substation has not been found")
        })
    public ResponseEntity<TopLevelDocument<SubstationAttributes>> getSubstation(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                @ApiParam(value = "Substation ID", required = true) @PathVariable("substationId") String substationId) {
        return get(() -> repository.getSubstation(networkId, variantNum, substationId));
    }

    @PostMapping(value = "/{networkId}/{variantNum}/substations")
    @ApiOperation(value = "Create substations")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully substations"))
    public ResponseEntity<Void> createSubstations(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                  @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                  @ApiParam(value = "Substation resources", required = true) @RequestBody List<Resource<SubstationAttributes>> substationResources) {
        return createAll(resource -> repository.createSubstations(networkId, variantNum, resource), substationResources);
    }

    @DeleteMapping(value = "/{networkId}/substations/{substationId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete a substation by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully delete substation")
    })
    public ResponseEntity<Void> deleteSubstation(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                @ApiParam(value = "Substation ID", required = true) @PathVariable("substationId") String substationId) {
        repository.deleteSubstation(networkId, substationId);
        return ResponseEntity.ok().build();
    }

    // voltage level

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get voltage levels", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get voltage level list"))
    public ResponseEntity<TopLevelDocument<VoltageLevelAttributes>> getVoltageLevels(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                     @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                     @ApiParam(value = "Max number of voltage level to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getVoltageLevels(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a voltage level by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get voltage level"),
            @ApiResponse(code = 404, message = "Voltage level has not been found")
        })
    public ResponseEntity<TopLevelDocument<VoltageLevelAttributes>> getVoltageLevel(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                    @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                    @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return get(() -> repository.getVoltageLevel(networkId, variantNum, voltageLevelId));
    }

    @PostMapping(value = "/{networkId}/{variantNum}/voltage-levels")
    @ApiOperation(value = "Create voltage levels")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create voltage levels"))
    public ResponseEntity<Void> createVoltageLevels(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                    @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                    @ApiParam(value = "Voltage level resources", required = true) @RequestBody List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        return createAll(resource -> repository.createVoltageLevels(networkId, variantNum, resource), voltageLevelResources);
    }

    @PutMapping(value = "/{networkId}/{variantNum}/voltage-levels")
    @ApiOperation(value = "Update voltage levels")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully update voltage levels"))
    public ResponseEntity<Void> updateVoltageLevels(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                    @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                    @ApiParam(value = "voltage level resources", required = true) @RequestBody List<Resource<VoltageLevelAttributes>> voltageLevelResources) {

        return updateAll(resources -> repository.updateVoltageLevels(networkId, variantNum, resources), voltageLevelResources);
    }

    @DeleteMapping(value = "/{networkId}/voltage-levels/{voltageLevelId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete a voltage level by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully delete voltage level")
    })
    public ResponseEntity<Void> deleteVoltageLevel(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                   @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        repository.deleteVoltageLevel(networkId, voltageLevelId);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/{networkId}/{variantNum}/substations/{substationId}/voltage-levels", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get voltage levels for a substation", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get voltage level list for a substation"))
    public ResponseEntity<TopLevelDocument<VoltageLevelAttributes>> getVoltageLevels(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                     @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                     @ApiParam(value = "Substation ID", required = true) @PathVariable("substationId") String substationId) {
        return getAll(() -> repository.getVoltageLevels(networkId, variantNum, substationId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/busbar-sections", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get busbar sections connected to voltage level", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get busbar sections connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<BusbarSectionAttributes>> getVoltageLevelBusbarSections(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                   @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                   @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelBusbarSections(networkId, variantNum, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/switches", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get switches connected to voltage level", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get switches connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<SwitchAttributes>> getVoltageLevelSwitches(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                      @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                      @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelSwitches(networkId, variantNum, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/generators", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get generators connected to voltage level", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get generators connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<GeneratorAttributes>> getVoltageLevelGenerators(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                           @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                           @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelGenerators(networkId, variantNum, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/batteries", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get batteries connected to voltage level", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get batteries connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<BatteryAttributes>> getVoltageLevelBatteries(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                        @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                        @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelBatteries(networkId, variantNum, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/loads", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get loads connected to voltage level", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get loads connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<LoadAttributes>> getVoltageLevelLoads(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                 @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                 @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelLoads(networkId, variantNum, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/shunt-compensators", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get shunt compensators connected to voltage level", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get shunt compensators connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                         @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                         @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelShuntCompensators(networkId, variantNum, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/vsc-converter-stations", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get static VSC converter stations connected to voltage level", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get VSC converter stations connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                               @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                               @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelVscConverterStations(networkId, variantNum, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/lcc-converter-stations", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get static LCC converter stations connected to voltage level", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get LCC converter stations connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                               @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                               @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelLccConverterStations(networkId, variantNum, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/static-var-compensators", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get static var compensators connected to voltage level", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get static var compensators connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                                 @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                                 @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelStaticVarCompensators(networkId, variantNum, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/2-windings-transformers", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get 2 windings transformers connected to voltage level", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get 2 windings transformers connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                                     @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                                     @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelTwoWindingsTransformers(networkId, variantNum, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/3-windings-transformers", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get 3 windings transformers connected to voltage level", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get 3 windings transformers connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                                         @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                                         @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelThreeWindingsTransformers(networkId, variantNum, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/lines", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get lines connected to voltage level", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get lines connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<LineAttributes>> getVoltageLevelLines(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                 @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                 @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelLines(networkId, variantNum, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/dangling-lines", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get dangling lines connected to voltage level", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get dangling lines connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<DanglingLineAttributes>> getVoltageLevelDanglingLines(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                 @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                 @ApiParam(value = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelDanglingLines(networkId, variantNum, voltageLevelId), null);
    }

    // generator

    @PostMapping(value = "/{networkId}/{variantNum}/generators")
    @ApiOperation(value = "Create generators")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create generators"))
    public ResponseEntity<Void> createGenerators(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                 @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                 @ApiParam(value = "Generator resources", required = true) @RequestBody List<Resource<GeneratorAttributes>> generatorResources) {
        return createAll(resource -> repository.createGenerators(networkId, variantNum, resource), generatorResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/generators", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get generators", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get generator list"))
    public ResponseEntity<TopLevelDocument<GeneratorAttributes>> getGenerators(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                               @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                               @ApiParam(value = "Max number of generator to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getGenerators(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/generators/{generatorId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a generator by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get generator"),
            @ApiResponse(code = 404, message = "Generator has not been found")
        })
    public ResponseEntity<TopLevelDocument<GeneratorAttributes>> getGenerator(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                              @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                              @ApiParam(value = "Generator ID", required = true) @PathVariable("generatorId") String generatorId) {
        return get(() -> repository.getGenerator(networkId, variantNum, generatorId));
    }

    @PutMapping(value = "/{networkId}/{variantNum}/generators")
    @ApiOperation(value = "Update generators")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully update generators"))
    public ResponseEntity<Void> updateGenerators(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                 @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                 @ApiParam(value = "generator resource", required = true) @RequestBody List<Resource<GeneratorAttributes>> generatorResources) {

        return updateAll(resources -> repository.updateGenerators(networkId, variantNum, resources), generatorResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/generators/{generatorId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete a generator by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully delete generator")
    })
    public ResponseEntity<Void> deleteGenerator(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                @ApiParam(value = "Generator ID", required = true) @PathVariable("generatorId") String generatorId) {
        repository.deleteGenerator(networkId, variantNum, generatorId);
        return ResponseEntity.ok().build();
    }

    // battery

    @PostMapping(value = "/{networkId}/{variantNum}/batteries")
    @ApiOperation(value = "Create batteries")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create batteries"))
    public ResponseEntity<Void> createBatteries(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                @ApiParam(value = "Battery resources", required = true) @RequestBody List<Resource<BatteryAttributes>> batteryResources) {
        return createAll(resource -> repository.createBatteries(networkId, variantNum, resource), batteryResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/batteries", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get batteries", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get batteries list"))
    public ResponseEntity<TopLevelDocument<BatteryAttributes>> getBatteries(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                            @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                            @ApiParam(value = "Max number of batteries to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getBatteries(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/batteries/{batteryId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a battery by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get battery"),
            @ApiResponse(code = 404, message = "Battery has not been found")
    })
    public ResponseEntity<TopLevelDocument<BatteryAttributes>> getBattery(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                          @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                          @ApiParam(value = "Battery ID", required = true) @PathVariable("batteryId") String batteryId) {
        return get(() -> repository.getBattery(networkId, variantNum, batteryId));
    }

    @PutMapping(value = "/{networkId}/{variantNum}/batteries")
    @ApiOperation(value = "Update batteries")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully update batteries"))
    public ResponseEntity<Void> updateBatteries(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                @ApiParam(value = "Battery resource", required = true) @RequestBody List<Resource<BatteryAttributes>> batteryResources) {

        return updateAll(resources -> repository.updateBatteries(networkId, variantNum, resources), batteryResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/batteries/{batteryId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete a battery by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully delete battery")
    })
    public ResponseEntity<Void> deleteBattery(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                              @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                              @ApiParam(value = "Battery ID", required = true) @PathVariable("batteryId") String batteryId) {
        repository.deleteBattery(networkId, variantNum, batteryId);
        return ResponseEntity.ok().build();
    }

    // load

    @PostMapping(value = "/{networkId}/{variantNum}/loads")
    @ApiOperation(value = "Create loads")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create loads"))
    public ResponseEntity<Void> createLoads(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                            @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                            @ApiParam(value = "Load resources", required = true) @RequestBody List<Resource<LoadAttributes>> loadResources) {
        return createAll(resource -> repository.createLoads(networkId, variantNum, resource), loadResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/loads", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get loads", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get load list"))
    public ResponseEntity<TopLevelDocument<LoadAttributes>> getLoads(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                     @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                     @ApiParam(value = "Max number of load to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getLoads(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/loads/{loadId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a load by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get load"),
            @ApiResponse(code = 404, message = "Load has not been found")
        })
    public ResponseEntity<TopLevelDocument<LoadAttributes>> getLoad(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                    @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                    @ApiParam(value = "Load ID", required = true) @PathVariable("loadId") String loadId) {
        return get(() -> repository.getLoad(networkId, variantNum, loadId));
    }

    @PutMapping(value = "/{networkId}/{variantNum}/loads")
    @ApiOperation(value = "Update loads")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully update loads"))
    public ResponseEntity<Void> updateLoads(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                            @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                            @ApiParam(value = "load resource", required = true) @RequestBody List<Resource<LoadAttributes>> loadResources) {

        return updateAll(resources -> repository.updateLoads(networkId, variantNum, resources), loadResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/loads/{loadId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete a load by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully delete load")
    })
    public ResponseEntity<Void> deleteLoad(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                           @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                           @ApiParam(value = "Load ID", required = true) @PathVariable("loadId") String loadId) {
        repository.deleteLoad(networkId, variantNum, loadId);
        return ResponseEntity.ok().build();
    }

    // shunt compensator

    @PostMapping(value = "/{networkId}/{variantNum}/shunt-compensators")
    @ApiOperation(value = "Create shunt compensators")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create shunt compensators"))
    public ResponseEntity<Void> createShuntCompensators(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                        @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                        @ApiParam(value = "Shunt compensator resources", required = true) @RequestBody List<Resource<ShuntCompensatorAttributes>> shuntResources) {
        return createAll(resource -> repository.createShuntCompensators(networkId, variantNum, resource), shuntResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/shunt-compensators", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get shunt compensators", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get shunt compensator list"))
    public ResponseEntity<TopLevelDocument<ShuntCompensatorAttributes>> getShuntCompensators(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                             @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                             @ApiParam(value = "Max number of shunt compensator to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getShuntCompensators(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/shunt-compensators/{shuntCompensatorId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a shunt compensator by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get shunt compensator"),
            @ApiResponse(code = 404, message = "Shunt compensator has not been found")
        })
    public ResponseEntity<TopLevelDocument<ShuntCompensatorAttributes>> getShuntCompensator(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                            @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                            @ApiParam(value = "Shunt compensator ID", required = true) @PathVariable("shuntCompensatorId") String shuntCompensatorId) {
        return get(() -> repository.getShuntCompensator(networkId, variantNum, shuntCompensatorId));
    }

    @PutMapping(value = "/{networkId}/{variantNum}/shunt-compensators")
    @ApiOperation(value = "Update shunt compensators")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully update shunt compensators"))
    public ResponseEntity<Void> updateShuntCompensators(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                        @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                        @ApiParam(value = "shunt compensator resource", required = true) @RequestBody List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {

        return updateAll(resources -> repository.updateShuntCompensators(networkId, variantNum, resources), shuntCompensatorResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/shunt-compensators/{shuntCompensatorId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete a shunt compensator by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully delete shunt compensator")
    })
    public ResponseEntity<Void> deleteShuntCompensator(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                       @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                       @ApiParam(value = "Shunt compensator ID", required = true) @PathVariable("shuntCompensatorId") String shuntCompensatorId) {
        repository.deleteShuntCompensator(networkId, variantNum, shuntCompensatorId);
        return ResponseEntity.ok().build();
    }

    // VSC converter station

    @PostMapping(value = "/{networkId}/{variantNum}/vsc-converter-stations")
    @ApiOperation(value = "Create VSC converter stations")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create VSC converter stations"))
    public ResponseEntity<Void> createVscConverterStations(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                           @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                           @ApiParam(value = "VSC converter station resources", required = true) @RequestBody List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        return createAll(resource -> repository.createVscConverterStations(networkId, variantNum, resource), vscConverterStationResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/vsc-converter-stations", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get VSC converter stations", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get VSC converter stations list"))
    public ResponseEntity<TopLevelDocument<VscConverterStationAttributes>> getVscConverterStations(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                   @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                   @ApiParam(value = "Max number of VSC converter stations to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getVscConverterStations(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/vsc-converter-stations/{vscConverterStationId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a VSC converter station by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get VSC converter station"),
            @ApiResponse(code = 404, message = "VSC converter station has not been found")
        })
    public ResponseEntity<TopLevelDocument<VscConverterStationAttributes>> getVscConverterStation(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                  @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                  @ApiParam(value = "VSC converter station ID", required = true) @PathVariable("vscConverterStationId") String vscConverterStationId) {
        return get(() -> repository.getVscConverterStation(networkId, variantNum, vscConverterStationId));
    }

    @PutMapping(value = "/{networkId}/{variantNum}/vsc-converter-stations")
    @ApiOperation(value = "Update VSC converter stations")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully update VSC converter stations"))
    public ResponseEntity<Void> updateVscConverterStations(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                           @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                           @ApiParam(value = "VSC converter station resource", required = true) @RequestBody List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {

        return updateAll(resources -> repository.updateVscConverterStations(networkId, variantNum, resources), vscConverterStationResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/vsc-converter-stations/{vscConverterStationId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete a vsc converter station by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully delete vsc converter station")
    })
    public ResponseEntity<Void> deleteVscConverterStation(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                          @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                          @ApiParam(value = "Vsc converter station ID", required = true) @PathVariable("vscConverterStationId") String vscConverterStationId) {
        repository.deleteVscConverterStation(networkId, variantNum, vscConverterStationId);
        return ResponseEntity.ok().build();
    }

    // LCC converter station

    @PostMapping(value = "/{networkId}/{variantNum}/lcc-converter-stations")
    @ApiOperation(value = "Create LCC converter stations")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create LCC converter stations"))
    public ResponseEntity<Void> createLccConverterStations(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                           @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                           @ApiParam(value = "LCC converter station resources", required = true) @RequestBody List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        return createAll(resource -> repository.createLccConverterStations(networkId, variantNum, resource), lccConverterStationResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/lcc-converter-stations", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get LCC converter stations", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get LCC converter stations list"))
    public ResponseEntity<TopLevelDocument<LccConverterStationAttributes>> getLccConverterStations(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                   @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                   @ApiParam(value = "Max number of LCC converter stations to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getLccConverterStations(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/lcc-converter-stations/{lccConverterStationId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a LCC converter station by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get LCC converter station"),
            @ApiResponse(code = 404, message = "LCC converter station has not been found")
        })
    public ResponseEntity<TopLevelDocument<LccConverterStationAttributes>> getLccConverterStation(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                  @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                  @ApiParam(value = "LCC converter station ID", required = true) @PathVariable("lccConverterStationId") String lccConverterStationId) {
        return get(() -> repository.getLccConverterStation(networkId, variantNum, lccConverterStationId));
    }

    @PutMapping(value = "/{networkId}/{variantNum}/lcc-converter-stations")
    @ApiOperation(value = "Update LCC converter stations")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully update LCC converter stations"))
    public ResponseEntity<Void> updateLccConverterStations(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                           @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                           @ApiParam(value = "LCC converter station resource", required = true) @RequestBody List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {

        return updateAll(resources -> repository.updateLccConverterStations(networkId, variantNum, resources), lccConverterStationResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/lcc-converter-stations/{lccConverterStationId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete a lcc converter station by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully delete lcc converter station")
    })
    public ResponseEntity<Void> deleteLccConverterStation(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                          @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                          @ApiParam(value = "Lcc converter station ID", required = true) @PathVariable("lccConverterStationId") String lccConverterStationId) {
        repository.deleteLccConverterStation(networkId, variantNum, lccConverterStationId);
        return ResponseEntity.ok().build();
    }

    // static var compensator

    @PostMapping(value = "/{networkId}/{variantNum}/static-var-compensators")
    @ApiOperation(value = "Create static var compensators")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create static var compensators"))
    public ResponseEntity<Void> createStaticVarCompensators(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                            @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                            @ApiParam(value = "Static var compensator resources", required = true) @RequestBody List<Resource<StaticVarCompensatorAttributes>> staticVarCompenstatorResources) {
        return createAll(resource -> repository.createStaticVarCompensators(networkId, variantNum, resource), staticVarCompenstatorResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/static-var-compensators", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get static var compensators", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get static var compensator list"))
    public ResponseEntity<TopLevelDocument<StaticVarCompensatorAttributes>> getStaticVarCompensators(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                     @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                     @ApiParam(value = "Max number of static var compensators to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getStaticVarCompensators(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/static-var-compensators/{staticVarCompensatorId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a static var compensator by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get static var compensator"),
            @ApiResponse(code = 404, message = "Static var compensator has not been found")
        })
    public ResponseEntity<TopLevelDocument<StaticVarCompensatorAttributes>> getStaticVarCompensator(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                    @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                    @ApiParam(value = "Static var compensator ID", required = true) @PathVariable("staticVarCompensatorId") String staticVarCompensatorId) {
        return get(() -> repository.getStaticVarCompensator(networkId, variantNum, staticVarCompensatorId));
    }

    @PutMapping(value = "/{networkId}/{variantNum}/static-var-compensators")
    @ApiOperation(value = "Update static var compensators")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully update static var compensators"))
    public ResponseEntity<Void> updateStaticVarCompensators(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                            @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                            @ApiParam(value = "Static var compensator resource", required = true) @RequestBody List<Resource<StaticVarCompensatorAttributes>> staticVarCompensatorResources) {

        return updateAll(resources -> repository.updateStaticVarCompensators(networkId, variantNum, resources), staticVarCompensatorResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/static-var-compensators/{staticVarCompensatorId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete a static var compensator by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully delete static var compensator")
    })
    public ResponseEntity<Void> deleteStaticVarCompensator(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                           @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                           @ApiParam(value = "Static var compensator ID", required = true) @PathVariable("staticVarCompensatorId") String staticVarCompensatorId) {
        repository.deleteStaticVarCompensator(networkId, variantNum, staticVarCompensatorId);
        return ResponseEntity.ok().build();
    }


    // busbar section

    @PostMapping(value = "/{networkId}/{variantNum}/busbar-sections")
    @ApiOperation(value = "Create busbar sections")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create busbar sections"))
    public ResponseEntity<Void> createBusbarSections(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                     @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                     @ApiParam(value = "Busbar section resources", required = true) @RequestBody List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        return createAll(resource -> repository.createBusbarSections(networkId, variantNum, resource), busbarSectionResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/busbar-sections", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get busbar sections", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get busbar section list"))
    public ResponseEntity<TopLevelDocument<BusbarSectionAttributes>> getBusbarSections(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                       @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                       @ApiParam(value = "Max number of busbar section to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getBusbarSections(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/busbar-sections/{busbarSectionId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a busbar section by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get busbar section"),
            @ApiResponse(code = 404, message = "Busbar section has not been found")
        })
    public ResponseEntity<TopLevelDocument<BusbarSectionAttributes>> getBusbarSection(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                      @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                      @ApiParam(value = "Busbar section ID", required = true) @PathVariable("busbarSectionId") String busbarSectionId) {
        return get(() -> repository.getBusbarSection(networkId, variantNum, busbarSectionId));
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/busbar-sections/{busBarSectionId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete a bus bar section by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully delete bus bar section")
    })
    public ResponseEntity<Void> deleteBusBarSection(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                    @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                    @ApiParam(value = "Bus bar section ID", required = true) @PathVariable("busBarSectionId") String busBarSectionId) {
        repository.deleteBusBarSection(networkId, variantNum, busBarSectionId);
        return ResponseEntity.ok().build();
    }

    // switch

    @PostMapping(value = "/{networkId}/{variantNum}/switches")
    @ApiOperation(value = "Create switches")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create switches"))
    public ResponseEntity<Void> createSwitches(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                               @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                               @ApiParam(value = "Switch resource", required = true) @RequestBody List<Resource<SwitchAttributes>> switchResources) {
        return createAll(resources -> repository.createSwitches(networkId, variantNum, resources), switchResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/switches", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get switches", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get switch list"))
    public ResponseEntity<TopLevelDocument<SwitchAttributes>> getSwitches(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                          @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                          @ApiParam(value = "Max number of switch to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getSwitches(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/switches/{switchId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a switch by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get switch"),
            @ApiResponse(code = 404, message = "Switch has not been found")
    })
    public ResponseEntity<TopLevelDocument<SwitchAttributes>> getSwitch(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                        @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                        @ApiParam(value = "Switch ID", required = true) @PathVariable("switchId") String switchId) {
        return get(() -> repository.getSwitch(networkId, variantNum, switchId));
    }

    @PutMapping(value = "/{networkId}/{variantNum}/switches")
    @ApiOperation(value = "Update switches")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully update switches"))
    public ResponseEntity<Void> updateSwitches(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                               @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                               @ApiParam(value = "Switch resource", required = true) @RequestBody List<Resource<SwitchAttributes>> switchResources) {

        return updateAll(resources -> repository.updateSwitches(networkId, variantNum, resources), switchResources);
    }

    @DeleteMapping(value = "/{networkId}/switches/{switchId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete a switch by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully delete switch")
    })
    public ResponseEntity<Void> deleteSwitch(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                             @ApiParam(value = "Switch ID", required = true) @PathVariable("switchId") String switchId) {
        repository.deleteSwitch(networkId, switchId);
        return ResponseEntity.ok().build();
    }

    // 2 windings transformer

    @PostMapping(value = "/{networkId}/{variantNum}/2-windings-transformers")
    @ApiOperation(value = "Create 2 windings transformers")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create 2 windings transformers"))
    public ResponseEntity<Void> createTwoWindingsTransformers(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                              @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                              @ApiParam(value = "2 windings transformer resources", required = true) @RequestBody List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        return createAll(resource -> repository.createTwoWindingsTransformers(networkId, variantNum, resource), twoWindingsTransformerResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/2-windings-transformers", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get 2 windings transformers", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get 2 windings transformer list"))
    public ResponseEntity<TopLevelDocument<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                         @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                         @ApiParam(value = "Max number of 2 windings transformer to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getTwoWindingsTransformers(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/2-windings-transformers/{twoWindingsTransformerId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a 2 windings transformer by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get 2 windings transformer"),
            @ApiResponse(code = 404, message = "2 windings transformer has not been found")
        })
    public ResponseEntity<TopLevelDocument<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                        @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                        @ApiParam(value = "2 windings transformer ID", required = true) @PathVariable("twoWindingsTransformerId") String twoWindingsTransformerId) {
        return get(() -> repository.getTwoWindingsTransformer(networkId, variantNum, twoWindingsTransformerId));
    }

    @PutMapping(value = "/{networkId}/{variantNum}/2-windings-transformers")
    @ApiOperation(value = "Update 2 windings transformers")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully update 2 windings transformers"))
    public ResponseEntity<Void> updateTwoWindingsTransformers(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                              @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                              @ApiParam(value = "2 windings transformer resource", required = true) @RequestBody List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {

        return updateAll(resources -> repository.updateTwoWindingsTransformers(networkId, variantNum, resources), twoWindingsTransformerResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/2-windings-transformers/{twoWindingsTransformerId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete a 2 windings transformer by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully delete 2 windings transformer")
    })
    public ResponseEntity<Void> deleteTwoWindingsTransformer(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                             @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                             @ApiParam(value = "2 windings transformer ID", required = true) @PathVariable("twoWindingsTransformerId") String twoWindingsTransformerId) {
        repository.deleteTwoWindingsTransformer(networkId, variantNum, twoWindingsTransformerId);
        return ResponseEntity.ok().build();
    }

    // 3 windings transformer

    @PostMapping(value = "/{networkId}/{variantNum}/3-windings-transformers")
    @ApiOperation(value = "Create 3 windings transformers")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create 3 windings transformers"))
    public ResponseEntity<Void> createThreeWindingsTransformers(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                @ApiParam(value = "3 windings transformer resources", required = true) @RequestBody List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        return createAll(resource -> repository.createThreeWindingsTransformers(networkId, variantNum, resource), threeWindingsTransformerResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/3-windings-transformers", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get 3 windings transformers", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get 3 windings transformer list"))
    public ResponseEntity<TopLevelDocument<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                             @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                             @ApiParam(value = "Max number of 3 windings transformer to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getThreeWindingsTransformers(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/3-windings-transformers/{threeWindingsTransformerId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a 3 windings transformer by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get 3 windings transformer"),
            @ApiResponse(code = 404, message = "3 windings transformer has not been found")
        })
    public ResponseEntity<TopLevelDocument<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                            @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                            @ApiParam(value = "3 windings transformer ID", required = true) @PathVariable("threeWindingsTransformerId") String threeWindingsTransformerId) {
        return get(() -> repository.getThreeWindingsTransformer(networkId, variantNum, threeWindingsTransformerId));
    }

    @PutMapping(value = "/{networkId}/{variantNum}/3-windings-transformers")
    @ApiOperation(value = "Update 3 windings transformers")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully update 3 windings transformers"))
    public ResponseEntity<Void> updateThreeWindingsTransformers(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                @ApiParam(value = "3 windings transformer resource", required = true) @RequestBody List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {

        return updateAll(resources -> repository.updateThreeWindingsTransformers(networkId, variantNum, resources), threeWindingsTransformerResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/3-windings-transformers/{threeWindingsTransformerId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete a 3 windings transformer by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully delete 3 windings transformer")
    })
    public ResponseEntity<Void> deleteThreeWindingsTransformer(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                               @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                               @ApiParam(value = "3 windings transformer ID", required = true) @PathVariable("threeWindingsTransformerId") String threeWindingsTransformerId) {
        repository.deleteThreeWindingsTransformer(networkId, variantNum, threeWindingsTransformerId);
        return ResponseEntity.ok().build();
    }

    // line

    @PostMapping(value = "/{networkId}/{variantNum}/lines")
    @ApiOperation(value = "Create lines")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create lines"))
    public ResponseEntity<Void> createLines(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                            @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                            @ApiParam(value = "line resources", required = true) @RequestBody List<Resource<LineAttributes>> lineResources) {
        return createAll(resource -> repository.createLines(networkId, variantNum, resource), lineResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/lines", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get lines", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get line list"))
    public ResponseEntity<TopLevelDocument<LineAttributes>> getLines(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                     @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                     @ApiParam(value = "Max number of line to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getLines(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/lines/{lineId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a line by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get line"),
            @ApiResponse(code = 404, message = "line has not been found")
        })
    public ResponseEntity<TopLevelDocument<LineAttributes>> getLine(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                    @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                    @ApiParam(value = "Line ID", required = true) @PathVariable("lineId") String lineId) {
        return get(() -> repository.getLine(networkId, variantNum, lineId));
    }

    @PutMapping(value = "/{networkId}/{variantNum}/lines")
    @ApiOperation(value = "Update lines")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully update lines"))
    public ResponseEntity<Void> updateLines(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                            @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                            @ApiParam(value = "line resource", required = true) @RequestBody List<Resource<LineAttributes>> lineResources) {

        return updateAll(resources -> repository.updateLines(networkId, variantNum, resources), lineResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/lines/{lineId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete a line by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully delete line")
    })
    public ResponseEntity<Void> deleteLine(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                           @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                           @ApiParam(value = "Line ID", required = true) @PathVariable("lineId") String lineId) {
        repository.deleteLine(networkId, variantNum, lineId);
        return ResponseEntity.ok().build();
    }

    // hvdc line

    @PostMapping(value = "/{networkId}/{variantNum}/hvdc-lines")
    @ApiOperation(value = "Create hvdc lines")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create hvdc lines"))
    public ResponseEntity<Void> createHvdcLines(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                @ApiParam(value = "Hvdc line resources", required = true) @RequestBody List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        return createAll(resource -> repository.createHvdcLines(networkId, variantNum, resource), hvdcLineResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/hvdc-lines", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get hvdc lines", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get hvdc line list"))
    public ResponseEntity<TopLevelDocument<HvdcLineAttributes>> getHvdcLines(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                             @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                             @ApiParam(value = "Max number of hvdc line to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getHvdcLines(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/hvdc-lines/{hvdcLineId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a hvdc line by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get hvdc line"),
            @ApiResponse(code = 404, message = "Hvdc line has not been found")
        })
    public ResponseEntity<TopLevelDocument<HvdcLineAttributes>> getHvdcLine(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                            @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                            @ApiParam(value = "Hvdc line ID", required = true) @PathVariable("hvdcLineId") String hvdcLineId) {
        return get(() -> repository.getHvdcLine(networkId, variantNum, hvdcLineId));
    }

    @PutMapping(value = "/{networkId}/{variantNum}/hvdc-lines")
    @ApiOperation(value = "Update hvdc lines")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully update hvdc lines"))
    public ResponseEntity<Void> updateHvdcLines(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                @ApiParam(value = "hvdc line resource", required = true) @RequestBody List<Resource<HvdcLineAttributes>> hvdcLineResources) {

        return updateAll(resources -> repository.updateHvdcLines(networkId, variantNum, resources), hvdcLineResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/hvdc-lines/{hvdcLineId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete a hvdc line by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully delete hvdc line")
    })
    public ResponseEntity<Void> deleteHvdcLine(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                               @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                               @ApiParam(value = "Hvdc line ID", required = true) @PathVariable("hvdcLineId") String hvdcLineId) {
        repository.deleteHvdcLine(networkId, variantNum, hvdcLineId);
        return ResponseEntity.ok().build();
    }

    // dangling line

    @PostMapping(value = "/{networkId}/{variantNum}/dangling-lines")
    @ApiOperation(value = "Create dangling lines")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create dangling lines"))
    public ResponseEntity<Void> createDanglingLines(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                    @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                    @ApiParam(value = "Dangling line resources", required = true) @RequestBody List<Resource<DanglingLineAttributes>> danglingLineResources) {
        return createAll(resource -> repository.createDanglingLines(networkId, variantNum, resource), danglingLineResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/dangling-lines", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get dangling lines", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get dangling line list"))
    public ResponseEntity<TopLevelDocument<DanglingLineAttributes>> getDanglingLines(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                     @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                     @ApiParam(value = "Max number of dangling line to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getDanglingLines(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/dangling-lines/{danglingLineId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a dangling line by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get dangling line"),
            @ApiResponse(code = 404, message = "Dangling line has not been found")
        })
    public ResponseEntity<TopLevelDocument<DanglingLineAttributes>> getDanglingLine(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                    @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                    @ApiParam(value = "Dangling line ID", required = true) @PathVariable("danglingLineId") String danglingLineId) {
        return get(() -> repository.getDanglingLine(networkId, variantNum, danglingLineId));
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/dangling-lines/{danglingLineId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete a dangling line by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully delete dangling line")
    })
    public ResponseEntity<Void> deleteDanglingLine(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                   @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                   @ApiParam(value = "Dangling line ID", required = true) @PathVariable("danglingLineId") String danglingLineId) {
        repository.deleteDanglingLine(networkId, variantNum, danglingLineId);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/{networkId}/{variantNum}/dangling-lines")
    @ApiOperation(value = "Update dangling lines")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully update dangling lines"))
    public ResponseEntity<Void> updateDanglingLines(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                    @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                    @ApiParam(value = "dangling line resource", required = true) @RequestBody List<Resource<DanglingLineAttributes>> danglingLineResources) {

        return updateAll(resources -> repository.updateDanglingLines(networkId, variantNum, resources), danglingLineResources);
    }

    // buses

    @PostMapping(value = "/{networkId}/{variantNum}/configured-buses")
    @ApiOperation(value = "Create buses")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully create buses"))
    public ResponseEntity<Void> createBuses(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                            @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                            @ApiParam(value = "Buses resources", required = true) @RequestBody List<Resource<ConfiguredBusAttributes>> busesResources) {
        return createAll(resource -> repository.createBuses(networkId, variantNum, busesResources), busesResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/configured-buses", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get buses", response = TopLevelDocument.class)
    @ApiResponses(@ApiResponse(code = 200, message = "Successfully get buses list"))
    public ResponseEntity<TopLevelDocument<ConfiguredBusAttributes>> getBuses(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                              @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                              @ApiParam(value = "Max number of buses to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getConfiguredBuses(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/configured-buses/{busId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a bus by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get bus"),
            @ApiResponse(code = 404, message = "bus has not been found")
    })
    public ResponseEntity<TopLevelDocument<ConfiguredBusAttributes>> getBuses(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                              @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                              @ApiParam(value = "bus ID", required = true) @PathVariable("busId") String busId) {
        return get(() -> repository.getConfiguredBus(networkId, variantNum, busId));
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-level/{voltageLevelId}/configured-buses", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a bus by id", response = TopLevelDocument.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully get buses"),
            @ApiResponse(code = 404, message = "bus has not been found")
    })
    public ResponseEntity<TopLevelDocument<ConfiguredBusAttributes>> getVoltageLevelBuses(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                          @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                          @ApiParam(value = "voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelBuses(networkId, variantNum, voltageLevelId), null);
    }

    @PutMapping(value = "/{networkId}/{variantNum}/configured-buses")
    @ApiOperation(value = "Update buses")
    @ApiResponses(@ApiResponse(code = 201, message = "Successfully update buses"))
    public ResponseEntity<Void> updateBuses(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                            @ApiParam(value = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                            @ApiParam(value = "bus resource", required = true) @RequestBody List<Resource<ConfiguredBusAttributes>> busResources) {

        return updateAll(resources -> repository.updateBuses(networkId, variantNum, resources), busResources);
    }

    @DeleteMapping(value = "/{networkId}/configured-buses/{busId}", produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete a bus by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully delete bus")
    })
    public ResponseEntity<Void> deleteBus(@ApiParam(value = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                          @ApiParam(value = "Bus ID", required = true) @PathVariable("busId") String busId) {
        repository.deleteBus(networkId, busId);
        return ResponseEntity.ok().build();
    }
}
