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
import com.powsybl.network.store.iidm.impl.TerminalRefUtils;
import com.powsybl.network.store.iidm.impl.VoltageLevelImpl;

import java.util.Objects;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class SlackTerminalImpl implements SlackTerminal {

    private VoltageLevelImpl vl;

    public SlackTerminalImpl(VoltageLevelImpl voltageLevel) {
        this.vl = voltageLevel;
    }

    public SlackTerminalImpl(VoltageLevelImpl voltageLevel, Terminal terminal) {
        this(voltageLevel.initSlackTerminalAttributes(terminal));
    }

    @Override
    public VoltageLevel getExtendable() {
        return vl;
    }

    @Override
    public void setExtendable(VoltageLevel vl) {
        this.vl = (VoltageLevelImpl) vl;
    }

    @Override
    public Terminal getTerminal() {
        return vl.getTerminal(vl.getResource().getAttributes().getSlackTerminal());
    }

    @Override
    public SlackTerminal setTerminal(Terminal terminal) {
        if (terminal != null && !terminal.getVoltageLevel().equals(getExtendable())) {
            throw new PowsyblException("Terminal given is not in the right VoltageLevel ("
                    + terminal.getVoltageLevel().getId() + " instead of " + getExtendable().getId() + ")");
        }
        vl.getResource().getAttributes().setSlackTerminal(TerminalRefUtils.getTerminalRefAttributes(terminal));
        return this;
    }

    @Override
    public boolean isEmpty() {
        return Objects.isNull(getTerminal());
    }

}
