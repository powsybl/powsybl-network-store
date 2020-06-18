package com.powsybl.iidm.network.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.entsoe.util.MergedXnode;
import com.powsybl.entsoe.util.MergedXnodeAdderImpl;
import com.powsybl.iidm.network.Line;

@AutoService(ExtensionAdderProvider.class)
public class MergedXnodeAdderImplNetworkStoreProvider
        implements
        ExtensionAdderProvider<Line, MergedXnode, MergedXnodeAdderImpl> {

    @Override
    public String getImplementationName() {
        return "NetworkStore";
    }

    @Override
    public Class<MergedXnodeAdderImpl> getAdderClass() {
        return MergedXnodeAdderImpl.class;
    }

    @Override
    public MergedXnodeAdderImpl newAdder(Line extendable) {
        return new MergedXnodeAdderImpl(extendable);
    }
}
