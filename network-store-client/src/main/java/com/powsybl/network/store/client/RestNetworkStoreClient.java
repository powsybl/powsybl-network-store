/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.powsybl.commons.PowsyblException;
import com.powsybl.network.store.iidm.impl.NetworkStoreClient;
import com.powsybl.network.store.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class RestNetworkStoreClient implements NetworkStoreClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestNetworkStoreClient.class);

    private static final int RESOURCES_CREATION_CHUNK_SIZE = 1000;
    public static final String STR_RETRYING = "Retrying...";
    private static final String STR_NETWORK = "network";
    private static final String URL_NETWORK_UUID = "/networks/{networkUuid}";
    private static final String STR_SUBSTATION = "substation";
    private static final String STR_VOLTAGE_LEVEL = "voltage level";
    private static final String STR_BUSBAR_SECTION = "busbar section";
    private static final String STR_SWITCH = "switch";
    private static final String STR_GENERATOR = "generator";
    private static final String STR_BATTERY = "battery";
    private static final String STR_SHUNT_COMPENSATOR = "shunt compensator";
    private static final String STR_VSC_CONVERTER_STATION = "VSC converter station";
    private static final String STR_LCC_CONVERTER_STATION = "LCC converter station";
    private static final String STR_STATIC_VAR_COMPENSATOR = "static var compensator";
    private static final String STR_TWO_WINDINGS_TRANSFORMER = "2 windings transformer";
    private static final String STR_THREE_WINDINGS_TRANSFORMER = "3 windings transformer";
    private static final String STR_DANGLING_LINE = "dangling line";
    private static final String STR_HVDC_LINE = "hvdc line";
    private static final String STR_TIE_LINE = "tie line";
    private static final String STR_AREA = "area";
    private static final String STR_GROUND = "ground";

    private final RestClient restClient;

    private final ObjectMapper objectMapper;

    public RestNetworkStoreClient(RestClient restClient) {
        this(restClient, new ObjectMapper());
    }

    public RestNetworkStoreClient(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = Objects.requireNonNull(restClient);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        objectMapper.registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
            .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

    }

    // network

    private <T extends IdentifiableAttributes> void create(String target, String url, List<Resource<T>> resourceList, Object... uriVariables) {
        for (List<Resource<T>> resourcePartition : Lists.partition(resourceList, RESOURCES_CREATION_CHUNK_SIZE)) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Creating {} {} resources ({})...", resourcePartition.size(), target, UriComponentsBuilder.fromUriString(url).buildAndExpand(uriVariables));
            }
            Stopwatch stopwatch = Stopwatch.createStarted();
            try {
                restClient.createAll(url, resourcePartition, uriVariables);
            } catch (ResourceAccessException e) {
                LOGGER.error(e.toString(), e);
                // retry only one time
                LOGGER.info(STR_RETRYING);
                restClient.createAll(url, resourcePartition, uriVariables);
            }
            stopwatch.stop();
            LOGGER.info("{} {} resources created in {} ms", resourcePartition.size(), target, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }

    private <T extends IdentifiableAttributes> List<Resource<T>> getAll(String target, String url, Object... uriVariables) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Loading {} resources {}", target, UriComponentsBuilder.fromUriString(url).buildAndExpand(uriVariables));
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Resource<T>> resourceList = restClient.getAll(target, url, uriVariables);
        stopwatch.stop();
        LOGGER.info("{} {} resources loaded in {} ms", resourceList.size(), target, stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return resourceList;
    }

    private <T extends IdentifiableAttributes> Optional<Resource<T>> get(String target, String url, Object... uriVariables) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Loading {} resource {}", target, UriComponentsBuilder.fromUriString(url).buildAndExpand(uriVariables));
        }
        Stopwatch stopwatch = Stopwatch.createStarted();
        Optional<Resource<T>> resource = restClient.getOne(target, url, uriVariables);
        stopwatch.stop();
        LOGGER.info("{} resource (empty={}) loaded in {} ms", target, resource.isEmpty(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return resource;
    }

    private Optional<ExtensionAttributes> getExtensionAttributes(String urlTemplate, Object... uriVariables) {
        logGetExtensionAttributesUrl(urlTemplate, uriVariables);
        Stopwatch stopwatch = Stopwatch.createStarted();
        Optional<ExtensionAttributes> rawExtensionAttributes = restClient.getOneExtensionAttributes(urlTemplate, uriVariables);
        boolean wasFiltered = rawExtensionAttributes.filter(RawExtensionAttributes.class::isInstance).isPresent();
        Optional<ExtensionAttributes> filteredExtensionAttributes = rawExtensionAttributes.filter(attr -> !(attr instanceof RawExtensionAttributes));
        stopwatch.stop();
        logGetExtensionAttributesTime(filteredExtensionAttributes.isPresent() ? 1 : 0, stopwatch.elapsed(TimeUnit.MILLISECONDS), wasFiltered ? 1 : 0);

        return filteredExtensionAttributes;
    }

    private Map<String, ExtensionAttributes> getExtensionAttributesMap(String urlTemplate, Object... uriVariables) {
        logGetExtensionAttributesUrl(urlTemplate, uriVariables);
        Stopwatch stopwatch = Stopwatch.createStarted();
        Map<String, ExtensionAttributes> rawExtensionAttributes = restClient.get(urlTemplate, new ParameterizedTypeReference<>() { }, uriVariables);
        Map<String, ExtensionAttributes> filteredExtensionAttributes = filterRawExtensionAttributes(rawExtensionAttributes);
        int filteredCount = rawExtensionAttributes.size() - filteredExtensionAttributes.size();

        stopwatch.stop();
        logGetExtensionAttributesTime(filteredExtensionAttributes.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS), filteredCount);

        return filteredExtensionAttributes;
    }

    private Map<String, Map<String, ExtensionAttributes>> getExtensionAttributesNestedMap(String urlTemplate, Object... uriVariables) {
        logGetExtensionAttributesUrl(urlTemplate, uriVariables);
        Stopwatch stopwatch = Stopwatch.createStarted();
        Map<String, Map<String, ExtensionAttributes>> rawExtensionAttributes = restClient.get(urlTemplate, new ParameterizedTypeReference<>() { }, uriVariables);
        Map<String, Map<String, ExtensionAttributes>> filteredExtensionAttributes = new HashMap<>();
        long filteredAttributesCount = 0;
        for (Map.Entry<String, Map<String, ExtensionAttributes>> entry : rawExtensionAttributes.entrySet()) {
            Map<String, ExtensionAttributes> filteredInnerMap = filterRawExtensionAttributes(entry.getValue());
            if (!filteredInnerMap.isEmpty()) {
                filteredExtensionAttributes.put(entry.getKey(), filteredInnerMap);
            }
            filteredAttributesCount += entry.getValue().size() - filteredInnerMap.size();
        }
        stopwatch.stop();
        long loadedAttributesCount = filteredExtensionAttributes.values().stream().mapToLong(Map::size).sum();
        logGetExtensionAttributesTime(loadedAttributesCount, stopwatch.elapsed(TimeUnit.MILLISECONDS), filteredAttributesCount);

        return filteredExtensionAttributes;
    }

    private static Map<String, ExtensionAttributes> filterRawExtensionAttributes(Map<String, ExtensionAttributes> extensionAttributes) {
        return extensionAttributes.entrySet().stream()
                .filter(entry -> !(entry.getValue() instanceof RawExtensionAttributes))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Optional<OperationalLimitsGroupAttributes> getOperationalLimitsGroupAttributes(String urlTemplate, Object... uriVariables) {
        logGetOperationalLimitsGroupAttributesUrl(urlTemplate, uriVariables);
        Stopwatch stopwatch = Stopwatch.createStarted();
        Optional<OperationalLimitsGroupAttributes> operationalLimitsGroupAttributes = restClient.getOneOperationalLimitsGroupAttributes(urlTemplate, uriVariables);
        stopwatch.stop();
        logGetOperationalLimitsGroupAttributesTime(operationalLimitsGroupAttributes.isPresent() ? 1 : 0, stopwatch.elapsed(TimeUnit.MILLISECONDS));

        return operationalLimitsGroupAttributes;
    }

    private List<OperationalLimitsGroupAttributes> getOperationalLimitsGroupAttributesForBranch(String urlTemplate, Object... uriVariables) {
        logGetOperationalLimitsGroupAttributesUrl(urlTemplate, uriVariables);
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<OperationalLimitsGroupAttributes> operationalLimitsGroupAttributesList = restClient.get(urlTemplate, new ParameterizedTypeReference<>() { }, uriVariables);
        stopwatch.stop();
        logGetOperationalLimitsGroupAttributesTime(operationalLimitsGroupAttributesList.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));

        return operationalLimitsGroupAttributesList;
    }

    private Map<String, Map<Integer, Map<String, OperationalLimitsGroupAttributes>>> getOperationalLimitsGroupAttributesNestedMap(String urlTemplate, Object... uriVariables) {
        logGetOperationalLimitsGroupAttributesUrl(urlTemplate, uriVariables);
        Stopwatch stopwatch = Stopwatch.createStarted();
        Map<String, Map<Integer, Map<String, OperationalLimitsGroupAttributes>>> operationalLimitsGroupAttributes = restClient.get(urlTemplate, new ParameterizedTypeReference<>() { }, uriVariables);
        stopwatch.stop();
        AtomicLong loadedAttributesCount = new AtomicLong();
        operationalLimitsGroupAttributes.values().forEach(map1 ->
            map1.values().forEach(map2 -> loadedAttributesCount.addAndGet(map2.size())));
        logGetOperationalLimitsGroupAttributesTime(loadedAttributesCount.get(), stopwatch.elapsed(TimeUnit.MILLISECONDS));

        return operationalLimitsGroupAttributes;
    }

    private static void logGetExtensionAttributesUrl(String urlTemplate, Object... uriVariables) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Loading extension attributes {}", UriComponentsBuilder.fromUriString(urlTemplate).build(uriVariables));
        }
    }

    private static void logGetExtensionAttributesTime(long loadedAttributesCount, long timeElapsed, long filteredAttributesCount) {
        if (filteredAttributesCount > 0) {
            LOGGER.info("{} extension attributes loaded in {} ms ({} ignored due to missing deserializer in classpath)", loadedAttributesCount, timeElapsed, filteredAttributesCount);
        } else {
            LOGGER.info("{} extension attributes loaded in {} ms", loadedAttributesCount, timeElapsed);
        }
    }

    private static void logGetOperationalLimitsGroupAttributesUrl(String urlTemplate, Object... uriVariables) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Loading operational limits group attributes {}", UriComponentsBuilder.fromUriString(urlTemplate).build(uriVariables));
        }
    }

    private static void logGetOperationalLimitsGroupAttributesTime(long loadedAttributesCount, long timeElapsed) {
        LOGGER.info("{} operational limits group attributes loaded in {} ms", loadedAttributesCount, timeElapsed);
    }

    private <T extends IdentifiableAttributes> void updatePartition(String target, String url, AttributeFilter attributeFilter, List<Resource<T>> resources, Object[] uriVariables) {
        if (attributeFilter == null) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Updating {} {} resources ({})...", resources.size(), target, UriComponentsBuilder.fromUriString(url).buildAndExpand(uriVariables));
            }
            restClient.updateAll(url, resources, uriVariables);
        } else {
            List<Resource<Attributes>> filteredResources = resources.stream()
                    .map(resource -> resource.filterAttributes(attributeFilter))
                    .collect(Collectors.toList());
            String filteredUrl = url + "/" + attributeFilter.name().toLowerCase();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Updating {} {} {} resources ({})...", filteredResources.size(), target, attributeFilter, UriComponentsBuilder.fromUriString(filteredUrl).buildAndExpand(uriVariables));
            }
            restClient.updateAll(filteredUrl, filteredResources, uriVariables);
        }
    }

    private <T extends IdentifiableAttributes> void updateAll(String target, String url, List<Resource<T>> resources, AttributeFilter attributeFilter, Object... uriVariables) {
        for (List<Resource<T>> resourcePartition : Lists.partition(resources, RESOURCES_CREATION_CHUNK_SIZE)) {
            Stopwatch stopwatch = Stopwatch.createStarted();
            try {
                updatePartition(target, url, attributeFilter, resourcePartition, uriVariables);
            } catch (ResourceAccessException e) {
                LOGGER.error(e.toString(), e);
                // retry only one time
                LOGGER.info(STR_RETRYING);
                updatePartition(target, url, attributeFilter, resourcePartition, uriVariables);
            }
            stopwatch.stop();
            if (LOGGER.isInfoEnabled()) {
                if (attributeFilter == null) {
                    LOGGER.info("{} {} resources updated in {} ms", resourcePartition.size(), target, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                } else {
                    LOGGER.info("{} {} {} resources updated in {} ms", resourcePartition.size(), target, attributeFilter, stopwatch.elapsed(TimeUnit.MILLISECONDS));
                }
            }
        }
    }

    private void removeAll(String target, String url, UUID networkUuid, int variantNum, List<String> ids) {
        for (List<String> idsPartition : Lists.partition(ids, RESOURCES_CREATION_CHUNK_SIZE)) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Deleting {} {} resources ({})...", idsPartition.size(), target, UriComponentsBuilder.fromUriString(url).buildAndExpand(networkUuid, variantNum));
            }
            Stopwatch stopwatch = Stopwatch.createStarted();
            try {
                restClient.deleteAll(url, idsPartition, networkUuid, variantNum);
            } catch (ResourceAccessException e) {
                LOGGER.error(e.toString(), e);
                // retry only one time
                LOGGER.info(STR_RETRYING);
                restClient.deleteAll(url, idsPartition, networkUuid, variantNum);
            }
            stopwatch.stop();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("{} {} resources deleted in {} ms", idsPartition.size(), target, stopwatch.elapsed(TimeUnit.MILLISECONDS));
            }
        }
    }

    @Override
    public List<NetworkInfos> getNetworksInfos() {
        return restClient.get("/networks", new ParameterizedTypeReference<>() {
        });
    }

    @Override
    public void createNetworks(List<Resource<NetworkAttributes>> networkResources) {
        create(STR_NETWORK, "/networks", networkResources);
    }

    @Override
    public List<VariantInfos> getVariantsInfos(UUID networkUuid, boolean disableCache) {
        return restClient.get(URL_NETWORK_UUID, new ParameterizedTypeReference<>() {
        }, networkUuid);
    }

    @Override
    public Optional<Resource<NetworkAttributes>> getNetwork(UUID networkUuid, int variantNum) {
        return get(STR_NETWORK, "/networks/{networkUuid}/{variantNum}", networkUuid, variantNum);
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        LOGGER.info("Removing network {}", networkUuid);
        restClient.delete(URL_NETWORK_UUID, networkUuid);
    }

    @Override
    public void deleteNetwork(UUID networkUuid, int variantNum) {
        LOGGER.info("Removing network {} variant {}", networkUuid, variantNum);
        restClient.delete("/networks/{networkUuid}/{variantNum}", networkUuid, variantNum);
    }

    @Override
    public void updateNetworks(List<Resource<NetworkAttributes>> networkResources, AttributeFilter attributeFilter) {
        for (Resource<NetworkAttributes> networkResource : networkResources) {
            updateAll(STR_NETWORK, URL_NETWORK_UUID, Collections.singletonList(networkResource), attributeFilter, networkResource.getAttributes().getUuid());
        }
    }

    @Override
    public void cloneNetwork(UUID networkUuid, int sourceVariantNum, int targetVariantNum, String targetVariantId) {
        LOGGER.info("Cloning network {} variant {} to variant {} (variantId='{}')", networkUuid, sourceVariantNum, targetVariantNum, targetVariantId);
        restClient.put("/networks/{networkUuid}/{sourceVariantNum}/to/{targetVariantNum}?targetVariantId={targetVariantId}",
                networkUuid, sourceVariantNum, targetVariantNum, targetVariantId);
    }

    @Override
    public void cloneNetwork(UUID networkUuid, String sourceVariantId, String targetVariantId, boolean mayOverwrite) {
        LOGGER.info("Cloning network {} variantId {} to variantId {}", networkUuid, sourceVariantId, targetVariantId);
        try {
            restClient.put("/networks/{networkUuid}/{sourceVariantId}/toId/{targetVariantId}?mayOverwrite={mayOverwrite}",
                    networkUuid, sourceVariantId, targetVariantId, mayOverwrite);
        } catch (HttpClientErrorException ex) {
            String body = ex.getResponseBodyAsString();
            Optional<TopLevelError> optError = RestTemplateResponseErrorHandler.parseJsonApiError(body, objectMapper);
            if (optError.isPresent()) {
                TopLevelError error = optError.get();
                Optional<ErrorObject> errorCloneOverExisting = error.getErrors().stream()
                        .filter(eo -> ErrorObject.CLONE_OVER_EXISTING_CODE.equals(eo.getCode())).findAny();
                if (errorCloneOverExisting.isPresent()) {
                    throw new PowsyblException(errorCloneOverExisting.get().getDetail(), ex);
                }
                Optional<ErrorObject> errorCloneOverInitial = error.getErrors().stream()
                        .filter(eo -> ErrorObject.CLONE_OVER_INITIAL_FORBIDDEN_CODE.equals(eo.getCode())).findAny();
                if (errorCloneOverInitial.isPresent()) {
                    throw new PowsyblException(errorCloneOverInitial.get().getTitle(), ex);
                }
            }
            throw ex;
        }
    }

    public void cloneNetwork(UUID targetNetworkUuid, UUID sourceNetworkUuid, List<String> targetVariantIds) {
        LOGGER.info("Duplicating network {} into network {}", sourceNetworkUuid, targetNetworkUuid);
        restClient.post("/networks/{targetNetworkUuid}?duplicateFrom={sourceNetworkId}&targetVariantIds={targetVariantIds}", targetNetworkUuid, sourceNetworkUuid, String.join(",", targetVariantIds));
    }
    // substation

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        create(STR_SUBSTATION, "/networks/{networkUuid}/substations", substationResources, networkUuid);
    }

    @Override
    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid, int variantNum) {
        return getAll(STR_SUBSTATION, "/networks/{networkUuid}/{variantNum}/substations", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, int variantNum, String substationId) {
        return get(STR_SUBSTATION, "/networks/{networkUuid}/{variantNum}/substations/{substationId}", networkUuid, variantNum, substationId);
    }

    @Override
    public void updateSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources, AttributeFilter attributeFilter) {
        updateAll(STR_SUBSTATION, "/networks/{networkUuid}/substations", substationResources, attributeFilter, networkUuid);
    }

    public void removeSubstations(UUID networkUuid, int variantNum, List<String> substationsId) {
        removeAll(STR_SUBSTATION, "/networks/{networkUuid}/{variantNum}/substations", networkUuid, variantNum, substationsId);
    }

    // voltage level

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        create(STR_VOLTAGE_LEVEL, "/networks/{networkUuid}/voltage-levels", voltageLevelResources, networkUuid);
    }

    @Override
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, int variantNum, String voltageLevelId) {
        return get(STR_VOLTAGE_LEVEL, "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid, int variantNum) {
        return getAll(STR_VOLTAGE_LEVEL, "/networks/{networkUuid}/{variantNum}/voltage-levels", networkUuid, variantNum);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, int variantNum, String substationId) {
        return getAll(STR_VOLTAGE_LEVEL, "/networks/{networkUuid}/{variantNum}/substations/{substationId}/voltage-levels", networkUuid, variantNum, substationId);
    }

    @Override
    public void updateVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelsResources, AttributeFilter attributeFilter) {
        updateAll(STR_VOLTAGE_LEVEL, "/networks/{networkUuid}/voltage-levels", voltageLevelsResources, attributeFilter, networkUuid);
    }

    @Override
    public void removeVoltageLevels(UUID networkUuid, int variantNum, List<String> voltageLevelsId) {
        removeAll(STR_VOLTAGE_LEVEL, "/networks/{networkUuid}/{variantNum}/voltage-levels", networkUuid, variantNum, voltageLevelsId);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll(STR_BUSBAR_SECTION, "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/busbar-sections", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeBusBarSections(UUID networkUuid, int variantNum, List<String> busBarSectionsId) {
        removeAll(STR_BUSBAR_SECTION, "/networks/{networkUuid}/{variantNum}/busbar-sections", networkUuid, variantNum, busBarSectionsId);
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll(STR_SWITCH, "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/switches", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll(STR_GENERATOR, "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/generators", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeGenerators(UUID networkUuid, int variantNum, List<String> generatorsId) {
        removeAll(STR_GENERATOR, "/networks/{networkUuid}/{variantNum}/generators", networkUuid, variantNum, generatorsId);
    }

    @Override
    public List<Resource<BatteryAttributes>> getVoltageLevelBatteries(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll(STR_BATTERY, "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/batteries", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeBatteries(UUID networkUuid, int variantNum, List<String> batteriesId) {
        removeAll(STR_BATTERY, "/networks/{networkUuid}/{variantNum}/batteries", networkUuid, variantNum, batteriesId);
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("load", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/loads", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll(STR_SHUNT_COMPENSATOR, "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/shunt-compensators", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeShuntCompensators(UUID networkUuid, int variantNum, List<String> shuntCompensatorsId) {
        removeAll(STR_SHUNT_COMPENSATOR, "/networks/{networkUuid}/{variantNum}/shunt-compensators", networkUuid, variantNum, shuntCompensatorsId);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll(STR_VSC_CONVERTER_STATION, "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/vsc-converter-stations", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeVscConverterStations(UUID networkUuid, int variantNum, List<String> vscConverterStationsId) {
        removeAll(STR_VSC_CONVERTER_STATION, "/networks/{networkUuid}/{variantNum}/vsc-converter-stations", networkUuid, variantNum, vscConverterStationsId);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll(STR_STATIC_VAR_COMPENSATOR, "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/static-var-compensators", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeStaticVarCompensators(UUID networkUuid, int variantNum, List<String> staticVarCompensatorsId) {
        removeAll(STR_STATIC_VAR_COMPENSATOR, "/networks/{networkUuid}/{variantNum}/static-var-compensators", networkUuid, variantNum, staticVarCompensatorsId);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStations(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll(STR_LCC_CONVERTER_STATION, "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/lcc-converter-stations", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeLccConverterStations(UUID networkUuid, int variantNum, List<String> lccConverterStationsId) {
        removeAll(STR_LCC_CONVERTER_STATION, "/networks/{networkUuid}/{variantNum}/lcc-converter-stations", networkUuid, variantNum, lccConverterStationsId);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll(STR_TWO_WINDINGS_TRANSFORMER, "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/2-windings-transformers", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeTwoWindingsTransformers(UUID networkUuid, int variantNum, List<String> twoWindingsTransformersId) {
        removeAll(STR_TWO_WINDINGS_TRANSFORMER, "/networks/{networkUuid}/{variantNum}/2-windings-transformers", networkUuid, variantNum, twoWindingsTransformersId);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll(STR_THREE_WINDINGS_TRANSFORMER, "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/3-windings-transformers", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeThreeWindingsTransformers(UUID networkUuid, int variantNum, List<String> threeWindingsTransformersId) {
        removeAll(STR_THREE_WINDINGS_TRANSFORMER, "/networks/{networkUuid}/{variantNum}/3-windings-transformers", networkUuid, variantNum, threeWindingsTransformersId);
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("line", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/lines", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void removeLines(UUID networkUuid, int variantNum, List<String> linesId) {
        removeAll("line", "/networks/{networkUuid}/{variantNum}/lines", networkUuid, variantNum, linesId);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll(STR_DANGLING_LINE, "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/dangling-lines", networkUuid, variantNum, voltageLevelId);
    }

    // switch

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        create(STR_SWITCH, "/networks/{networkUuid}/switches", switchResources, networkUuid);
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid, int variantNum) {
        return getAll(STR_SWITCH, "/networks/{networkUuid}/{variantNum}/switches", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, int variantNum, String switchId) {
        return get(STR_SWITCH, "/networks/{networkUuid}/{variantNum}/switches/{switchId}", networkUuid, variantNum, switchId);
    }

    @Override
    public void updateSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources, AttributeFilter attributeFilter) {
        updateAll("switches", "/networks/{networkUuid}/switches", switchResources, attributeFilter, networkUuid);
    }

    @Override
    public void removeSwitches(UUID networkUuid, int variantNum, List<String> switchesId) {
        removeAll(STR_SWITCH, "/networks/{networkUuid}/{variantNum}/switches", networkUuid, variantNum, switchesId);
    }

    // busbar section

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        create(STR_BUSBAR_SECTION, "/networks/{networkUuid}/busbar-sections", busbarSectionResources, networkUuid);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid, int variantNum) {
        return getAll(STR_BUSBAR_SECTION, "/networks/{networkUuid}/{variantNum}/busbar-sections", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, int variantNum, String busbarSectionId) {
        return get(STR_BUSBAR_SECTION, "/networks/{networkUuid}/{variantNum}/busbar-sections/{busbarSectionId}", networkUuid, variantNum, busbarSectionId);
    }

    @Override
    public void updateBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources, AttributeFilter attributeFilter) {
        updateAll(STR_BUSBAR_SECTION, "/networks/{networkUuid}/busbar-sections", busbarSectionResources, attributeFilter, networkUuid);
    }

    // load

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        create("load", "/networks/{networkUuid}/loads", loadResources, networkUuid);
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid, int variantNum) {
        return getAll("load", "/networks/{networkUuid}/{variantNum}/loads", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, int variantNum, String loadId) {
        return get("load", "/networks/{networkUuid}/{variantNum}/loads/{loadId}", networkUuid, variantNum, loadId);
    }

    @Override
    public void updateLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources, AttributeFilter attributeFilter) {
        updateAll("load", "/networks/{networkUuid}/loads", loadResources, attributeFilter, networkUuid);
    }

    @Override
    public void removeLoads(UUID networkUuid, int variantNum, List<String> loadsId) {
        removeAll("load", "/networks/{networkUuid}/{variantNum}/loads", networkUuid, variantNum, loadsId);
    }

    // generator

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        create(STR_GENERATOR, "/networks/{networkUuid}/generators", generatorResources, networkUuid);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid, int variantNum) {
        return getAll(STR_GENERATOR, "/networks/{networkUuid}/{variantNum}/generators", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, int variantNum, String generatorId) {
        return get(STR_GENERATOR, "/networks/{networkUuid}/{variantNum}/generators/{generatorId}", networkUuid, variantNum, generatorId);
    }

    @Override
    public void updateGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources, AttributeFilter attributeFilter) {
        updateAll(STR_GENERATOR, "/networks/{networkUuid}/generators", generatorResources, attributeFilter, networkUuid);
    }

    // battery

    @Override
    public void createBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources) {
        create(STR_BATTERY, "/networks/{networkUuid}/batteries", batteryResources, networkUuid);
    }

    @Override
    public List<Resource<BatteryAttributes>> getBatteries(UUID networkUuid, int variantNum) {
        return getAll(STR_BATTERY, "/networks/{networkUuid}/{variantNum}/batteries", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<BatteryAttributes>> getBattery(UUID networkUuid, int variantNum, String batteryId) {
        return get(STR_BATTERY, "/networks/{networkUuid}/{variantNum}/batteries/{batteryId}", networkUuid, variantNum, batteryId);
    }

    @Override
    public void updateBatteries(UUID networkUuid, List<Resource<BatteryAttributes>> batteryResources, AttributeFilter attributeFilter) {
        updateAll(STR_BATTERY, "/networks/{networkUuid}/batteries", batteryResources, attributeFilter, networkUuid);
    }

    // 2 windings transformer

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        create(STR_TWO_WINDINGS_TRANSFORMER, "/networks/{networkUuid}/2-windings-transformers", twoWindingsTransformerResources, networkUuid);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid, int variantNum) {
        return getAll(STR_TWO_WINDINGS_TRANSFORMER, "/networks/{networkUuid}/{variantNum}/2-windings-transformers", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, int variantNum, String twoWindingsTransformerId) {
        return get(STR_TWO_WINDINGS_TRANSFORMER, "/networks/{networkUuid}/{variantNum}/2-windings-transformers/{twoWindingsTransformerId}", networkUuid, variantNum, twoWindingsTransformerId);
    }

    @Override
    public void updateTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources, AttributeFilter attributeFilter) {
        updateAll(STR_TWO_WINDINGS_TRANSFORMER, "/networks/{networkUuid}/2-windings-transformers", twoWindingsTransformerResources, attributeFilter, networkUuid);
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        create(STR_THREE_WINDINGS_TRANSFORMER, "/networks/{networkUuid}/3-windings-transformers", threeWindingsTransformerResources, networkUuid);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid, int variantNum) {
        return getAll(STR_THREE_WINDINGS_TRANSFORMER, "/networks/{networkUuid}/{variantNum}/3-windings-transformers", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, int variantNum, String threeWindingsTransformerId) {
        return get(STR_THREE_WINDINGS_TRANSFORMER, "/networks/{networkUuid}/{variantNum}/3-windings-transformers/{threeWindingsTransformerId}", networkUuid, variantNum, threeWindingsTransformerId);
    }

    @Override
    public void updateThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources, AttributeFilter attributeFilter) {
        updateAll(STR_THREE_WINDINGS_TRANSFORMER, "/networks/{networkUuid}/3-windings-transformers", threeWindingsTransformerResources, attributeFilter, networkUuid);
    }

    // line

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        create("line", "/networks/{networkUuid}/lines", lineResources, networkUuid);
    }

    @Override
    public List<Resource<LineAttributes>> getLines(UUID networkUuid, int variantNum) {
        return getAll("line", "/networks/{networkUuid}/{variantNum}/lines", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, int variantNum, String lineId) {
        return get("line", "/networks/{networkUuid}/{variantNum}/lines/{lineId}", networkUuid, variantNum, lineId);
    }

    @Override
    public void updateLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources, AttributeFilter attributeFilter) {
        updateAll("line", "/networks/{networkUuid}/lines", lineResources, attributeFilter, networkUuid);
    }

    // shunt compensator

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        create(STR_SHUNT_COMPENSATOR, "/networks/{networkUuid}/shunt-compensators", shuntCompensatorResources, networkUuid);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid, int variantNum) {
        return getAll(STR_SHUNT_COMPENSATOR, "/networks/{networkUuid}/{variantNum}/shunt-compensators", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, int variantNum, String shuntCompensatorId) {
        return get(STR_SHUNT_COMPENSATOR, "/networks/{networkUuid}/{variantNum}/shunt-compensators/{shuntCompensatorId}", networkUuid, variantNum, shuntCompensatorId);
    }

    @Override
    public void updateShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources, AttributeFilter attributeFilter) {
        updateAll(STR_SHUNT_COMPENSATOR, "/networks/{networkUuid}/shunt-compensators", shuntCompensatorResources, attributeFilter, networkUuid);
    }

    // VSC converter station

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        create(STR_VSC_CONVERTER_STATION, "/networks/{networkUuid}/vsc-converter-stations", vscConverterStationResources, networkUuid);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid, int variantNum) {
        return getAll(STR_VSC_CONVERTER_STATION, "/networks/{networkUuid}/{variantNum}/vsc-converter-stations", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, int variantNum, String vscConverterStationId) {
        return get(STR_VSC_CONVERTER_STATION, "/networks/{networkUuid}/{variantNum}/vsc-converter-stations/{vscConverterStationId}", networkUuid, variantNum, vscConverterStationId);
    }

    @Override
    public void updateVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources, AttributeFilter attributeFilter) {
        updateAll(STR_VSC_CONVERTER_STATION, "/networks/{networkUuid}/vsc-converter-stations", vscConverterStationResources, attributeFilter, networkUuid);
    }

    // LCC converter station

    @Override
    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        create(STR_LCC_CONVERTER_STATION, "/networks/{networkUuid}/lcc-converter-stations", lccConverterStationResources, networkUuid);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid, int variantNum) {
        return getAll(STR_LCC_CONVERTER_STATION, "/networks/{networkUuid}/{variantNum}/lcc-converter-stations", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, int variantNum, String lccConverterStationId) {
        return get(STR_LCC_CONVERTER_STATION, "/networks/{networkUuid}/{variantNum}/lcc-converter-stations/{vscConverterStationId}", networkUuid, variantNum, lccConverterStationId);
    }

    @Override
    public void updateLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources, AttributeFilter attributeFilter) {
        updateAll(STR_LCC_CONVERTER_STATION, "/networks/{networkUuid}/lcc-converter-stations", lccConverterStationResources, attributeFilter, networkUuid);
    }

    // SVC

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> staticVarCompensatorResources) {
        create(STR_STATIC_VAR_COMPENSATOR, "/networks/{networkUuid}/static-var-compensators", staticVarCompensatorResources, networkUuid);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid, int variantNum) {
        return getAll(STR_STATIC_VAR_COMPENSATOR, "/networks/{networkUuid}/{variantNum}/static-var-compensators", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, int variantNum, String staticVarCompensatorId) {
        return get("static compensator", "/networks/{networkUuid}/{variantNum}/static-var-compensators/{staticVarCompensatorId}", networkUuid, variantNum, staticVarCompensatorId);
    }

    @Override
    public void updateStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> staticVarCompensatorResources, AttributeFilter attributeFilter) {
        updateAll(STR_STATIC_VAR_COMPENSATOR, "/networks/{networkUuid}/static-var-compensators", staticVarCompensatorResources, attributeFilter, networkUuid);
    }

    // HVDC line

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources) {
        create(STR_HVDC_LINE, "/networks/{networkUuid}/hvdc-lines", hvdcLineResources, networkUuid);
    }

    @Override
    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid, int variantNum) {
        return getAll(STR_HVDC_LINE, "/networks/{networkUuid}/{variantNum}/hvdc-lines", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, int variantNum, String hvdcLineId) {
        return get(STR_HVDC_LINE, "/networks/{networkUuid}/{variantNum}/hvdc-lines/{hvdcLineId}", networkUuid, variantNum, hvdcLineId);
    }

    @Override
    public void removeHvdcLines(UUID networkUuid, int variantNum, List<String> hvdcLinesId) {
        removeAll(STR_HVDC_LINE, "/networks/{networkUuid}/{variantNum}/hvdc-lines", networkUuid, variantNum, hvdcLinesId);
    }

    @Override
    public void updateHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLineResources, AttributeFilter attributeFilter) {
        updateAll(STR_HVDC_LINE, "/networks/{networkUuid}/hvdc-lines", hvdcLineResources, attributeFilter, networkUuid);
    }

    // Dangling line

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources) {
        create(STR_DANGLING_LINE, "/networks/{networkUuid}/dangling-lines", danglingLineResources, networkUuid);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid, int variantNum) {
        return getAll(STR_DANGLING_LINE, "/networks/{networkUuid}/{variantNum}/dangling-lines", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, int variantNum, String danglingLineId) {
        return get(STR_DANGLING_LINE, "/networks/{networkUuid}/{variantNum}/dangling-lines/{danglingLineId}", networkUuid, variantNum, danglingLineId);
    }

    @Override
    public void removeDanglingLines(UUID networkUuid, int variantNum, List<String> danglingLinesId) {
        removeAll(STR_DANGLING_LINE, "/networks/{networkUuid}/{variantNum}/dangling-lines", networkUuid, variantNum, danglingLinesId);
    }

    @Override
    public void updateDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLineResources, AttributeFilter attributeFilter) {
        updateAll(STR_DANGLING_LINE, "/networks/{networkUuid}/dangling-lines", danglingLineResources, attributeFilter, networkUuid);
    }

    //ConfiguredBus

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesResources) {
        create("bus", "/networks/{networkUuid}/configured-buses", busesResources, networkUuid);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid, int variantNum) {
        return getAll("bus", "/networks/{networkUuid}/{variantNum}/configured-buses", networkUuid, variantNum);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getVoltageLevelConfiguredBuses(UUID networkUuid, int variantNum, String voltageLevelId) {
        return getAll("bus", "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/configured-buses", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, int variantNum, String busId) {
        return get("bus", "/networks/{networkUuid}/{variantNum}/configured-buses/{busId}", networkUuid, variantNum, busId);
    }

    @Override
    public void updateConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesResources, AttributeFilter attributeFilter) {
        updateAll("bus", "/networks/{networkUuid}/configured-buses", busesResources, attributeFilter, networkUuid);
    }

    @Override
    public void removeConfiguredBuses(UUID networkUuid, int variantNum, List<String> busesId) {
        removeAll("bus", "/networks/{networkUuid}/{variantNum}/configured-buses", networkUuid, variantNum, busesId);
    }

    @Override
    public void createTieLines(UUID networkUuid, List<Resource<TieLineAttributes>> tieLineResources) {
        create(STR_TIE_LINE, "/networks/{networkUuid}/tie-lines", tieLineResources, networkUuid);
    }

    @Override
    public List<Resource<TieLineAttributes>> getTieLines(UUID networkUuid, int variantNum) {
        return getAll(STR_TIE_LINE, "/networks/{networkUuid}/{variantNum}/tie-lines", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<TieLineAttributes>> getTieLine(UUID networkUuid, int variantNum, String tieLineId) {
        return get(STR_TIE_LINE, "/networks/{networkUuid}/{variantNum}/tie-lines/{tieLineId}", networkUuid, variantNum, tieLineId);
    }

    @Override
    public void removeTieLines(UUID networkUuid, int variantNum, List<String> tieLinesId) {
        removeAll(STR_TIE_LINE, "/networks/{networkUuid}/{variantNum}/tie-lines", networkUuid, variantNum, tieLinesId);
    }

    @Override
    public void updateTieLines(UUID networkUuid, List<Resource<TieLineAttributes>> tieLineResources, AttributeFilter attributeFilter) {
        updateAll(STR_TIE_LINE, "/networks/{networkUuid}/tie-lines", tieLineResources, attributeFilter, networkUuid);
    }

    // Areas
    @Override
    public void createAreas(UUID networkUuid, List<Resource<AreaAttributes>> areaResources) {
        create(STR_AREA, "/networks/{networkUuid}/areas", areaResources, networkUuid);
    }

    @Override
    public List<Resource<AreaAttributes>> getAreas(UUID networkUuid, int variantNum) {
        return getAll(STR_AREA, "/networks/{networkUuid}/{variantNum}/areas", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<AreaAttributes>> getArea(UUID networkUuid, int variantNum, String areaId) {
        return get(STR_AREA, "/networks/{networkUuid}/{variantNum}/areas/{AreaId}", networkUuid, variantNum, areaId);
    }

    @Override
    public void removeAreas(UUID networkUuid, int variantNum, List<String> areasIds) {
        removeAll(STR_AREA, "/networks/{networkUuid}/{variantNum}/areas", networkUuid, variantNum, areasIds);
    }

    @Override
    public void updateAreas(UUID networkUuid, List<Resource<AreaAttributes>> areaResources, AttributeFilter attributeFilter) {
        updateAll(STR_AREA, "/networks/{networkUuid}/areas", areaResources, attributeFilter, networkUuid);
    }

    @Override
    public Optional<Resource<IdentifiableAttributes>> getIdentifiable(UUID networkUuid, int variantNum, String id) {
        return get("identifiable", "/networks/{networkUuid}/{variantNum}/identifiables/{id}", networkUuid, variantNum, id);
    }

    @Override
    public List<String> getIdentifiablesIds(UUID networkUuid, int variantNum) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<String> identifiablesIds = restClient.get("/networks/{networkUuid}/{variantNum}/identifiables-ids", new ParameterizedTypeReference<>() {
        }, networkUuid, variantNum);
        stopwatch.stop();
        LOGGER.info("Get identifiables IDs ({}) loaded in {} ms", identifiablesIds.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
        return identifiablesIds;
    }

    @Override
    public void flush(UUID networkUuid) {
        // nothing to do
    }

    @Override
    public List<Resource<GroundAttributes>> getVoltageLevelGrounds(UUID networkUuid, int variantNum,
            String voltageLevelId) {
        return getAll(STR_GROUND, "/networks/{networkUuid}/{variantNum}/voltage-levels/{voltageLevelId}/grounds", networkUuid, variantNum, voltageLevelId);
    }

    @Override
    public void createGrounds(UUID networkUuid, List<Resource<GroundAttributes>> groundResources) {
        create(STR_GROUND, "/networks/{networkUuid}/grounds", groundResources, networkUuid);
    }

    @Override
    public List<Resource<GroundAttributes>> getGrounds(UUID networkUuid, int variantNum) {
        return getAll(STR_GROUND, "/networks/{networkUuid}/{variantNum}/grounds", networkUuid, variantNum);
    }

    @Override
    public Optional<Resource<GroundAttributes>> getGround(UUID networkUuid, int variantNum, String groundId) {
        return get(STR_GROUND, "/networks/{networkUuid}/{variantNum}/grounds/{groundId}", networkUuid, variantNum, groundId);
    }

    @Override
    public void removeGrounds(UUID networkUuid, int variantNum, List<String> groundsId) {
        removeAll(STR_GROUND, "/networks/{networkUuid}/{variantNum}/grounds", networkUuid, variantNum, groundsId);
    }

    @Override
    public void updateGrounds(UUID networkUuid, List<Resource<GroundAttributes>> groundResources,
            AttributeFilter attributeFilter) {
        updateAll(STR_GROUND, "/networks/{networkUuid}/grounds", groundResources, attributeFilter, networkUuid);
    }

    @Override
    public Optional<ExtensionAttributes> getExtensionAttributes(UUID networkUuid, int variantNum, ResourceType resourceType, String identifiableId, String extensionName) {
        return getExtensionAttributes("/networks/{networkUuid}/{variantNum}/identifiables/{identifiableId}/extensions/{extensionName}", networkUuid, variantNum, identifiableId, extensionName);
    }

    @Override
    public Map<String, ExtensionAttributes> getAllExtensionsAttributesByResourceTypeAndExtensionName(UUID networkUuid, int variantNum, ResourceType resourceType, String extensionName) {
        return getExtensionAttributesMap("/networks/{networkUuid}/{variantNum}/identifiables/types/{type}/extensions/{extensionName}", networkUuid, variantNum, resourceType, extensionName);
    }

    @Override
    public Map<String, ExtensionAttributes> getAllExtensionsAttributesByIdentifiableId(UUID networkUuid, int variantNum, ResourceType resourceType, String identifiableId) {
        return getExtensionAttributesMap("/networks/{networkUuid}/{variantNum}/identifiables/{identifiableId}/extensions", networkUuid, variantNum, identifiableId);
    }

    @Override
    public Map<String, Map<String, ExtensionAttributes>> getAllExtensionsAttributesByResourceType(UUID networkUuid, int variantNum, ResourceType resourceType) {
        return getExtensionAttributesNestedMap("/networks/{networkUuid}/{variantNum}/identifiables/types/{resourceType}/extensions", networkUuid, variantNum, resourceType);
    }

    @Override
    public void removeExtensionAttributes(UUID networkUuid, int variantNum, ResourceType resourceType, String identifiableId, String extensionName) {
        restClient.delete("/networks/{networkUuid}/{variantNum}/identifiables/{identifiableId}/extensions/{extensionName}", networkUuid, variantNum, identifiableId, extensionName);
    }

    @Override
    public Optional<OperationalLimitsGroupAttributes> getOperationalLimitsGroupAttributes(UUID networkUuid, int variantNum, ResourceType resourceType, String branchId, String operationalLimitsGroupId, int side) {
        return getOperationalLimitsGroupAttributes("/networks/{networkUuid}/{variantNum}/branch/{branchId}/types/{resourceType}/operationalLimitsGroup/{operationalLimitsGroupId}/side/{side}",
            networkUuid, variantNum, branchId, resourceType, operationalLimitsGroupId, side);
    }

    @Override
    public Optional<OperationalLimitsGroupAttributes> getSelectedOperationalLimitsGroupAttributes(UUID networkUuid, int variantNum, ResourceType resourceType, String branchId, String operationalLimitsGroupId, int side) {
        return getOperationalLimitsGroupAttributes(networkUuid, variantNum, resourceType, branchId, operationalLimitsGroupId, side);
    }

    @Override
    public List<OperationalLimitsGroupAttributes> getOperationalLimitsGroupAttributesForBranchSide(UUID networkUuid, int variantNum, ResourceType resourceType, String branchId, int side) {
        return getOperationalLimitsGroupAttributesForBranch("/networks/{networkUuid}/{variantNum}/branch/{branchId}/types/{resourceType}/side/{side}/operationalLimitsGroup",
            networkUuid, variantNum, branchId, resourceType, side);
    }

    @Override
    public Map<String, Map<Integer, Map<String, OperationalLimitsGroupAttributes>>> getAllOperationalLimitsGroupAttributesByResourceType(UUID networkUuid, int variantNum, ResourceType resourceType) {
        return getOperationalLimitsGroupAttributesNestedMap("/networks/{networkUuid}/{variantNum}/branch/types/{resourceType}/operationalLimitsGroup",
            networkUuid, variantNum, resourceType);
    }

    @Override
    public Map<String, Map<Integer, Map<String, OperationalLimitsGroupAttributes>>> getAllSelectedOperationalLimitsGroupAttributesByResourceType(UUID networkUuid, int variantNum, ResourceType resourceType) {
        return getOperationalLimitsGroupAttributesNestedMap("/networks/{networkUuid}/{variantNum}/branch/types/{resourceType}/operationalLimitsGroup/selected",
            networkUuid, variantNum, resourceType);
    }

    @Override
    public void removeOperationalLimitsGroupAttributes(UUID networkUuid, int variantNum, ResourceType resourceType, Map<String, Map<Integer, Set<String>>> operationalLimitsGroupsToDelete) {
        for (List<Map.Entry<String, Map<Integer, Set<String>>>> partitionEntries : Iterables.partition(operationalLimitsGroupsToDelete.entrySet(), RESOURCES_CREATION_CHUNK_SIZE)) {
            Map<String, Map<Integer, Set<String>>> partitionMap = partitionEntries.stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            restClient.deleteAll("/networks/{networkUuid}/{variantNum}/branch/types/{resourceType}/operationalLimitsGroup",
                    partitionMap, networkUuid, variantNum, resourceType);
        }
    }
}
