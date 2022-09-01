package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuit;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuitAdder;

/**
 * @author Seddik Yengui <seddik.yengui at rte-france.com>
 */

@AutoService(ExtensionAdderProvider.class)
public class GeneratorShortCircuitAdderImplProvider implements ExtensionAdderProvider<Generator, GeneratorShortCircuit, GeneratorShortCircuitAdder> {

    public String getImplementationName() {
        return "NetworkStore";
    }

    @Override
    public String getExtensionName() {
        return GeneratorShortCircuit.NAME;
    }

    public Class<GeneratorShortCircuitAdder> getAdderClass() {
        return GeneratorShortCircuitAdder.class;
    }

    public GeneratorShortCircuitAdder newAdder(Generator extendable) {
        return new GeneratorShortCircuitAdderImpl(extendable);
    }
}
