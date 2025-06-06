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
import com.powsybl.iidm.network.extensions.SlackTerminalAdder;
import com.powsybl.network.store.iidm.impl.TerminalRefUtils;
import com.powsybl.network.store.iidm.impl.VoltageLevelImpl;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class SlackTerminalAdderImpl extends AbstractIidmExtensionAdder<VoltageLevel, SlackTerminal> implements SlackTerminalAdder {

    private Terminal terminal;

    public SlackTerminalAdderImpl(VoltageLevel voltageLevel) {
        super(voltageLevel);
    }

    @Override
    public SlackTerminalAdder withTerminal(Terminal terminal) {
        this.terminal = terminal;
        return this;
    }

    @Override
    public SlackTerminal createExtension(VoltageLevel voltageLevel) {
        if (terminal == null) {
            throw new PowsyblException("Terminal needs to be set to create a SlackTerminal extension");
        }
        if (!terminal.getVoltageLevel().equals(voltageLevel)) {
            throw new PowsyblException("Terminal given is not in the right VoltageLevel ("
                    + terminal.getVoltageLevel().getId() + " instead of " + voltageLevel.getId() + ")");
        }
        ((VoltageLevelImpl) voltageLevel).updateResourceWithoutNotification(res -> res.getAttributes()
                .setSlackTerminal(TerminalRefUtils.getTerminalRefAttributes(terminal)));
        return new SlackTerminalImpl((VoltageLevelImpl) voltageLevel);
    }
}
