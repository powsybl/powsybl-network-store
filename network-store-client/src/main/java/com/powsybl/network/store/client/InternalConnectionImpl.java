package com.powsybl.network.store.client;

import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.network.store.model.InternalConnectionAttributes;

public class InternalConnectionImpl implements VoltageLevel.NodeBreakerView.InternalConnection {

    private final InternalConnectionAttributes attributes;

    public InternalConnectionImpl(InternalConnectionAttributes attributes) {
        this.attributes = attributes;
    }

    static InternalConnectionImpl create(InternalConnectionAttributes attributes) {
        return new InternalConnectionImpl(attributes);
    }

    @Override
    public int getNode1() {
        return attributes.getNode1();
    }

    @Override
    public int getNode2() {
        return attributes.getNode2();
    }

}
