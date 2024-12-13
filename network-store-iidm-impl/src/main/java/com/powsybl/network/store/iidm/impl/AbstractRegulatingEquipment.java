/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.network.store.model.AbstractRegulatingEquipmentAttributes;
import com.powsybl.network.store.model.InjectionAttributes;
import com.powsybl.network.store.model.Resource;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
@Setter
@Getter
public abstract class AbstractRegulatingEquipment<I extends Injection<I>, D extends InjectionAttributes> extends AbstractInjectionImpl<I, D> implements Injection<I> {

    protected final RegulatingPoint<I, D> regulatingPoint;

    protected AbstractRegulatingEquipment(NetworkObjectIndex index, Resource<D> resource) {
        super(index, resource);
        regulatingPoint = new RegulatingPoint<>(index, this, AbstractRegulatingEquipmentAttributes.class::cast);
    }

    // should be setRegulatingTerminal but there is already a method with the same name in the regulating equipments
    protected void setRegTerminal(Terminal regulatingTerminal) {
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
        if (regulatingTerminal instanceof TerminalImpl<?>) {
            regulatingPoint.setRegulatingTerminal((TerminalImpl<?>) regulatingTerminal);
        } else {
            regulatingPoint.setRegulatingTerminalAsLocalTerminalAndRemoveRegulation();
        }
    }

    public Terminal getRegulatingTerminal() {
        return regulatingPoint.getRegulatingTerminal();
    }

    public Boolean isRegulating() {
        return regulatingPoint.isRegulating();
    }

    public void setRegulating(boolean regulating) {
        regulatingPoint.setRegulating(regulating);
    }
}
