package com.powsybl.network.store.iidm.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.extensions.GeneratorStartup;
import com.powsybl.network.store.iidm.impl.GeneratorImpl;
import com.powsybl.network.store.model.ExtensionLoader;
import com.powsybl.network.store.model.GeneratorStartupAttributes;

@AutoService(ExtensionLoader.class)
public class GeneratorStartupLoader implements ExtensionLoader<Generator, GeneratorStartup, GeneratorStartupAttributes> {
    @Override
    public Extension<Generator> load(Generator generator) {
        return new GeneratorStartupImpl((GeneratorImpl) generator);
    }

    @Override
    public String getName() {
        return GeneratorStartup.NAME;
    }

    @Override
    public Class<GeneratorStartup> getType() {
        return GeneratorStartup.class;
    }

    @Override
    public Class<GeneratorStartupAttributes> getAttributesType() {
        return GeneratorStartupAttributes.class;
    }
}
