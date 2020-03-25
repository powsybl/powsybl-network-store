/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.google.common.collect.ImmutableList;
import com.powsybl.commons.PowsyblException;
import com.powsybl.network.store.model.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CacheNetworkStoreClient implements NetworkStoreClient {

    static class NetworkCache {

        private final Resource<NetworkAttributes> networkResource;

        private final Map<String, Resource<SubstationAttributes>> substationResources = new HashMap<>();

        private final Map<String, Resource<HvdcLineAttributes>> hvdcLineResources = new HashMap<>();

        static class NestedResources<T extends IdentifiableAttributes> {

            private final Function<Resource<T>, String> containerIdFct1;

            private final Function<Resource<T>, String> containerIdFct2;

            private final Function<Resource<T>, String> containerIdFct3;

            private final Map<String, Resource<T>> resourcesById = new HashMap<>();

            private final Map<String, Set<Resource<T>>> resourcesByContainerId = new HashMap<>();

            NestedResources(Function<Resource<T>, String> containerIdFct1) {
                this(containerIdFct1, null, null);
            }

            NestedResources(Function<Resource<T>, String> containerIdFct1, Function<Resource<T>, String> containerIdFct2) {
                this(containerIdFct1, containerIdFct2, null);
            }

            NestedResources(Function<Resource<T>, String> containerIdFct1, Function<Resource<T>, String> containerIdFct2, Function<Resource<T>, String> containerIdFct3) {
                this.containerIdFct1 = Objects.requireNonNull(containerIdFct1);
                this.containerIdFct2 = containerIdFct2;
                this.containerIdFct3 = containerIdFct3;
            }

            void addResources(List<Resource<T>> resources) {
                for (Resource<T> resource : resources) {
                    addResource(resource);
                }
            }

            void addResource(Resource<T> resource) {
                resourcesById.put(resource.getId(), resource);
                resourcesByContainerId.computeIfAbsent(containerIdFct1.apply(resource), k -> new HashSet<>())
                        .add(resource);
                if (containerIdFct2 != null) {
                    resourcesByContainerId.computeIfAbsent(containerIdFct2.apply(resource), k -> new HashSet<>())
                            .add(resource);
                }
                if (containerIdFct3 != null) {
                    resourcesByContainerId.computeIfAbsent(containerIdFct3.apply(resource), k -> new HashSet<>())
                            .add(resource);
                }
            }

            List<Resource<T>> getResources() {
                return new ArrayList<>(resourcesById.values());
            }

            Optional<Resource<T>> getResource(String resourceId) {
                return Optional.ofNullable(resourcesById.get(resourceId));
            }

            int getResourceCount() {
                return resourcesById.size();
            }

            List<Resource<T>> getContainerResources(String containerId) {
                return ImmutableList.<Resource<T>>builder().addAll(resourcesByContainerId.computeIfAbsent(containerId, k -> new HashSet<>())).build();
            }

            void removeResource(String resourceId) {
                resourcesById.remove(resourceId);
            }
        }

        private final NestedResources<VoltageLevelAttributes> voltageLevelResources = new NestedResources<>(resource -> resource.getAttributes().getSubstationId());

        private final NestedResources<GeneratorAttributes> generatorResources = new NestedResources<>(resource -> resource.getAttributes().getVoltageLevelId());

        private final NestedResources<LoadAttributes> loadResources = new NestedResources<>(resource -> resource.getAttributes().getVoltageLevelId());

        private final NestedResources<ShuntCompensatorAttributes> shuntCompensatorResources = new NestedResources<>(resource -> resource.getAttributes().getVoltageLevelId());

        private final NestedResources<StaticVarCompensatorAttributes> staticVarCompensatorResources = new NestedResources<>(resource -> resource.getAttributes().getVoltageLevelId());

        private final NestedResources<VscConverterStationAttributes> vscConverterStationResources = new NestedResources<>(resource -> resource.getAttributes().getVoltageLevelId());

        private final NestedResources<LccConverterStationAttributes> lccConverterStationResources = new NestedResources<>(resource -> resource.getAttributes().getVoltageLevelId());

        private final NestedResources<BusbarSectionAttributes> busbarSectionResources = new NestedResources<>(resource -> resource.getAttributes().getVoltageLevelId());

        private final NestedResources<SwitchAttributes> switchResources = new NestedResources<>(resource -> resource.getAttributes().getVoltageLevelId());

        private final NestedResources<TwoWindingsTransformerAttributes> twoWindingsTransformerResources = new NestedResources<>(resource -> resource.getAttributes().getVoltageLevelId1(),
            resource -> resource.getAttributes().getVoltageLevelId2());

        private final NestedResources<ThreeWindingsTransformerAttributes> threeWindingsTransformerResources = new NestedResources<>(resource -> resource.getAttributes().getLeg1().getVoltageLevelId(),
            resource -> resource.getAttributes().getLeg2().getVoltageLevelId(), resource -> resource.getAttributes().getLeg3().getVoltageLevelId());

        private final NestedResources<LineAttributes> lineResources = new NestedResources<>(resource -> resource.getAttributes().getVoltageLevelId1(),
            resource -> resource.getAttributes().getVoltageLevelId2());

        private final NestedResources<DanglingLineAttributes> danglingLineResources = new NestedResources<>(resource -> resource.getAttributes().getVoltageLevelId());

        private final NestedResources<ConfiguredBusAttributes> configuredBusResources = new NestedResources<>(resource -> resource.getAttributes().getVoltageLevelId());

        NetworkCache(Resource<NetworkAttributes> networkResource) {
            this.networkResource = Objects.requireNonNull(networkResource);
        }

        Resource<NetworkAttributes> getNetworkResource() {
            return networkResource;
        }

        void addSubstationResource(Resource<SubstationAttributes> substationResource) {
            substationResources.put(substationResource.getId(), substationResource);
        }

        List<Resource<SubstationAttributes>> getSubstationResources() {
            return new ArrayList<>(substationResources.values());
        }

        Optional<Resource<SubstationAttributes>> getSubstationResource(String substationId) {
            return Optional.ofNullable(substationResources.get(substationId));
        }

        int getSubstationResourceCount() {
            return substationResources.size();
        }

        NestedResources<VoltageLevelAttributes> getVoltageLevelResources() {
            return voltageLevelResources;
        }

        NestedResources<GeneratorAttributes> getGeneratorResources() {
            return generatorResources;
        }

        NestedResources<LoadAttributes> getLoadResources() {
            return loadResources;
        }

        NestedResources<ShuntCompensatorAttributes> getShuntCompensatorResources() {
            return shuntCompensatorResources;
        }

        NestedResources<StaticVarCompensatorAttributes> getStaticVarCompensatorResources() {
            return staticVarCompensatorResources;
        }

        NestedResources<DanglingLineAttributes> getDanglingLineResources() {
            return danglingLineResources;
        }

        NestedResources<VscConverterStationAttributes> getVscConverterStationResources() {
            return vscConverterStationResources;
        }

        NestedResources<LccConverterStationAttributes> getLccConverterStationResources() {
            return lccConverterStationResources;
        }

        NestedResources<BusbarSectionAttributes> getBusbarSectionResources() {
            return busbarSectionResources;
        }

        NestedResources<SwitchAttributes> getSwitchResources() {
            return switchResources;
        }

        NestedResources<TwoWindingsTransformerAttributes> getTwoWindingsTransformerResources() {
            return twoWindingsTransformerResources;
        }

        NestedResources<ThreeWindingsTransformerAttributes> getThreeWindingsTransformerResources() {
            return threeWindingsTransformerResources;
        }

        NestedResources<LineAttributes> getLineResources() {
            return lineResources;
        }

        void addHvdcLineResource(Resource<HvdcLineAttributes> hvdcLineResource) {
            hvdcLineResources.put(hvdcLineResource.getId(), hvdcLineResource);
        }

        List<Resource<HvdcLineAttributes>> getHvdcLineResources() {
            return new ArrayList<>(hvdcLineResources.values());
        }

        Optional<Resource<HvdcLineAttributes>> getHvdcLineResource(String hvdcLineId) {
            return Optional.ofNullable(hvdcLineResources.get(hvdcLineId));
        }

        NestedResources<ConfiguredBusAttributes> getBusResources() {
            return configuredBusResources;
        }

        Optional<Resource<ConfiguredBusAttributes>> getBusResources(String busId) {
            return configuredBusResources.getResource(busId);
        }

        int getHvdcLineResourceCount() {
            return hvdcLineResources.size();
        }
    }

    private final Map<UUID, NetworkCache> networkCaches = new HashMap<>();

    private NetworkCache getNetworkCache(UUID networkUuid) {
        NetworkCache networkCache = networkCaches.get(networkUuid);
        if (networkCache == null) {
            throw new PowsyblException("Network '" + networkUuid + "' not found");
        }
        return networkCache;
    }

    @Override
    public List<Resource<NetworkAttributes>> getNetworks() {
        return networkCaches.values().stream().map(NetworkCache::getNetworkResource).collect(Collectors.toList());
    }

    @Override
    public void createNetworks(List<Resource<NetworkAttributes>> networkResources) {
        for (Resource<NetworkAttributes> networkResource : networkResources) {
            networkCaches.put(networkResource.getAttributes().getUuid(), new NetworkCache(networkResource));
        }
    }

    @Override
    public Optional<Resource<NetworkAttributes>> getNetwork(UUID networkUuid) {
        return Optional.of(getNetworkCache(networkUuid).getNetworkResource());
    }

    @Override
    public void deleteNetwork(UUID networkUuid) {
        networkCaches.remove(networkUuid);
    }

    @Override
    public void createSubstations(UUID networkUuid, List<Resource<SubstationAttributes>> substationResources) {
        NetworkCache networkCache = getNetworkCache(networkUuid);
        for (Resource<SubstationAttributes> substationResource : substationResources) {
            networkCache.addSubstationResource(substationResource);
        }
    }

    @Override
    public List<Resource<SubstationAttributes>> getSubstations(UUID networkUuid) {
        return getNetworkCache(networkUuid).getSubstationResources();
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(UUID networkUuid, String substationId) {
        return getNetworkCache(networkUuid).getSubstationResource(substationId);
    }

    @Override
    public int getSubstationCount(UUID networkUuid) {
        return getNetworkCache(networkUuid).getSubstationResourceCount();
    }

    @Override
    public void createVoltageLevels(UUID networkUuid, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        getNetworkCache(networkUuid).getVoltageLevelResources().addResources(voltageLevelResources);
    }

    @Override
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(UUID networkUuid, String voltageLevelId) {
        return getNetworkCache(networkUuid).getVoltageLevelResources().getResource(voltageLevelId);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(UUID networkUuid) {
        return getNetworkCache(networkUuid).getVoltageLevelResources().getResources();
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(UUID networkUuid, String substationId) {
        return getNetworkCache(networkUuid).getVoltageLevelResources().getContainerResources(substationId);
    }

    @Override
    public int getVoltageLevelCount(UUID networkUuid) {
        return getNetworkCache(networkUuid).getVoltageLevelResources().getResourceCount();
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(UUID networkUuid, String voltageLevelId) {
        return getNetworkCache(networkUuid).getBusbarSectionResources().getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(UUID networkUuid, String voltageLevelId) {
        return getNetworkCache(networkUuid).getSwitchResources().getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(UUID networkUuid, String voltageLevelId) {
        return getNetworkCache(networkUuid).getGeneratorResources().getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(UUID networkUuid, String voltageLevelId) {
        return getNetworkCache(networkUuid).getLoadResources().getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(UUID networkUuid, String voltageLevelId) {
        return getNetworkCache(networkUuid).getShuntCompensatorResources().getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getVoltageLevelStaticVarCompensators(UUID networkUuid, String voltageLevelId) {
        return getNetworkCache(networkUuid).getStaticVarCompensatorResources().getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStation(UUID networkUuid, String voltageLevelId) {
        return getNetworkCache(networkUuid).getVscConverterStationResources().getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getVoltageLevelLccConverterStation(UUID networkUuid, String voltageLevelId) {
        return getNetworkCache(networkUuid).getLccConverterStationResources().getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return getNetworkCache(networkUuid).getTwoWindingsTransformerResources().getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getVoltageLevelThreeWindingsTransformers(UUID networkUuid, String voltageLevelId) {
        return getNetworkCache(networkUuid).getThreeWindingsTransformerResources().getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(UUID networkUuid, String voltageLevelId) {
        return getNetworkCache(networkUuid).getLineResources().getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getVoltageLevelDanglingLines(UUID networkUuid, String voltageLevelId) {
        return getNetworkCache(networkUuid).getDanglingLineResources().getContainerResources(voltageLevelId);
    }

    @Override
    public void createSwitches(UUID networkUuid, List<Resource<SwitchAttributes>> switchResources) {
        getNetworkCache(networkUuid).getSwitchResources().addResources(switchResources);
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(UUID networkUuid) {
        return getNetworkCache(networkUuid).getSwitchResources().getResources();
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(UUID networkUuid, String switchId) {
        return getNetworkCache(networkUuid).getSwitchResources().getResource(switchId);
    }

    @Override
    public int getSwitchCount(UUID networkUuid) {
        return getNetworkCache(networkUuid).getSwitchResources().getResourceCount();
    }

    @Override
    public void createBusbarSections(UUID networkUuid, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        getNetworkCache(networkUuid).getBusbarSectionResources().addResources(busbarSectionResources);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(UUID networkUuid) {
        return getNetworkCache(networkUuid).getBusbarSectionResources().getResources();
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(UUID networkUuid, String busbarSectionId) {
        return getNetworkCache(networkUuid).getBusbarSectionResources().getResource(busbarSectionId);
    }

    @Override
    public int getBusbarSectionCount(UUID networkUuid) {
        return getNetworkCache(networkUuid).getBusbarSectionResources().getResourceCount();
    }

    @Override
    public void createLoads(UUID networkUuid, List<Resource<LoadAttributes>> loadResources) {
        getNetworkCache(networkUuid).getLoadResources().addResources(loadResources);
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(UUID networkUuid) {
        return getNetworkCache(networkUuid).getLoadResources().getResources();
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(UUID networkUuid, String loadId) {
        return getNetworkCache(networkUuid).getLoadResources().getResource(loadId);
    }

    @Override
    public int getLoadCount(UUID networkUuid) {
        return getNetworkCache(networkUuid).getLoadResources().getResourceCount();
    }

    @Override
    public void createGenerators(UUID networkUuid, List<Resource<GeneratorAttributes>> generatorResources) {
        getNetworkCache(networkUuid).getGeneratorResources().addResources(generatorResources);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(UUID networkUuid) {
        return getNetworkCache(networkUuid).getGeneratorResources().getResources();
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(UUID networkUuid, String generatorId) {
        return getNetworkCache(networkUuid).getGeneratorResources().getResource(generatorId);
    }

    @Override
    public int getGeneratorCount(UUID networkUuid) {
        return getNetworkCache(networkUuid).getGeneratorResources().getResourceCount();
    }

    @Override
    public void createTwoWindingsTransformers(UUID networkUuid, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        getNetworkCache(networkUuid).getTwoWindingsTransformerResources().addResources(twoWindingsTransformerResources);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(UUID networkUuid) {
        return getNetworkCache(networkUuid).getTwoWindingsTransformerResources().getResources();
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(UUID networkUuid, String twoWindingsTransformerId) {
        return getNetworkCache(networkUuid).getTwoWindingsTransformerResources().getResource(twoWindingsTransformerId);
    }

    @Override
    public int getTwoWindingsTransformerCount(UUID networkUuid) {
        return getNetworkCache(networkUuid).getTwoWindingsTransformerResources().getResourceCount();
    }

    // 3 windings transformer

    @Override
    public void createThreeWindingsTransformers(UUID networkUuid, List<Resource<ThreeWindingsTransformerAttributes>> threeWindingsTransformerResources) {
        getNetworkCache(networkUuid).getThreeWindingsTransformerResources().addResources(threeWindingsTransformerResources);
    }

    @Override
    public List<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformers(UUID networkUuid) {
        return getNetworkCache(networkUuid).getThreeWindingsTransformerResources().getResources();
    }

    @Override
    public Optional<Resource<ThreeWindingsTransformerAttributes>> getThreeWindingsTransformer(UUID networkUuid, String threeWindingsTransformerId) {
        return getNetworkCache(networkUuid).getThreeWindingsTransformerResources().getResource(threeWindingsTransformerId);
    }

    @Override
    public int getThreeWindingsTransformerCount(UUID networkUuid) {
        return getNetworkCache(networkUuid).getThreeWindingsTransformerResources().getResourceCount();
    }

    @Override
    public void createLines(UUID networkUuid, List<Resource<LineAttributes>> lineResources) {
        getNetworkCache(networkUuid).getLineResources().addResources(lineResources);
    }

    @Override
    public List<Resource<LineAttributes>> getLines(UUID networkUuid) {
        return getNetworkCache(networkUuid).getLineResources().getResources();
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(UUID networkUuid, String lineId) {
        return getNetworkCache(networkUuid).getLineResources().getResource(lineId);
    }

    @Override
    public int getLineCount(UUID networkUuid) {
        return getNetworkCache(networkUuid).getLineResources().getResourceCount();
    }

    @Override
    public void createShuntCompensators(UUID networkUuid, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        getNetworkCache(networkUuid).getShuntCompensatorResources().addResources(shuntCompensatorResources);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(UUID networkUuid) {
        return getNetworkCache(networkUuid).getShuntCompensatorResources().getResources();
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(UUID networkUuid, String shuntCompensatorId) {
        return getNetworkCache(networkUuid).getShuntCompensatorResources().getResource(shuntCompensatorId);
    }

    @Override
    public int getShuntCompensatorCount(UUID networkUuid) {
        return getNetworkCache(networkUuid).getShuntCompensatorResources().getResourceCount();
    }

    @Override
    public void createVscConverterStations(UUID networkUuid, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        getNetworkCache(networkUuid).getVscConverterStationResources().addResources(vscConverterStationResources);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(UUID networkUuid) {
        return getNetworkCache(networkUuid).getVscConverterStationResources().getResources();
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(UUID networkUuid, String vscConverterStationId) {
        return getNetworkCache(networkUuid).getVscConverterStationResources().getResource(vscConverterStationId);
    }

    @Override
    public int getVscConverterStationCount(UUID networkUuid) {
        return getNetworkCache(networkUuid).getVscConverterStationResources().getResourceCount();
    }

    @Override
    public void createStaticVarCompensators(UUID networkUuid, List<Resource<StaticVarCompensatorAttributes>> staticVarCompensatorResources) {
        getNetworkCache(networkUuid).getStaticVarCompensatorResources().addResources(staticVarCompensatorResources);
    }

    @Override
    public List<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensators(UUID networkUuid) {
        return getNetworkCache(networkUuid).getStaticVarCompensatorResources().getResources();
    }

    @Override
    public Optional<Resource<StaticVarCompensatorAttributes>> getStaticVarCompensator(UUID networkUuid, String staticVarCompensatorId) {
        return getNetworkCache(networkUuid).getStaticVarCompensatorResources().getResource(staticVarCompensatorId);
    }

    @Override
    public int getStaticVarCompensatorCount(UUID networkUuid) {
        return getNetworkCache(networkUuid).getStaticVarCompensatorResources().getResourceCount();
    }

    public void createLccConverterStations(UUID networkUuid, List<Resource<LccConverterStationAttributes>> lccConverterStationResources) {
        getNetworkCache(networkUuid).getLccConverterStationResources().addResources(lccConverterStationResources);
    }

    @Override
    public List<Resource<LccConverterStationAttributes>> getLccConverterStations(UUID networkUuid) {
        return getNetworkCache(networkUuid).getLccConverterStationResources().getResources();
    }

    @Override
    public Optional<Resource<LccConverterStationAttributes>> getLccConverterStation(UUID networkUuid, String lccConverterStationId) {
        return getNetworkCache(networkUuid).getLccConverterStationResources().getResource(lccConverterStationId);
    }

    @Override
    public int getLccConverterStationCount(UUID networkUuid) {
        return getNetworkCache(networkUuid).getLccConverterStationResources().getResourceCount();
    }

    @Override
    public void createHvdcLines(UUID networkUuid, List<Resource<HvdcLineAttributes>> hvdcLinesResources) {
        NetworkCache networkCache = getNetworkCache(networkUuid);
        for (Resource<HvdcLineAttributes> hvdcLinesResource : hvdcLinesResources) {
            networkCache.addHvdcLineResource(hvdcLinesResource);
        }
    }

    @Override
    public List<Resource<HvdcLineAttributes>> getHvdcLines(UUID networkUuid) {
        return getNetworkCache(networkUuid).getHvdcLineResources();
    }

    @Override
    public Optional<Resource<HvdcLineAttributes>> getHvdcLine(UUID networkUuid, String hvdcLineId) {
        return getNetworkCache(networkUuid).getHvdcLineResource(hvdcLineId);
    }

    @Override
    public int getHvdcLineCount(UUID networkUuid) {
        return getNetworkCache(networkUuid).getHvdcLineResourceCount();
    }

    @Override
    public void createDanglingLines(UUID networkUuid, List<Resource<DanglingLineAttributes>> danglingLinesResources) {
        getNetworkCache(networkUuid).getDanglingLineResources().addResources(danglingLinesResources);
    }

    @Override
    public List<Resource<DanglingLineAttributes>> getDanglingLines(UUID networkUuid) {
        return getNetworkCache(networkUuid).getDanglingLineResources().getResources();
    }

    @Override
    public Optional<Resource<DanglingLineAttributes>> getDanglingLine(UUID networkUuid, String danglingLineId) {
        return getNetworkCache(networkUuid).getDanglingLineResources().getResource(danglingLineId);
    }

    @Override
    public int getDanglingLineCount(UUID networkUuid) {
        return getNetworkCache(networkUuid).getDanglingLineResources().getResourceCount();
    }

    @Override
    public void removeDanglingLine(UUID networkUuid, String danglingLineId) {
        getNetworkCache(networkUuid).getDanglingLineResources().removeResource(danglingLineId);
    }

    @Override
    public void createConfiguredBuses(UUID networkUuid, List<Resource<ConfiguredBusAttributes>> busesResources) {
        getNetworkCache(networkUuid).getBusResources().addResources(busesResources);
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid) {
        return getNetworkCache(networkUuid).getBusResources().getResources();
    }

    @Override
    public List<Resource<ConfiguredBusAttributes>> getConfiguredBuses(UUID networkUuid, String voltageLevelId) {
        return getNetworkCache(networkUuid).getBusResources().getResources();
    }

    @Override
    public Optional<Resource<ConfiguredBusAttributes>> getConfiguredBus(UUID networkUuid, String busId) {
        return getNetworkCache(networkUuid).getBusResources(busId);
    }

    @Override
    public void flush() {
        // nothing to do
    }
}
