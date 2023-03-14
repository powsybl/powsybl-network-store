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
import com.powsybl.network.store.iidm.impl.TerminalRefUtils;
import com.powsybl.network.store.model.RemoteReactivePowerControlAttributes;
import com.powsybl.network.store.model.TerminalRefAttributes;

/**
 * @author Jon Harper <jon.harper at rte-france.com.com>
 * TODO copied from powsybl-core, Implement this by storing in the attributes
 */
public class RemoteReactivePowerControlImpl extends AbstractExtension<Generator> implements RemoteReactivePowerControl {

    public RemoteReactivePowerControlImpl(GeneratorImpl generator) {
        super(generator);
    }

    private GeneratorImpl getGenerator() {
        return (GeneratorImpl) getExtendable();
    }

    @Override
    public String getName() {
        return RemoteReactivePowerControl.NAME;
    }

    @Override
    public double getTargetQ() {
        return getGenerator().checkResource().getAttributes().getRemoteReactivePowerControl().getTargetQ();
    }

    @Override
    public RemoteReactivePowerControl setTargetQ(double targetQ) {
        getGenerator().updateResource(res -> res.getAttributes().getRemoteReactivePowerControl().setTargetQ(targetQ));
        return this;
    }

    @Override
    public boolean isEnabled() {
        return getGenerator().checkResource().getAttributes().getRemoteReactivePowerControl().isEnabled();
    }

    @Override
    public RemoteReactivePowerControl setEnabled(boolean enabled) {
        getGenerator().updateResource(res -> res.getAttributes().getRemoteReactivePowerControl().setEnabled(enabled));
        return this;
    }

    @Override
    public Terminal getRegulatingTerminal() {
        RemoteReactivePowerControlAttributes attributes = getGenerator().checkResource().getAttributes().getRemoteReactivePowerControl();
        if (attributes != null) {
            TerminalRefAttributes terminalRefAttributes = attributes.getRegulatingTerminal();
            return TerminalRefUtils.getTerminal(getGenerator().getNetwork().getIndex(), terminalRefAttributes);
        }
        return null;
    }
}
