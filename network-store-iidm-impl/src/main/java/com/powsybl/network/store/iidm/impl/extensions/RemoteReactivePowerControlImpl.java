/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.extensions.RemoteReactivePowerControl;
import com.powsybl.network.store.iidm.impl.GeneratorImpl;
import com.powsybl.network.store.iidm.impl.TerminalImpl;
import com.powsybl.network.store.iidm.impl.TerminalRefUtils;
import com.powsybl.network.store.model.RemoteReactivePowerControlAttributes;
import com.powsybl.network.store.model.TerminalRefAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jon Harper <jon.harper at rte-france.com.com>
 * TODO copied from powsybl-core, Implement this by storing in the attributes
 */
public class RemoteReactivePowerControlImpl extends AbstractIidmExtension<Generator> implements RemoteReactivePowerControl {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteReactivePowerControlImpl.class);

    public RemoteReactivePowerControlImpl(GeneratorImpl generator) {
        super(generator);
        TerminalImpl<?> terminal = (TerminalImpl<?>) getRegulatingTerminal();
        if (terminal != null) {
            terminal.getReferrerManager().register(this);
        }
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
        return getGenerator().getResource().getAttributes().getRemoteReactivePowerControl().getTargetQ();
    }

    @Override
    public RemoteReactivePowerControl setTargetQ(double targetQ) {
        getGenerator().updateResource(res -> res.getAttributes().getRemoteReactivePowerControl().setTargetQ(targetQ));
        return this;
    }

    @Override
    public boolean isEnabled() {
        return getGenerator().getResource().getAttributes().getRemoteReactivePowerControl().isEnabled();
    }

    @Override
    public RemoteReactivePowerControl setEnabled(boolean enabled) {
        getGenerator().updateResource(res -> res.getAttributes().getRemoteReactivePowerControl().setEnabled(enabled));
        return this;
    }

    @Override
    public Terminal getRegulatingTerminal() {
        RemoteReactivePowerControlAttributes attributes = getGenerator().getResource().getAttributes().getRemoteReactivePowerControl();
        if (attributes != null) {
            TerminalRefAttributes terminalRefAttributes = attributes.getRegulatingTerminal();
            return TerminalRefUtils.getTerminal(getGenerator().getNetwork().getIndex(), terminalRefAttributes);
        }
        return null;
    }

    @Override
    public RemoteReactivePowerControl setRegulatingTerminal(Terminal regulatingTerminal) {
        if (regulatingTerminal != null && regulatingTerminal.getVoltageLevel().getNetwork() != getExtendable().getNetwork()) {
            throw new ValidationException((GeneratorImpl) getExtendable(), "regulating terminal is not part of the network");
        }
        RemoteReactivePowerControlAttributes attributes = getGenerator().getResource().getAttributes().getRemoteReactivePowerControl();
        if (attributes != null) {
            TerminalRefAttributes oldValue = attributes.getRegulatingTerminal();
            TerminalRefAttributes terminalRefAttributes = TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal);
            getGenerator().updateResource(res -> res.getAttributes().getRemoteReactivePowerControl().setRegulatingTerminal(terminalRefAttributes));
            getGenerator().getIndex().notifyUpdate(getGenerator(), "regulatingTerminal", getGenerator().getNetwork().getVariantManager().getWorkingVariantId(), oldValue, terminalRefAttributes);
        }
        return this;
    }

    @Override
    public void onReferencedRemoval(Terminal removedTerminal) {
        // we cannot set regulating terminal to null because otherwise extension won't be consistent anymore
        // we cannot also as for voltage regulation fallback to a local terminal
        // so we just remove the extension
        LOGGER.warn("Remove 'RemoteReactivePowerControl' extension of generator '{}', because its regulating terminal has been removed", getExtendable().getId());
        getExtendable().removeExtension(RemoteReactivePowerControl.class);
    }

    @Override
    public void onReferencedReplacement(Terminal oldReferenced, Terminal newReferenced) {
        Terminal oldTerminal = getRegulatingTerminal();
        if (oldTerminal == null || oldTerminal.getConnectable().getId().equals(oldReferenced.getConnectable().getId())) {
            setRegulatingTerminal(newReferenced);
        }
    }

    @Override
    public void cleanup() {
        if (getRegulatingTerminal() != null) {
            ((TerminalImpl<?>) getRegulatingTerminal()).getReferrerManager().unregister(this);
        }
    }
}
