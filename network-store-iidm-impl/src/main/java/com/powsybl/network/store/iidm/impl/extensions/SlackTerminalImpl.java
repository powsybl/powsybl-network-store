/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.network.store.iidm.impl.TerminalImpl;
import com.powsybl.network.store.iidm.impl.TerminalRefUtils;
import com.powsybl.network.store.iidm.impl.VoltageLevelImpl;

import java.util.Objects;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class SlackTerminalImpl extends AbstractIidmExtension<VoltageLevel> implements SlackTerminal {
    public SlackTerminalImpl(VoltageLevelImpl voltageLevel) {
        super(Objects.requireNonNull(voltageLevel));
        TerminalImpl<?> terminal = (TerminalImpl<?>) getTerminal();
        if (terminal != null) {
            terminal.getReferrerManager().register(this);
        }
    }

    @Override
    public VoltageLevel getExtendable() {
        return super.getExtendable();
    }

    @Override
    public void setExtendable(VoltageLevel voltageLevel) {
        super.setExtendable(voltageLevel);
    }

    @Override
    public Terminal getTerminal() {
        return ((VoltageLevelImpl) getExtendable()).getTerminal(((VoltageLevelImpl) getExtendable()).getResource().getAttributes().getSlackTerminal());
    }

    @Override
    public SlackTerminal setTerminal(Terminal terminal) {
        if (terminal != null && !terminal.getVoltageLevel().equals(getExtendable())) {
            throw new PowsyblException("Terminal given is not in the right VoltageLevel ("
                    + terminal.getVoltageLevel().getId() + " instead of " + getExtendable().getId() + ")");
        }
        // Get old terminal and, if not null, unregister the slack terminal from this old terminal
        Terminal oldTerminal = getTerminal();
        if (oldTerminal != null) {
            ((TerminalImpl<?>) oldTerminal).getReferrerManager().unregister(this);
        }
        ((VoltageLevelImpl) getExtendable()).updateResource(res -> res.getAttributes().setSlackTerminal(TerminalRefUtils.getTerminalRefAttributes(terminal)));
        // register the slack terminal in this new terminal, if not null
        if (terminal != null) {
            ((TerminalImpl<?>) terminal).getReferrerManager().register(this);
        }
        return this;
    }

    @Override
    public boolean isEmpty() {
        return Objects.isNull(getTerminal());
    }

    @Override
    public void onReferencedRemoval(Terminal removedTerminal) {
        Terminal oldTerminal = getTerminal();
        if (oldTerminal != null && oldTerminal.getConnectable().getId().equals(removedTerminal.getConnectable().getId())) {
            setTerminal(null);
        }
    }

    @Override
    public void onReferencedReplacement(Terminal oldReferenced, Terminal newReferenced) {
        Terminal oldTerminal = getTerminal();
        if (oldTerminal == null || oldTerminal.getConnectable().getId().equals(oldReferenced.getConnectable().getId())) {
            setTerminal(newReferenced);
        }
    }

    @Override
    public void cleanup() {
        if (getTerminal() != null) {
            ((TerminalImpl<?>) getTerminal()).getReferrerManager().unregister(this);
        }
    }
}
