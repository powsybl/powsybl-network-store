package com.powsybl.network.store.client;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusAdder;
import com.powsybl.network.store.model.ConfiguredBusAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.VoltageLevelAttributes;

public class ConfiguredBusAdderImpl implements BusAdder {

    private final Resource<VoltageLevelAttributes> voltageLevelResource;

    private final NetworkObjectIndex index;

    private String id;

    private String name;

    private boolean ensureIdUnicity;

    ConfiguredBusAdderImpl(Resource<VoltageLevelAttributes> voltageLevelResource, NetworkObjectIndex index) {
        this.voltageLevelResource = voltageLevelResource;
        this.index = index;
    }

    @Override
    public BusAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public BusAdder setEnsureIdUnicity(boolean ensureIdUnicity) {
        this.ensureIdUnicity = ensureIdUnicity;
        return this;
    }

    @Override
    public BusAdder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public Bus add() {
        Resource<ConfiguredBusAttributes> resource = Resource.configuredBusBuilder()
                .id(id)
                .attributes(ConfiguredBusAttributes.builder()
                        .id(id)
                        .name(name)
                        .ensureIdUnicity(ensureIdUnicity)
                        .voltageLevelId(voltageLevelResource.getId())
                        .build())
                .build();
        return index.createBus(resource);
    }
}
