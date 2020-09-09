/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;

import java.util.Objects;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class SlackTerminalImplNetworkStore  extends AbstractExtension<VoltageLevel> implements SlackTerminal {
    private Terminal terminal;

    SlackTerminalImplNetworkStore(VoltageLevel voltageLevel, Terminal terminal) {
        super(voltageLevel);
        this.setTerminal(terminal);
    }

    @Override
    public Terminal getTerminal() {
        return this.terminal;
    }

    @Override
    public SlackTerminal setTerminal(Terminal terminal) {
        if (terminal != null && !terminal.getVoltageLevel().equals(getExtendable())) {
            throw new PowsyblException("Terminal given is not in the right VoltageLevel ("
                    + terminal.getVoltageLevel().getId() + " instead of " + getExtendable().getId() + ")");
        }
        this.terminal = terminal;
        return this;
    }

    @Override
    public boolean isCleanable() {
        return Objects.isNull(terminal);
    }

}
