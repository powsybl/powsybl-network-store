/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.StaticVarCompensator;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@Getter
public class RegulatingPoint {
    private final String regulatedEquipmentId;
    private final IdentifiableType identifiableType;
    private final TerminalImpl localTerminal;
    private TerminalImpl regulatingTerminal;
    @Setter
    private int regulationMode;

    public RegulatingPoint(String regulatedEquipmentId, IdentifiableType identifiableType, TerminalImpl localTerminal, int regulationMode) {
        this.regulatedEquipmentId = regulatedEquipmentId;
        this.identifiableType = identifiableType;
        this.localTerminal = localTerminal;
        this.regulatingTerminal = localTerminal;
        this.regulationMode = regulationMode;
    }

    public void setRegulatingTerminal(TerminalImpl regulatingTerminal) {
        regulatingTerminal.addNewRegulatingPoint(this);
        this.regulatingTerminal.removeRegulatingPoint(this);
        this.regulatingTerminal = regulatingTerminal;
    }

    public void setRegulatingTerminalAsLocalTerminal() {
        regulatingTerminal = localTerminal;
    }

    public void removeRegulatingTerminal() {
        regulatingTerminal = localTerminal;
        if (!localTerminal.getBusView().getBus().equals(regulatingTerminal.getBusView().getBus())) {
            switch (identifiableType) {
                case STATIC_VAR_COMPENSATOR -> regulationMode = StaticVarCompensator.RegulationMode.OFF.ordinal();
            }
        }
    }

    void remove() {
        regulatingTerminal.removeRegulatingPoint(this);
    }
}
