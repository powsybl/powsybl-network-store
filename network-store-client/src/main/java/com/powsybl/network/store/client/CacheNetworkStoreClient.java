/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

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

        static class NestedResources<T extends IdentifiableAttributes> {

            private final Function<Resource<T>, String> containerIdFct1;

            private final Function<Resource<T>, String> containerIdFct2;

            private final Map<String, Resource<T>> resourcesById = new HashMap<>();

            private final Map<String, List<Resource<T>>> resourcesByContainerId = new HashMap<>();

            NestedResources(Function<Resource<T>, String> containerIdFct1) {
                this(containerIdFct1, null);
            }

            NestedResources(Function<Resource<T>, String> containerIdFct1, Function<Resource<T>, String> containerIdFct2) {
                this.containerIdFct1 = Objects.requireNonNull(containerIdFct1);
                this.containerIdFct2 = containerIdFct2;
            }

            void addResources(List<Resource<T>> resources) {
                for (Resource<T> resource : resources) {
                    addResource(resource);
                }
            }

            void addResource(Resource<T> resource) {
                resourcesById.put(resource.getId(), resource);
                resourcesByContainerId.computeIfAbsent(containerIdFct1.apply(resource), k -> new ArrayList<>())
                        .add(resource);
                if (containerIdFct2 != null) {
                    resourcesByContainerId.computeIfAbsent(containerIdFct2.apply(resource), k -> new ArrayList<>())
                            .add(resource);
                }
            }

            List<Resource<T>> getResources() {
                return new ArrayList<>(resourcesById.values());
            }

            Optional<Resource<T>> getResource(String resourceId) {
                return Optional.of(resourcesById.get(resourceId));
            }

            int getResourceCount() {
                return resourcesById.size();
            }

            List<Resource<T>> getContainerResources(String containerId) {
                return resourcesByContainerId.computeIfAbsent(containerId, k -> new ArrayList<>());
            }
        }

        private final NestedResources<VoltageLevelAttributes> voltageLevelResources = new NestedResources<>(resource -> resource.getAttributes().getSubstationId());

        private final NestedResources<GeneratorAttributes> generatorResources = new NestedResources<>(resource -> resource.getAttributes().getVoltageLevelId());

        private final NestedResources<LoadAttributes> loadResources = new NestedResources<>(resource -> resource.getAttributes().getVoltageLevelId());

        private final NestedResources<ShuntCompensatorAttributes> shuntCompensatorResources = new NestedResources<>(resource -> resource.getAttributes().getVoltageLevelId());

        private final NestedResources<VscConverterStationAttributes> vscConverterStationResources = new NestedResources<>(resource -> resource.getAttributes().getVoltageLevelId());

        private final NestedResources<BusbarSectionAttributes> busbarSectionResources = new NestedResources<>(resource -> resource.getAttributes().getVoltageLevelId());

        private final NestedResources<SwitchAttributes> switchResources = new NestedResources<>(resource -> resource.getAttributes().getVoltageLevelId());

        private final NestedResources<TwoWindingsTransformerAttributes> twoWindingsTransformerResources = new NestedResources<>(resource -> resource.getAttributes().getVoltageLevelId1(),
            resource -> resource.getAttributes().getVoltageLevelId2());

        private final NestedResources<LineAttributes> lineResources = new NestedResources<>(resource -> resource.getAttributes().getVoltageLevelId1(),
            resource -> resource.getAttributes().getVoltageLevelId2());

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
            return Optional.of(substationResources.get(substationId));
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

        NestedResources<VscConverterStationAttributes> getVscConverterStationResources() {
            return vscConverterStationResources;
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

        NestedResources<LineAttributes> getLineResources() {
            return lineResources;
        }
    }

    private final Map<String, NetworkCache> networkCaches = new HashMap<>();

    private NetworkCache getNetworkCache(String id) {
        NetworkCache networkCache = networkCaches.get(id);
        if (networkCache == null) {
            throw new PowsyblException("Network '" + id + "' not found");
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
            networkCaches.put(networkResource.getId(), new NetworkCache(networkResource));
        }
    }

    @Override
    public Optional<Resource<NetworkAttributes>> getNetwork(String networkId) {
        return Optional.of(getNetworkCache(networkId).getNetworkResource());
    }

    @Override
    public void deleteNetwork(String id) {
        networkCaches.remove(id);
    }

    @Override
    public void createSubstations(String networkId, List<Resource<SubstationAttributes>> substationResources) {
        NetworkCache networkCache = getNetworkCache(networkId);
        for (Resource<SubstationAttributes> substationResource : substationResources) {
            networkCache.addSubstationResource(substationResource);
        }
    }

    @Override
    public List<Resource<SubstationAttributes>> getSubstations(String networkId) {
        return getNetworkCache(networkId).getSubstationResources();
    }

    @Override
    public Optional<Resource<SubstationAttributes>> getSubstation(String networkId, String substationId) {
        return getNetworkCache(networkId).getSubstationResource(substationId);
    }

    @Override
    public int getSubstationCount(String networkId) {
        return getNetworkCache(networkId).getSubstationResourceCount();
    }

    @Override
    public void createVoltageLevels(String networkId, List<Resource<VoltageLevelAttributes>> voltageLevelResources) {
        getNetworkCache(networkId).getVoltageLevelResources().addResources(voltageLevelResources);
    }

    @Override
    public Optional<Resource<VoltageLevelAttributes>> getVoltageLevel(String networkId, String voltageLevelId) {
        return getNetworkCache(networkId).getVoltageLevelResources().getResource(voltageLevelId);
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevels(String networkId) {
        return getNetworkCache(networkId).getVoltageLevelResources().getResources();
    }

    @Override
    public List<Resource<VoltageLevelAttributes>> getVoltageLevelsInSubstation(String networkId, String substationId) {
        return getNetworkCache(networkId).getVoltageLevelResources().getContainerResources(substationId);
    }

    @Override
    public int getVoltageLevelCount(String networkId) {
        return getNetworkCache(networkId).getVoltageLevelResources().getResourceCount();
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getVoltageLevelBusbarSections(String networkId, String voltageLevelId) {
        return getNetworkCache(networkId).getBusbarSectionResources().getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<SwitchAttributes>> getVoltageLevelSwitches(String networkId, String voltageLevelId) {
        return getNetworkCache(networkId).getSwitchResources().getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getVoltageLevelGenerators(String networkId, String voltageLevelId) {
        return getNetworkCache(networkId).getGeneratorResources().getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<LoadAttributes>> getVoltageLevelLoads(String networkId, String voltageLevelId) {
        return getNetworkCache(networkId).getLoadResources().getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getVoltageLevelShuntCompensators(String networkId, String voltageLevelId) {
        return getNetworkCache(networkId).getShuntCompensatorResources().getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVoltageLevelVscConverterStation(String networkId, String voltageLevelId) {
        return getNetworkCache(networkId).getVscConverterStationResources().getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getVoltageLevelTwoWindingsTransformers(String networkId, String voltageLevelId) {
        return getNetworkCache(networkId).getTwoWindingsTransformerResources().getContainerResources(voltageLevelId);
    }

    @Override
    public List<Resource<LineAttributes>> getVoltageLevelLines(String networkId, String voltageLevelId) {
        return getNetworkCache(networkId).getLineResources().getContainerResources(voltageLevelId);
    }

    @Override
    public void createSwitches(String networkId, List<Resource<SwitchAttributes>> switchResources) {
        getNetworkCache(networkId).getSwitchResources().addResources(switchResources);
    }

    @Override
    public List<Resource<SwitchAttributes>> getSwitches(String networkId) {
        return getNetworkCache(networkId).getSwitchResources().getResources();
    }

    @Override
    public Optional<Resource<SwitchAttributes>> getSwitch(String networkId, String switchId) {
        return getNetworkCache(networkId).getSwitchResources().getResource(switchId);
    }

    @Override
    public int getSwitchCount(String networkId) {
        return getNetworkCache(networkId).getSwitchResources().getResourceCount();
    }

    @Override
    public void createBusbarSections(String networkId, List<Resource<BusbarSectionAttributes>> busbarSectionResources) {
        getNetworkCache(networkId).getBusbarSectionResources().addResources(busbarSectionResources);
    }

    @Override
    public List<Resource<BusbarSectionAttributes>> getBusbarSections(String networkId) {
        return getNetworkCache(networkId).getBusbarSectionResources().getResources();
    }

    @Override
    public Optional<Resource<BusbarSectionAttributes>> getBusbarSection(String networkId, String busbarSectionId) {
        return getNetworkCache(networkId).getBusbarSectionResources().getResource(busbarSectionId);
    }

    @Override
    public int getBusbarSectionCount(String networkId) {
        return getNetworkCache(networkId).getBusbarSectionResources().getResourceCount();
    }

    @Override
    public void createLoads(String networkId, List<Resource<LoadAttributes>> loadResources) {
        getNetworkCache(networkId).getLoadResources().addResources(loadResources);
    }

    @Override
    public List<Resource<LoadAttributes>> getLoads(String networkId) {
        return getNetworkCache(networkId).getLoadResources().getResources();
    }

    @Override
    public Optional<Resource<LoadAttributes>> getLoad(String networkId, String loadId) {
        return getNetworkCache(networkId).getLoadResources().getResource(loadId);
    }

    @Override
    public int getLoadCount(String networkId) {
        return getNetworkCache(networkId).getLoadResources().getResourceCount();
    }

    @Override
    public void createGenerators(String networkId, List<Resource<GeneratorAttributes>> generatorResources) {
        getNetworkCache(networkId).getGeneratorResources().addResources(generatorResources);
    }

    @Override
    public List<Resource<GeneratorAttributes>> getGenerators(String networkId) {
        return getNetworkCache(networkId).getGeneratorResources().getResources();
    }

    @Override
    public Optional<Resource<GeneratorAttributes>> getGenerator(String networkId, String generatorId) {
        return getNetworkCache(networkId).getGeneratorResources().getResource(generatorId);
    }

    @Override
    public int getGeneratorCount(String networkId) {
        return getNetworkCache(networkId).getGeneratorResources().getResourceCount();
    }

    @Override
    public void createTwoWindingsTransformers(String networkId, List<Resource<TwoWindingsTransformerAttributes>> twoWindingsTransformerResources) {
        getNetworkCache(networkId).getTwoWindingsTransformerResources().addResources(twoWindingsTransformerResources);
    }

    @Override
    public List<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformers(String networkId) {
        return getNetworkCache(networkId).getTwoWindingsTransformerResources().getResources();
    }

    @Override
    public Optional<Resource<TwoWindingsTransformerAttributes>> getTwoWindingsTransformer(String networkId, String twoWindingsTransformerId) {
        return getNetworkCache(networkId).getTwoWindingsTransformerResources().getResource(twoWindingsTransformerId);
    }

    @Override
    public int getTwoWindingsTransformerCount(String networkId) {
        return getNetworkCache(networkId).getTwoWindingsTransformerResources().getResourceCount();
    }

    @Override
    public void createLines(String networkId, List<Resource<LineAttributes>> lineResources) {
        getNetworkCache(networkId).getLineResources().addResources(lineResources);
    }

    @Override
    public List<Resource<LineAttributes>> getLines(String networkId) {
        return getNetworkCache(networkId).getLineResources().getResources();
    }

    @Override
    public Optional<Resource<LineAttributes>> getLine(String networkId, String lineId) {
        return getNetworkCache(networkId).getLineResources().getResource(lineId);
    }

    @Override
    public int getLineCount(String networkId) {
        return getNetworkCache(networkId).getLineResources().getResourceCount();
    }

    @Override
    public void createShuntCompensators(String networkId, List<Resource<ShuntCompensatorAttributes>> shuntCompensatorResources) {
        getNetworkCache(networkId).getShuntCompensatorResources().addResources(shuntCompensatorResources);
    }

    @Override
    public List<Resource<ShuntCompensatorAttributes>> getShuntCompensators(String networkId) {
        return getNetworkCache(networkId).getShuntCompensatorResources().getResources();
    }

    @Override
    public Optional<Resource<ShuntCompensatorAttributes>> getShuntCompensator(String networkId, String shuntCompensatorId) {
        return getNetworkCache(networkId).getShuntCompensatorResources().getResource(shuntCompensatorId);
    }

    @Override
    public int getShuntCompensatorCount(String networkId) {
        return getNetworkCache(networkId).getShuntCompensatorResources().getResourceCount();
    }

    @Override
    public void createVscConverterStations(String networkId, List<Resource<VscConverterStationAttributes>> vscConverterStationResources) {
        getNetworkCache(networkId).getVscConverterStationResources().addResources(vscConverterStationResources);
    }

    @Override
    public List<Resource<VscConverterStationAttributes>> getVscConverterStations(String networkId) {
        return getNetworkCache(networkId).getVscConverterStationResources().getResources();
    }

    @Override
    public Optional<Resource<VscConverterStationAttributes>> getVscConverterStation(String networkId, String vscConverterStationId) {
        return getNetworkCache(networkId).getVscConverterStationResources().getResource(vscConverterStationId);
    }

    @Override
    public int getVscConverterStationCount(String networkId) {
        return getNetworkCache(networkId).getVscConverterStationResources().getResourceCount();
    }

    @Override
    public void createStaticVarCompensators(String id, List<Resource<StaticVarCompensatorAttributes>> svcResources) {
        // TODO
    }

    @Override
    public void createHvdcLines(String id, List<Resource<HvdcLineAttributes>> hvdcLinesResources) {
        // TODO
    }

    @Override
    public void flush() {
        // nothing to do
    }
}
