package com.powsybl.network.store.client;

import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.network.store.model.InternalConnectionAttributes;
import com.powsybl.network.store.model.Resource;

public class InternalConnectionImpl implements VoltageLevel.NodeBreakerView.InternalConnection {

    private final Resource<InternalConnectionAttributes> resource;

    public InternalConnectionImpl(Resource<InternalConnectionAttributes> resource) {
        this.resource = resource;
    }

    static InternalConnectionImpl create(Resource<InternalConnectionAttributes> resource) {
        return new InternalConnectionImpl(resource);
    }

    @Override
    public int getNode1() {
        return resource.getAttributes().getNode1();
    }

    @Override
    public int getNode2() {
        return resource.getAttributes().getNode2();
    }

}
