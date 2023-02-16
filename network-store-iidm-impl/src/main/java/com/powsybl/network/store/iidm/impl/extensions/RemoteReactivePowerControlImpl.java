/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import com.powsybl.network.store.iidm.impl.GeneratorImpl;
import com.powsybl.network.store.model.RemoteReactivePowerControlAttributes;
import com.powsybl.network.store.model.TerminalRefAttributes;

/**
 * @author Jon Harper <jon.harper at rte-france.com.com>
 * TODO copied from powsybl-core, Implement this by storing in the attributes
 */
public class RemoteReactivePowerControlImpl extends AbstractExtension<Generator> implements RemoteReactivePowerControl {

    private GeneratorImpl generator;

    public RemoteReactivePowerControlImpl(GeneratorImpl generator) {
        this.generator = generator;
    }

    public RemoteReactivePowerControlImpl(GeneratorImpl generatorImpl, double targetQ, TerminalRefAttributes regulatingTerminal, boolean enabled) {
        this(generatorImpl);
        generatorImpl.getResource().getAttributes().setRemoteReactivePowerControl(RemoteReactivePowerControlAttributes
                .builder().targetQ(targetQ).regulatingTerminal(regulatingTerminal).enabled(enabled).build());
    }

    @Override
    public String getName() {
        return RemoteReactivePowerControl.NAME;
    }

    @Override
    public double getTargetQ() {
        return generator.getResource().getAttributes().getRemoteReactivePowerControl().getTargetQ();
    }

    @Override
    public RemoteReactivePowerControl setTargetQ(double targetQ) {
        generator.getResource().getAttributes().getRemoteReactivePowerControl().setTargetQ(targetQ);
        return this;
    }

    @Override
    public boolean isEnabled() {
        return generator.getResource().getAttributes().getRemoteReactivePowerControl().isEnabled();
    }

    @Override
    public RemoteReactivePowerControl setEnabled(boolean enabled) {
        generator.getResource().getAttributes().getRemoteReactivePowerControl().setEnabled(enabled);
        return this;
    }

    @Override
    public Terminal getRegulatingTerminal() {
        return generator.getRemoteReactivePowerControlRegulatingTerminal();
    }
}
