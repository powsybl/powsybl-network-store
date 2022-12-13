/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server;

import com.powsybl.network.store.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@RestController
@RequestMapping(value = "/" + NetworkStoreApi.VERSION + "/networks")
@Tag(name = "Network store")
public class NetworkStoreController {

    @Autowired
    private NetworkStoreRepository repository;

    private <T extends IdentifiableAttributes> ResponseEntity<TopLevelDocument<T>> get(Supplier<Optional<Resource<T>>> f) {
        return f.get()
                .map(resource -> ResponseEntity.ok(TopLevelDocument.of(resource)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(TopLevelDocument.empty()));
    }

    private <T extends IdentifiableAttributes> ResponseEntity<Void> createAll(Consumer<List<Resource<T>>> f, List<Resource<T>> resources) {
        f.accept(resources);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private <T extends Attributes> ResponseEntity<Void> updateAll(Consumer<List<Resource<T>>> f, List<Resource<T>> resources) {
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
    @Operation(summary = "Get all networks infos")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get all networks infos"))
    public List<NetworkInfos> getNetworksInfos() {
        return repository.getNetworksInfos();
    }

    @GetMapping(value = "/{networkId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get variants infos for a given network")
    @ApiResponses(value = @ApiResponse(responseCode = "200", description = "Successfully get variants infos"))
    public List<VariantInfos> getNetworks(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID id) {
        return repository.getVariantsInfos(id);
    }

    @GetMapping(value = "/{networkId}/{variantNum}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a network by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully get network"),
            @ApiResponse(responseCode = "404", description = "Network has not been found")
    })
    public ResponseEntity<TopLevelDocument<NetworkAttributes>> getNetwork(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID id,
                                                                          @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum) {
        return get(() -> repository.getNetwork(id, variantNum));
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Create networks")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully create networks"))
    public ResponseEntity<Void> createNetworks(@Parameter(description = "Network resources", required = true) @RequestBody List<Resource<NetworkAttributes>> networkResources) {
        return createAll(repository::createNetworks, networkResources);
    }

    @DeleteMapping(value = "/{networkId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a network by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully delete network"),
            @ApiResponse(responseCode = "404", description = "Network has not been found")
        })
    public ResponseEntity<Void> deleteNetwork(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID id) {
        repository.deleteNetwork(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a network by id (only one variant)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully delete network variant"),
            @ApiResponse(responseCode = "404", description = "Network has not been found")
    })
    public ResponseEntity<Void> deleteNetwork(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID id,
                                              @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum) {
        repository.deleteNetwork(id, variantNum);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/{networkId}")
    @Operation(summary = "Update network")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update network"))
    public ResponseEntity<Void> updateNetwork(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                              @Parameter(description = "network resource", required = true) @RequestBody Resource<NetworkAttributes> networkResources) {

        return updateAll(resources -> repository.updateNetworks(resources), Collections.singletonList(networkResources));
    }

    @PutMapping(value = "/{networkId}/{sourceVariantNum}/to/{targetVariantNum}")
    @Operation(summary = "Clone a network variant")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully clone the network variant"))
    public ResponseEntity<Void> cloneNetworkVariant(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                             @Parameter(description = "Source variant number", required = true) @PathVariable("sourceVariantNum") int sourceVariantNum,
                                             @Parameter(description = "Target variant number", required = true) @PathVariable("targetVariantNum") int targetVariantNum,
                                             @Parameter(description = "Target variant id", required = true) @RequestParam(required = false) String targetVariantId) {
        repository.cloneNetworkVariant(networkId, sourceVariantNum, targetVariantNum, targetVariantId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{targetNetworkUuid}")
    @Operation(summary = "Clone a network provided variants to a different network")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully clone the network"))
    public ResponseEntity<Void> cloneNetwork(@Parameter(description = "Target network ID", required = true) @PathVariable("targetNetworkUuid") UUID targetNetworkUuid,
                                             @Parameter(description = "Source network ID", required = true) @RequestParam("duplicateFrom") UUID sourceNetworkId,
                                             @Parameter(description = "List of target variant ID", required = true) @RequestParam("targetVariantIds") List<String> targetVariantIds) {
        repository.cloneNetwork(targetNetworkUuid, sourceNetworkId, targetVariantIds);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/{networkId}/{sourceVariantId}/toId/{targetVariantId}")
    @Operation(summary = "Clone a network variant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully clone the network variant"),
            @ApiResponse(responseCode = ErrorObject.CLONE_OVER_EXISTING_STATUS, description = ErrorObject.CLONE_OVER_EXISTING_TITLE),
            @ApiResponse(responseCode = ErrorObject.CLONE_OVER_INITIAL_FORBIDDEN_STATUS, description = ErrorObject.CLONE_OVER_INITIAL_FORBIDDEN_TITLE),
        })
    public ResponseEntity<Void> cloneNetwork(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                             @Parameter(description = "Source variant Id", required = true) @PathVariable("sourceVariantId") String sourceVariantId,
                                             @Parameter(description = "Target variant Id", required = true) @PathVariable("targetVariantId") String targetVariantId,
                                             @Parameter(description = "mayOverwrite", required = false) @RequestParam(required = false) boolean mayOverwrite) {
        repository.cloneNetwork(networkId, sourceVariantId, targetVariantId, mayOverwrite);
        return ResponseEntity.ok().build();
    }

    // substation

    @GetMapping(value = "/{networkId}/{variantNum}/substations", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get substations")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get substation list"))
    public ResponseEntity<TopLevelDocument<SubstationAttributes>> getSubstations(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                 @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                 @Parameter(description = "Max number of substation to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getSubstations(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/substations/{substationId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a substation by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully get substation"),
            @ApiResponse(responseCode = "404", description = "Substation has not been found")
        })
    public ResponseEntity<TopLevelDocument<SubstationAttributes>> getSubstation(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                @Parameter(description = "Substation ID", required = true) @PathVariable("substationId") String substationId) {
        return get(() -> repository.getSubstation(networkId, variantNum, substationId));
    }

    @PostMapping(value = "/{networkId}/substations")
    @Operation(summary = "Create substations")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully substations"))
    public ResponseEntity<Void> createSubstations(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                  @Parameter(description = "Substation resources", required = true) @RequestBody List<Resource<SubstationAttributes>> substationResources) {
        return createAll(resource -> repository.createSubstations(networkId, resource), substationResources);
    }

    @PutMapping(value = "/{networkId}/substations")
    @Operation(summary = "Update substations")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update substations"))
    public ResponseEntity<Void> updateSubstations(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                            @Parameter(description = "substation resource", required = true) @RequestBody List<Resource<SubstationAttributes>> substationsResources) {
        return updateAll(resources -> repository.updateSubstations(networkId, resources), substationsResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/substations/{substationId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a substation by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully delete substation")
    })
    public ResponseEntity<Void> deleteSubstation(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                 @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                 @Parameter(description = "Substation ID", required = true) @PathVariable("substationId") String substationId) {
        repository.deleteSubstation(networkId, variantNum, substationId);
        return ResponseEntity.ok().build();
    }

    // voltage level

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get voltage levels")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get voltage level list"))
    public ResponseEntity<TopLevelDocument<VoltageLevelAttributes>> getVoltageLevels(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                     @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                     @Parameter(description = "Max number of voltage level to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getVoltageLevels(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a voltage level by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully get voltage level"),
            @ApiResponse(responseCode = "404", description = "Voltage level has not been found")
        })
    public ResponseEntity<TopLevelDocument<VoltageLevelAttributes>> getVoltageLevel(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                    @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                    @Parameter(description = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return get(() -> repository.getVoltageLevel(networkId, variantNum, voltageLevelId));
    }

    @PostMapping(value = "/{networkId}/voltage-levels")
    @Operation(summary = "Create voltage levels")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully create voltage levels"))
    public ResponseEntity<Void> createVoltageLevels(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                    @Parameter(description = "Voltage level resources", required = true) @RequestBody List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        return createAll(resource -> repository.createVoltageLevels(networkId, resource), voltageLevelResources);
    }

    @PutMapping(value = "/{networkId}/voltage-levels")
    @Operation(summary = "Update voltage levels")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update voltage levels"))
    public ResponseEntity<Void> updateVoltageLevels(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                    @Parameter(description = "voltage level resources", required = true) @RequestBody List<Resource<VoltageLevelAttributes>> voltageLevelResources) {

        return updateAll(resources -> repository.updateVoltageLevels(networkId, resources), voltageLevelResources);
    }

    @PutMapping(value = "/{networkId}/voltage-levels/sv")
    @Operation(summary = "Update voltage levels SV")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update voltage levels SV"))
    public ResponseEntity<Void> updateVoltageLevelsSv(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                      @Parameter(description = "voltage level SV resources", required = true) @RequestBody List<Resource<VoltageLevelSvAttributes>> voltageLevelResources) {

        return updateAll(resources -> repository.updateVoltageLevelsSv(networkId, resources), voltageLevelResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a voltage level by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully delete voltage level")
    })
    public ResponseEntity<Void> deleteVoltageLevel(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                   @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                   @Parameter(description = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        repository.deleteVoltageLevel(networkId, variantNum, voltageLevelId);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/{networkId}/{variantNum}/substations/{substationId}/voltage-levels", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get voltage levels for a substation")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get voltage level list for a substation"))
    public ResponseEntity<TopLevelDocument<VoltageLevelAttributes>> getVoltageLevels(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                     @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                     @Parameter(description = "Substation ID", required = true) @PathVariable("substationId") String substationId) {
        return getAll(() -> repository.getVoltageLevels(networkId, variantNum, substationId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/busbar-sections", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get busbar sections connected to voltage level")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get busbar sections connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<BusbarSectionAttributes>> getVoltageLevelBusbarSections(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                   @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                   @Parameter(description = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelBusbarSections(networkId, variantNum, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/switches", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get switches connected to voltage level")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get switches connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<SwitchAttributes>> getVoltageLevelSwitches(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                      @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                      @Parameter(description = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelSwitches(networkId, variantNum, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/generators", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get generators connected to voltage level")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get generators connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<GeneratorAttributes>> getVoltageLevelGenerators(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                           @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                           @Parameter(description = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelGenerators(networkId, variantNum, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/batteries", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get batteries connected to voltage level")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get batteries connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<BatteryAttributes>> getVoltageLevelBatteries(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                        @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                        @Parameter(description = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelBatteries(networkId, variantNum, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/loads", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get loads connected to voltage level")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get loads connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<LoadAttributes>> getVoltageLevelLoads(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                 @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                 @Parameter(description = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelLoads(networkId, variantNum, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/shunt-compensators", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get shunt compensators connected to voltage level")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get shunt compensators connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                         @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                         @Parameter(description = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelShuntCompensators(networkId, variantNum, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/vsc-converter-stations", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get static VSC converter stations connected to voltage level")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get VSC converter stations connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                               @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                               @Parameter(description = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelVscConverterStations(networkId, variantNum, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/lcc-converter-stations", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get static LCC converter stations connected to voltage level")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get LCC converter stations connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                               @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                               @Parameter(description = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelLccConverterStations(networkId, variantNum, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/static-var-compensators", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get static var compensators connected to voltage level")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get static var compensators connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                                 @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                                 @Parameter(description = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelStaticVarCompensators(networkId, variantNum, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/2-windings-transformers", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get 2 windings transformers connected to voltage level")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get 2 windings transformers connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                                     @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                                     @Parameter(description = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelTwoWindingsTransformers(networkId, variantNum, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/3-windings-transformers", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get 3 windings transformers connected to voltage level")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get 3 windings transformers connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                                         @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                                         @Parameter(description = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelThreeWindingsTransformers(networkId, variantNum, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/lines", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get lines connected to voltage level")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get lines connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<LineAttributes>> getVoltageLevelLines(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                 @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                 @Parameter(description = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelLines(networkId, variantNum, voltageLevelId), null);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/dangling-lines", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get dangling lines connected to voltage level")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get dangling lines connected to the voltage level"))
    public ResponseEntity<TopLevelDocument<DanglingLineAttributes>> getVoltageLevelDanglingLines(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                 @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                 @Parameter(description = "Voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelDanglingLines(networkId, variantNum, voltageLevelId), null);
    }

    // generator

    @PostMapping(value = "/{networkId}/generators")
    @Operation(summary = "Create generators")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully create generators"))
    public ResponseEntity<Void> createGenerators(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                 @Parameter(description = "Generator resources", required = true) @RequestBody List<Resource<GeneratorAttributes>> generatorResources) {
        return createAll(resource -> repository.createGenerators(networkId, resource), generatorResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/generators", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get generators")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get generator list"))
    public ResponseEntity<TopLevelDocument<GeneratorAttributes>> getGenerators(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                               @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                               @Parameter(description = "Max number of generator to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getGenerators(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/generators/{generatorId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a generator by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully get generator"),
            @ApiResponse(responseCode = "404", description = "Generator has not been found")
    })
    public ResponseEntity<TopLevelDocument<GeneratorAttributes>> getGenerator(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                              @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                              @Parameter(description = "Generator ID", required = true) @PathVariable("generatorId") String generatorId) {
        return get(() -> repository.getGenerator(networkId, variantNum, generatorId));
    }

    @PutMapping(value = "/{networkId}/generators")
    @Operation(summary = "Update generators")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update generators"))
    public ResponseEntity<Void> updateGenerators(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                 @Parameter(description = "generator resources", required = true) @RequestBody List<Resource<GeneratorAttributes>> generatorResources) {

        return updateAll(resources -> repository.updateGenerators(networkId, resources), generatorResources);
    }

    @PutMapping(value = "/{networkId}/generators/sv")
    @Operation(summary = "Update generators SV")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update generators SV"))
    public ResponseEntity<Void> updateGeneratorsSv(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                   @Parameter(description = "generator SV resources", required = true) @RequestBody List<Resource<InjectionSvAttributes>> generatorResources) {

        return updateAll(resources -> repository.updateGeneratorsSv(networkId, resources), generatorResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/generators/{generatorId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a generator by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully delete generator")
    })
    public ResponseEntity<Void> deleteGenerator(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                @Parameter(description = "Generator ID", required = true) @PathVariable("generatorId") String generatorId) {
        repository.deleteGenerator(networkId, variantNum, generatorId);
        return ResponseEntity.ok().build();
    }

    // battery

    @PostMapping(value = "/{networkId}/batteries")
    @Operation(summary = "Create batteries")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully create batteries"))
    public ResponseEntity<Void> createBatteries(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                @Parameter(description = "Battery resources", required = true) @RequestBody List<Resource<BatteryAttributes>> batteryResources) {
        return createAll(resource -> repository.createBatteries(networkId, resource), batteryResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/batteries", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get batteries")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get batteries list"))
    public ResponseEntity<TopLevelDocument<BatteryAttributes>> getBatteries(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                            @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                            @Parameter(description = "Max number of batteries to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getBatteries(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/batteries/{batteryId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a battery by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully get battery"),
            @ApiResponse(responseCode = "404", description = "Battery has not been found")
    })
    public ResponseEntity<TopLevelDocument<BatteryAttributes>> getBattery(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                          @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                          @Parameter(description = "Battery ID", required = true) @PathVariable("batteryId") String batteryId) {
        return get(() -> repository.getBattery(networkId, variantNum, batteryId));
    }

    @PutMapping(value = "/{networkId}/batteries")
    @Operation(summary = "Update batteries")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update batteries"))
    public ResponseEntity<Void> updateBatteries(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                @Parameter(description = "Battery resources", required = true) @RequestBody List<Resource<BatteryAttributes>> batteryResources) {

        return updateAll(resources -> repository.updateBatteries(networkId, resources), batteryResources);
    }

    @PutMapping(value = "/{networkId}/batteries/sv")
    @Operation(summary = "Update batteries SV")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update batteries SV"))
    public ResponseEntity<Void> updateBatteriesSv(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                  @Parameter(description = "Battery SV resources", required = true) @RequestBody List<Resource<InjectionSvAttributes>> batteryResources) {

        return updateAll(resources -> repository.updateBatteriesSv(networkId, resources), batteryResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/batteries/{batteryId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a battery by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully delete battery")
    })
    public ResponseEntity<Void> deleteBattery(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                              @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                              @Parameter(description = "Battery ID", required = true) @PathVariable("batteryId") String batteryId) {
        repository.deleteBattery(networkId, variantNum, batteryId);
        return ResponseEntity.ok().build();
    }

    // load

    @PostMapping(value = "/{networkId}/loads")
    @Operation(summary = "Create loads")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully create loads"))
    public ResponseEntity<Void> createLoads(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                            @Parameter(description = "Load resources", required = true) @RequestBody List<Resource<LoadAttributes>> loadResources) {
        return createAll(resource -> repository.createLoads(networkId, resource), loadResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/loads", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get loads")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get load list"))
    public ResponseEntity<TopLevelDocument<LoadAttributes>> getLoads(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                     @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                     @Parameter(description = "Max number of load to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getLoads(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/loads/{loadId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a load by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully get load"),
            @ApiResponse(responseCode = "404", description = "Load has not been found")
        })
    public ResponseEntity<TopLevelDocument<LoadAttributes>> getLoad(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                    @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                    @Parameter(description = "Load ID", required = true) @PathVariable("loadId") String loadId) {
        return get(() -> repository.getLoad(networkId, variantNum, loadId));
    }

    @PutMapping(value = "/{networkId}/loads")
    @Operation(summary = "Update loads")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update loads"))
    public ResponseEntity<Void> updateLoads(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                            @Parameter(description = "load resources", required = true) @RequestBody List<Resource<LoadAttributes>> loadResources) {

        return updateAll(resources -> repository.updateLoads(networkId, resources), loadResources);
    }

    @PutMapping(value = "/{networkId}/loads/sv")
    @Operation(summary = "Update loads SV")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update loads SV"))
    public ResponseEntity<Void> updateLoadsSv(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                              @Parameter(description = "load SV resources", required = true) @RequestBody List<Resource<InjectionSvAttributes>> loadResources) {

        return updateAll(resources -> repository.updateLoadsSv(networkId, resources), loadResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/loads/{loadId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a load by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully delete load")
    })
    public ResponseEntity<Void> deleteLoad(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                           @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                           @Parameter(description = "Load ID", required = true) @PathVariable("loadId") String loadId) {
        repository.deleteLoad(networkId, variantNum, loadId);
        return ResponseEntity.ok().build();
    }

    // shunt compensator

    @PostMapping(value = "/{networkId}/shunt-compensators")
    @Operation(summary = "Create shunt compensators")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully create shunt compensators"))
    public ResponseEntity<Void> createShuntCompensators(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                        @Parameter(description = "Shunt compensator resources", required = true) @RequestBody List<Resource<ShuntCompensatorAttributes>> shuntResources) {
        return createAll(resource -> repository.createShuntCompensators(networkId, resource), shuntResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/shunt-compensators", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get shunt compensators")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get shunt compensator list"))
    public ResponseEntity<TopLevelDocument<ShuntCompensatorAttributes>> getShuntCompensators(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                             @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                             @Parameter(description = "Max number of shunt compensator to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getShuntCompensators(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/shunt-compensators/{shuntCompensatorId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a shunt compensator by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully get shunt compensator"),
            @ApiResponse(responseCode = "404", description = "Shunt compensator has not been found")
        })
    public ResponseEntity<TopLevelDocument<ShuntCompensatorAttributes>> getShuntCompensator(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                            @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                            @Parameter(description = "Shunt compensator ID", required = true) @PathVariable("shuntCompensatorId") String shuntCompensatorId) {
        return get(() -> repository.getShuntCompensator(networkId, variantNum, shuntCompensatorId));
    }

    @PutMapping(value = "/{networkId}/shunt-compensators")
    @Operation(summary = "Update shunt compensators")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update shunt compensators"))
    public ResponseEntity<Void> updateShuntCompensators(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                        @Parameter(description = "shunt compensator resources", required = true) @RequestBody List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {

        return updateAll(resources -> repository.updateShuntCompensators(networkId, resources), shuntCompensatorResources);
    }

    @PutMapping(value = "/{networkId}/shunt-compensators/sv")
    @Operation(summary = "Update shunt compensators SV")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update shunt compensators SV"))
    public ResponseEntity<Void> updateShuntCompensatorsSv(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                          @Parameter(description = "shunt compensator SV resources", required = true) @RequestBody List<Resource<InjectionSvAttributes>> shuntCompensatorResources) {

        return updateAll(resources -> repository.updateShuntCompensatorsSv(networkId, resources), shuntCompensatorResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/shunt-compensators/{shuntCompensatorId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a shunt compensator by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully delete shunt compensator")
    })
    public ResponseEntity<Void> deleteShuntCompensator(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                       @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                       @Parameter(description = "Shunt compensator ID", required = true) @PathVariable("shuntCompensatorId") String shuntCompensatorId) {
        repository.deleteShuntCompensator(networkId, variantNum, shuntCompensatorId);
        return ResponseEntity.ok().build();
    }

    // VSC converter station

    @PostMapping(value = "/{networkId}/vsc-converter-stations")
    @Operation(summary = "Create VSC converter stations")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully create VSC converter stations"))
    public ResponseEntity<Void> createVscConverterStations(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                           @Parameter(description = "VSC converter station resources", required = true) @RequestBody List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        return createAll(resource -> repository.createVscConverterStations(networkId, resource), vscConverterStationResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/vsc-converter-stations", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get VSC converter stations")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get VSC converter stations list"))
    public ResponseEntity<TopLevelDocument<VscConverterStationAttributes>> getVscConverterStations(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                   @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                   @Parameter(description = "Max number of VSC converter stations to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getVscConverterStations(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/vsc-converter-stations/{vscConverterStationId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a VSC converter station by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully get VSC converter station"),
            @ApiResponse(responseCode = "404", description = "VSC converter station has not been found")
        })
    public ResponseEntity<TopLevelDocument<VscConverterStationAttributes>> getVscConverterStation(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                  @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                  @Parameter(description = "VSC converter station ID", required = true) @PathVariable("vscConverterStationId") String vscConverterStationId) {
        return get(() -> repository.getVscConverterStation(networkId, variantNum, vscConverterStationId));
    }

    @PutMapping(value = "/{networkId}/vsc-converter-stations")
    @Operation(summary = "Update VSC converter stations")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update VSC converter stations"))
    public ResponseEntity<Void> updateVscConverterStationsSv(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                             @Parameter(description = "VSC converter station resources", required = true) @RequestBody List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {

        return updateAll(resources -> repository.updateVscConverterStations(networkId, resources), vscConverterStationResources);
    }

    @PutMapping(value = "/{networkId}/vsc-converter-stations/sv")
    @Operation(summary = "Update VSC converter stations SV")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update VSC converter stations SV"))
    public ResponseEntity<Void> updateVscConverterStations(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                           @Parameter(description = "VSC converter station SV resources", required = true) @RequestBody List<Resource<InjectionSvAttributes>> vscConverterStationResources) {

        return updateAll(resources -> repository.updateVscConverterStationsSv(networkId, resources), vscConverterStationResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/vsc-converter-stations/{vscConverterStationId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a vsc converter station by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully delete vsc converter station")
    })
    public ResponseEntity<Void> deleteVscConverterStation(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                          @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                          @Parameter(description = "Vsc converter station ID", required = true) @PathVariable("vscConverterStationId") String vscConverterStationId) {
        repository.deleteVscConverterStation(networkId, variantNum, vscConverterStationId);
        return ResponseEntity.ok().build();
    }

    // LCC converter station

    @PostMapping(value = "/{networkId}/lcc-converter-stations")
    @Operation(summary = "Create LCC converter stations")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully create LCC converter stations"))
    public ResponseEntity<Void> createLccConverterStations(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                           @Parameter(description = "LCC converter station resources", required = true) @RequestBody List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        return createAll(resource -> repository.createLccConverterStations(networkId, resource), lccConverterStationResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/lcc-converter-stations", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get LCC converter stations")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get LCC converter stations list"))
    public ResponseEntity<TopLevelDocument<LccConverterStationAttributes>> getLccConverterStations(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                   @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                   @Parameter(description = "Max number of LCC converter stations to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getLccConverterStations(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/lcc-converter-stations/{lccConverterStationId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a LCC converter station by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully get LCC converter station"),
            @ApiResponse(responseCode = "404", description = "LCC converter station has not been found")
        })
    public ResponseEntity<TopLevelDocument<LccConverterStationAttributes>> getLccConverterStation(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                  @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                  @Parameter(description = "LCC converter station ID", required = true) @PathVariable("lccConverterStationId") String lccConverterStationId) {
        return get(() -> repository.getLccConverterStation(networkId, variantNum, lccConverterStationId));
    }

    @PutMapping(value = "/{networkId}/lcc-converter-stations")
    @Operation(summary = "Update LCC converter stations")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update LCC converter stations"))
    public ResponseEntity<Void> updateLccConverterStations(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                           @Parameter(description = "LCC converter station resources", required = true) @RequestBody List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {

        return updateAll(resources -> repository.updateLccConverterStations(networkId, resources), lccConverterStationResources);
    }

    @PutMapping(value = "/{networkId}/lcc-converter-stations/sv")
    @Operation(summary = "Update LCC converter stations SV")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update LCC converter stations SV"))
    public ResponseEntity<Void> updateLccConverterStationsSv(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                             @Parameter(description = "LCC converter station SV resources", required = true) @RequestBody List<Resource<InjectionSvAttributes>> lccConverterStationResources) {

        return updateAll(resources -> repository.updateLccConverterStationsSv(networkId, resources), lccConverterStationResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/lcc-converter-stations/{lccConverterStationId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a lcc converter station by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully delete lcc converter station")
    })
    public ResponseEntity<Void> deleteLccConverterStation(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                          @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                          @Parameter(description = "Lcc converter station ID", required = true) @PathVariable("lccConverterStationId") String lccConverterStationId) {
        repository.deleteLccConverterStation(networkId, variantNum, lccConverterStationId);
        return ResponseEntity.ok().build();
    }

    // static var compensator

    @PostMapping(value = "/{networkId}/static-var-compensators")
    @Operation(summary = "Create static var compensators")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully create static var compensators"))
    public ResponseEntity<Void> createStaticVarCompensators(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                            @Parameter(description = "Static var compensator resources", required = true) @RequestBody List<Resource<StaticVarCompensatorAttributes>> staticVarCompenstatorResources) {
        return createAll(resource -> repository.createStaticVarCompensators(networkId, resource), staticVarCompenstatorResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/static-var-compensators", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get static var compensators")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get static var compensator list"))
    public ResponseEntity<TopLevelDocument<StaticVarCompensatorAttributes>> getStaticVarCompensators(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                     @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                     @Parameter(description = "Max number of static var compensators to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getStaticVarCompensators(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/static-var-compensators/{staticVarCompensatorId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a static var compensator by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully get static var compensator"),
            @ApiResponse(responseCode = "404", description = "Static var compensator has not been found")
        })
    public ResponseEntity<TopLevelDocument<StaticVarCompensatorAttributes>> getStaticVarCompensator(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                    @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                    @Parameter(description = "Static var compensator ID", required = true) @PathVariable("staticVarCompensatorId") String staticVarCompensatorId) {
        return get(() -> repository.getStaticVarCompensator(networkId, variantNum, staticVarCompensatorId));
    }

    @PutMapping(value = "/{networkId}/static-var-compensators")
    @Operation(summary = "Update static var compensators")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update static var compensators"))
    public ResponseEntity<Void> updateStaticVarCompensators(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                           @Parameter(description = "Static var compensator resources", required = true) @RequestBody List<Resource<StaticVarCompensatorAttributes>> staticVarCompensatorResources) {

        return updateAll(resources -> repository.updateStaticVarCompensators(networkId, resources), staticVarCompensatorResources);
    }

    @PutMapping(value = "/{networkId}/static-var-compensators/sv")
    @Operation(summary = "Update static var compensators SV")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update static var compensators SV"))
    public ResponseEntity<Void> updateStaticVarCompensatorsSv(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                              @Parameter(description = "Static var compensator SV resources", required = true) @RequestBody List<Resource<InjectionSvAttributes>> staticVarCompensatorResources) {

        return updateAll(resources -> repository.updateStaticVarCompensatorsSv(networkId, resources), staticVarCompensatorResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/static-var-compensators/{staticVarCompensatorId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a static var compensator by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully delete static var compensator")
    })
    public ResponseEntity<Void> deleteStaticVarCompensator(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                           @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                           @Parameter(description = "Static var compensator ID", required = true) @PathVariable("staticVarCompensatorId") String staticVarCompensatorId) {
        repository.deleteStaticVarCompensator(networkId, variantNum, staticVarCompensatorId);
        return ResponseEntity.ok().build();
    }


    // busbar section

    @PostMapping(value = "/{networkId}/busbar-sections")
    @Operation(summary = "Create busbar sections")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully create busbar sections"))
    public ResponseEntity<Void> createBusbarSections(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                     @Parameter(description = "Busbar section resources", required = true) @RequestBody List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        return createAll(resource -> repository.createBusbarSections(networkId, resource), busbarSectionResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/busbar-sections", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get busbar sections")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get busbar section list"))
    public ResponseEntity<TopLevelDocument<BusbarSectionAttributes>> getBusbarSections(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                       @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                       @Parameter(description = "Max number of busbar section to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getBusbarSections(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/busbar-sections/{busbarSectionId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a busbar section by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully get busbar section"),
            @ApiResponse(responseCode = "404", description = "Busbar section has not been found")
        })
    public ResponseEntity<TopLevelDocument<BusbarSectionAttributes>> getBusbarSection(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                      @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                      @Parameter(description = "Busbar section ID", required = true) @PathVariable("busbarSectionId") String busbarSectionId) {
        return get(() -> repository.getBusbarSection(networkId, variantNum, busbarSectionId));
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/busbar-sections/{busBarSectionId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a bus bar section by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully delete bus bar section")
    })
    public ResponseEntity<Void> deleteBusBarSection(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                    @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                    @Parameter(description = "Bus bar section ID", required = true) @PathVariable("busBarSectionId") String busBarSectionId) {
        repository.deleteBusBarSection(networkId, variantNum, busBarSectionId);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/{networkId}/busbar-sections")
    @Operation(summary = "Update busbar sections")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update busbar sections"))
    public ResponseEntity<Void> updateBusbarSections(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                 @Parameter(description = "busbarsection resource", required = true) @RequestBody List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        return updateAll(resources -> repository.updateBusbarSections(networkId, resources), busbarSectionResources);
    }

    // switch

    @PostMapping(value = "/{networkId}/switches")
    @Operation(summary = "Create switches")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully create switches"))
    public ResponseEntity<Void> createSwitches(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                               @Parameter(description = "Switch resource", required = true) @RequestBody List<Resource<SwitchAttributes>> switchResources) {
        return createAll(resources -> repository.createSwitches(networkId, resources), switchResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/switches", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get switches")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get switch list"))
    public ResponseEntity<TopLevelDocument<SwitchAttributes>> getSwitches(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                          @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                          @Parameter(description = "Max number of switch to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getSwitches(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/switches/{switchId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a switch by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully get switch"),
            @ApiResponse(responseCode = "404", description = "Switch has not been found")
    })
    public ResponseEntity<TopLevelDocument<SwitchAttributes>> getSwitch(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                        @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                        @Parameter(description = "Switch ID", required = true) @PathVariable("switchId") String switchId) {
        return get(() -> repository.getSwitch(networkId, variantNum, switchId));
    }

    @PutMapping(value = "/{networkId}/switches")
    @Operation(summary = "Update switches")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update switches"))
    public ResponseEntity<Void> updateSwitches(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                               @Parameter(description = "Switch resource", required = true) @RequestBody List<Resource<SwitchAttributes>> switchResources) {

        return updateAll(resources -> repository.updateSwitches(networkId, resources), switchResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/switches/{switchId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a switch by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully delete switch")
    })
    public ResponseEntity<Void> deleteSwitch(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                             @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                             @Parameter(description = "Switch ID", required = true) @PathVariable("switchId") String switchId) {
        repository.deleteSwitch(networkId, variantNum, switchId);
        return ResponseEntity.ok().build();
    }

    // 2 windings transformer

    @PostMapping(value = "/{networkId}/2-windings-transformers")
    @Operation(summary = "Create 2 windings transformers")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully create 2 windings transformers"))
    public ResponseEntity<Void> createTwoWindingsTransformers(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                              @Parameter(description = "2 windings transformer resources", required = true) @RequestBody List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        return createAll(resource -> repository.createTwoWindingsTransformers(networkId, resource), twoWindingsTransformerResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/2-windings-transformers", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get 2 windings transformers")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get 2 windings transformer list"))
    public ResponseEntity<TopLevelDocument<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                         @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                         @Parameter(description = "Max number of 2 windings transformer to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getTwoWindingsTransformers(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/2-windings-transformers/{twoWindingsTransformerId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a 2 windings transformer by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully get 2 windings transformer"),
            @ApiResponse(responseCode = "404", description = "2 windings transformer has not been found")
        })
    public ResponseEntity<TopLevelDocument<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                        @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                        @Parameter(description = "2 windings transformer ID", required = true) @PathVariable("twoWindingsTransformerId") String twoWindingsTransformerId) {
        return get(() -> repository.getTwoWindingsTransformer(networkId, variantNum, twoWindingsTransformerId));
    }

    @PutMapping(value = "/{networkId}/2-windings-transformers")
    @Operation(summary = "Update 2 windings transformers")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update 2 windings transformers"))
    public ResponseEntity<Void> updateTwoWindingsTransformers(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                              @Parameter(description = "2 windings transformer resources", required = true) @RequestBody List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {

        return updateAll(resources -> repository.updateTwoWindingsTransformers(networkId, resources), twoWindingsTransformerResources);
    }

    @PutMapping(value = "/{networkId}/2-windings-transformers/sv")
    @Operation(summary = "Update 2 windings transformers SV")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update 2 windings transformers SV"))
    public ResponseEntity<Void> updateTwoWindingsTransformersSv(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                @Parameter(description = "2 windings transformer SV resources", required = true) @RequestBody List<Resource<BranchSvAttributes>> twoWindingsTransformerResources) {

        return updateAll(resources -> repository.updateTwoWindingsTransformersSv(networkId, resources), twoWindingsTransformerResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/2-windings-transformers/{twoWindingsTransformerId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a 2 windings transformer by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully delete 2 windings transformer")
    })
    public ResponseEntity<Void> deleteTwoWindingsTransformer(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                             @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                             @Parameter(description = "2 windings transformer ID", required = true) @PathVariable("twoWindingsTransformerId") String twoWindingsTransformerId) {
        repository.deleteTwoWindingsTransformer(networkId, variantNum, twoWindingsTransformerId);
        return ResponseEntity.ok().build();
    }

    // 3 windings transformer

    @PostMapping(value = "/{networkId}/3-windings-transformers")
    @Operation(summary = "Create 3 windings transformers")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully create 3 windings transformers"))
    public ResponseEntity<Void> createThreeWindingsTransformers(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                @Parameter(description = "3 windings transformer resources", required = true) @RequestBody List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        return createAll(resource -> repository.createThreeWindingsTransformers(networkId, resource), threeWindingsTransformerResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/3-windings-transformers", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get 3 windings transformers")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get 3 windings transformer list"))
    public ResponseEntity<TopLevelDocument<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                             @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                             @Parameter(description = "Max number of 3 windings transformer to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getThreeWindingsTransformers(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/3-windings-transformers/{threeWindingsTransformerId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a 3 windings transformer by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully get 3 windings transformer"),
            @ApiResponse(responseCode = "404", description = "3 windings transformer has not been found")
        })
    public ResponseEntity<TopLevelDocument<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                                            @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                                            @Parameter(description = "3 windings transformer ID", required = true) @PathVariable("threeWindingsTransformerId") String threeWindingsTransformerId) {
        return get(() -> repository.getThreeWindingsTransformer(networkId, variantNum, threeWindingsTransformerId));
    }

    @PutMapping(value = "/{networkId}/3-windings-transformers")
    @Operation(summary = "Update 3 windings transformers")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update 3 windings transformers"))
    public ResponseEntity<Void> updateThreeWindingsTransformers(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                              @Parameter(description = "3 windings transformer resources", required = true) @RequestBody List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {

        return updateAll(resources -> repository.updateThreeWindingsTransformers(networkId, resources), threeWindingsTransformerResources);
    }

    @PutMapping(value = "/{networkId}/3-windings-transformers/sv")
    @Operation(summary = "Update 3 windings transformers SV")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update 3 windings transformers SV"))
    public ResponseEntity<Void> updateThreeWindingsTransformersSv(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                  @Parameter(description = "3 windings transformer SV resources", required = true) @RequestBody List<Resource<ThreeWindingsTransformerSvAttributes>> threeWindingsTransformerResources) {

        return updateAll(resources -> repository.updateThreeWindingsTransformersSv(networkId, resources), threeWindingsTransformerResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/3-windings-transformers/{threeWindingsTransformerId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a 3 windings transformer by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully delete 3 windings transformer")
    })
    public ResponseEntity<Void> deleteThreeWindingsTransformer(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                               @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                               @Parameter(description = "3 windings transformer ID", required = true) @PathVariable("threeWindingsTransformerId") String threeWindingsTransformerId) {
        repository.deleteThreeWindingsTransformer(networkId, variantNum, threeWindingsTransformerId);
        return ResponseEntity.ok().build();
    }

    // line

    @PostMapping(value = "/{networkId}/lines")
    @Operation(summary = "Create lines")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully create lines"))
    public ResponseEntity<Void> createLines(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                            @Parameter(description = "line resources", required = true) @RequestBody List<Resource<LineAttributes>> lineResources) {
        return createAll(resource -> repository.createLines(networkId, resource), lineResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/lines", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get lines")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get line list"))
    public ResponseEntity<TopLevelDocument<LineAttributes>> getLines(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                     @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                     @Parameter(description = "Max number of line to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getLines(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/lines/{lineId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a line by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully get line"),
            @ApiResponse(responseCode = "404", description = "line has not been found")
        })
    public ResponseEntity<TopLevelDocument<LineAttributes>> getLine(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                    @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                    @Parameter(description = "Line ID", required = true) @PathVariable("lineId") String lineId) {
        return get(() -> repository.getLine(networkId, variantNum, lineId));
    }

    @PutMapping(value = "/{networkId}/lines")
    @Operation(summary = "Update lines")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update lines"))
    public ResponseEntity<Void> updateLines(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                            @Parameter(description = "line resources", required = true) @RequestBody List<Resource<LineAttributes>> lineResources) {

        return updateAll(resources -> repository.updateLines(networkId, resources), lineResources);
    }

    @PutMapping(value = "/{networkId}/lines/sv")
    @Operation(summary = "Update lines SV")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update lines SV"))
    public ResponseEntity<Void> updateLinesSv(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                              @Parameter(description = "line SV resources", required = true) @RequestBody List<Resource<BranchSvAttributes>> lineResources) {

        return updateAll(resources -> repository.updateLinesSv(networkId, resources), lineResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/lines/{lineId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a line by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully delete line")
    })
    public ResponseEntity<Void> deleteLine(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                           @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                           @Parameter(description = "Line ID", required = true) @PathVariable("lineId") String lineId) {
        repository.deleteLine(networkId, variantNum, lineId);
        return ResponseEntity.ok().build();
    }

    // hvdc line

    @PostMapping(value = "/{networkId}/hvdc-lines")
    @Operation(summary = "Create hvdc lines")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully create hvdc lines"))
    public ResponseEntity<Void> createHvdcLines(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                @Parameter(description = "Hvdc line resources", required = true) @RequestBody List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        return createAll(resource -> repository.createHvdcLines(networkId, resource), hvdcLineResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/hvdc-lines", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get hvdc lines")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get hvdc line list"))
    public ResponseEntity<TopLevelDocument<HvdcLineAttributes>> getHvdcLines(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                             @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                             @Parameter(description = "Max number of hvdc line to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getHvdcLines(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/hvdc-lines/{hvdcLineId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a hvdc line by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully get hvdc line"),
            @ApiResponse(responseCode = "404", description = "Hvdc line has not been found")
        })
    public ResponseEntity<TopLevelDocument<HvdcLineAttributes>> getHvdcLine(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                            @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                            @Parameter(description = "Hvdc line ID", required = true) @PathVariable("hvdcLineId") String hvdcLineId) {
        return get(() -> repository.getHvdcLine(networkId, variantNum, hvdcLineId));
    }

    @PutMapping(value = "/{networkId}/hvdc-lines")
    @Operation(summary = "Update hvdc lines")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update hvdc lines"))
    public ResponseEntity<Void> updateHvdcLines(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                            @Parameter(description = "hvdc line resource", required = true) @RequestBody List<Resource<HvdcLineAttributes>> hvdcLineResources) {

        return updateAll(resources -> repository.updateHvdcLines(networkId, resources), hvdcLineResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/hvdc-lines/{hvdcLineId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a hvdc line by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully delete hvdc line")
    })
    public ResponseEntity<Void> deleteHvdcLine(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                               @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                               @Parameter(description = "Hvdc line ID", required = true) @PathVariable("hvdcLineId") String hvdcLineId) {
        repository.deleteHvdcLine(networkId, variantNum, hvdcLineId);
        return ResponseEntity.ok().build();
    }

    // dangling line

    @PostMapping(value = "/{networkId}/dangling-lines")
    @Operation(summary = "Create dangling lines")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully create dangling lines"))
    public ResponseEntity<Void> createDanglingLines(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                    @Parameter(description = "Dangling line resources", required = true) @RequestBody List<Resource<DanglingLineAttributes>> danglingLineResources) {
        return createAll(resource -> repository.createDanglingLines(networkId, resource), danglingLineResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/dangling-lines", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get dangling lines")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get dangling line list"))
    public ResponseEntity<TopLevelDocument<DanglingLineAttributes>> getDanglingLines(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                     @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                     @Parameter(description = "Max number of dangling line to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getDanglingLines(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/dangling-lines/{danglingLineId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a dangling line by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully get dangling line"),
            @ApiResponse(responseCode = "404", description = "Dangling line has not been found")
        })
    public ResponseEntity<TopLevelDocument<DanglingLineAttributes>> getDanglingLine(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                    @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                    @Parameter(description = "Dangling line ID", required = true) @PathVariable("danglingLineId") String danglingLineId) {
        return get(() -> repository.getDanglingLine(networkId, variantNum, danglingLineId));
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/dangling-lines/{danglingLineId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a dangling line by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully delete dangling line")
    })
    public ResponseEntity<Void> deleteDanglingLine(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                   @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                   @Parameter(description = "Dangling line ID", required = true) @PathVariable("danglingLineId") String danglingLineId) {
        repository.deleteDanglingLine(networkId, variantNum, danglingLineId);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/{networkId}/dangling-lines")
    @Operation(summary = "Update dangling lines")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update dangling lines"))
    public ResponseEntity<Void> updateDanglingLines(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                    @Parameter(description = "dangling line resources", required = true) @RequestBody List<Resource<DanglingLineAttributes>> danglingLineResources) {

        return updateAll(resources -> repository.updateDanglingLines(networkId, resources), danglingLineResources);
    }

    @PutMapping(value = "/{networkId}/dangling-lines/sv")
    @Operation(summary = "Update dangling lines SV")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update dangling lines SV"))
    public ResponseEntity<Void> updateDanglingLinesSv(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                      @Parameter(description = "dangling line SV resources", required = true) @RequestBody List<Resource<InjectionSvAttributes>> danglingLineResources) {

        return updateAll(resources -> repository.updateDanglingLinesSv(networkId, resources), danglingLineResources);
    }

    // buses

    @PostMapping(value = "/{networkId}/configured-buses")
    @Operation(summary = "Create buses")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully create buses"))
    public ResponseEntity<Void> createBuses(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                              @Parameter(description = "Buses resources", required = true) @RequestBody List<Resource<ConfiguredBusAttributes>> busesResources) {
        return createAll(resource -> repository.createBuses(networkId, busesResources), busesResources);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/configured-buses", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get buses")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Successfully get buses list"))
    public ResponseEntity<TopLevelDocument<ConfiguredBusAttributes>> getBuses(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                              @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                              @Parameter(description = "Max number of buses to get") @RequestParam(required = false) Integer limit) {
        return getAll(() -> repository.getConfiguredBuses(networkId, variantNum), limit);
    }

    @GetMapping(value = "/{networkId}/{variantNum}/configured-buses/{busId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a bus by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully get bus"),
            @ApiResponse(responseCode = "404", description = "bus has not been found")
    })
    public ResponseEntity<TopLevelDocument<ConfiguredBusAttributes>> getBuses(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                              @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                              @Parameter(description = "bus ID", required = true) @PathVariable("busId") String busId) {
        return get(() -> repository.getConfiguredBus(networkId, variantNum, busId));
    }

    @GetMapping(value = "/{networkId}/{variantNum}/voltage-levels/{voltageLevelId}/configured-buses", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a bus by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully get buses"),
            @ApiResponse(responseCode = "404", description = "bus has not been found")
    })
    public ResponseEntity<TopLevelDocument<ConfiguredBusAttributes>> getVoltageLevelBuses(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                          @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                          @Parameter(description = "voltage level ID", required = true) @PathVariable("voltageLevelId") String voltageLevelId) {
        return getAll(() -> repository.getVoltageLevelBuses(networkId, variantNum, voltageLevelId), null);
    }

    @PutMapping(value = "/{networkId}/configured-buses")
    @Operation(summary = "Update buses")
    @ApiResponses(@ApiResponse(responseCode = "201", description = "Successfully update buses"))
    public ResponseEntity<Void> updateBuses(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                            @Parameter(description = "bus resource", required = true) @RequestBody List<Resource<ConfiguredBusAttributes>> busResources) {

        return updateAll(resources -> repository.updateBuses(networkId, resources), busResources);
    }

    @DeleteMapping(value = "/{networkId}/{variantNum}/configured-buses/{busId}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a bus by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully delete bus")
    })
    public ResponseEntity<Void> deleteBus(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                          @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                          @Parameter(description = "Bus ID", required = true) @PathVariable("busId") String busId) {
        repository.deleteBus(networkId, variantNum, busId);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/{networkId}/{variantNum}/identifiables/{id}", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Get an identifiable by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully get the identifiable"),
            @ApiResponse(responseCode = "404", description = "The identifiable has not been found")
    })
    public ResponseEntity<TopLevelDocument<IdentifiableAttributes>> getIdentifiable(@Parameter(description = "Network ID", required = true) @PathVariable("networkId") UUID networkId,
                                                                                    @Parameter(description = "Variant number", required = true) @PathVariable("variantNum") int variantNum,
                                                                                    @Parameter(description = "Identifiable ID", required = true) @PathVariable("id") String id) {
        return get(() -> repository.getIdentifiable(networkId, variantNum, id));
    }
}
