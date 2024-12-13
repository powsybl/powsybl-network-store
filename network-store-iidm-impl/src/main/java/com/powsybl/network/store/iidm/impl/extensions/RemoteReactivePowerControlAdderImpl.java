/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControlAdder;
import com.powsybl.network.store.iidm.impl.GeneratorImpl;
import com.powsybl.network.store.iidm.impl.TerminalRefUtils;
import com.powsybl.network.store.model.RemoteReactivePowerControlAttributes;

/**
 * @author Jon Harper <jon.harper at rte-france.com.com>
 */
public class RemoteReactivePowerControlAdderImpl extends AbstractExtensionAdder<Generator, RemoteReactivePowerControl> implements RemoteReactivePowerControlAdder {

    private double targetQ = Double.NaN;

    private Terminal regulatingTerminal;

    private boolean enabled = true;

    protected RemoteReactivePowerControlAdderImpl(Generator extendable) {
        super(extendable);
    }

    @Override
    protected RemoteReactivePowerControl createExtension(Generator generator) {
        if (Double.isNaN(targetQ)) {
            throw new PowsyblException("Reactive power target must be set");
        }
        if (regulatingTerminal == null) {
            throw new PowsyblException("Regulating terminal must be set");
        }
        RemoteReactivePowerControlAttributes attributes = RemoteReactivePowerControlAttributes.builder()
                .targetQ(targetQ)
                .regulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal))
                .enabled(enabled)
                .build();
        ((GeneratorImpl) generator).updateResource(res -> res.getAttributes().setRemoteReactivePowerControl(attributes));
        return new RemoteReactivePowerControlImpl((GeneratorImpl) generator);
    }

    @Override
    public RemoteReactivePowerControlAdder withTargetQ(double targetQ) {
        this.targetQ = targetQ;
        return this;
    }

    @Override
    public RemoteReactivePowerControlAdder withRegulatingTerminal(Terminal regulatingTerminal) {
        this.regulatingTerminal = regulatingTerminal;
        return this;
    }

    @Override
    public RemoteReactivePowerControlAdder withEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
}
