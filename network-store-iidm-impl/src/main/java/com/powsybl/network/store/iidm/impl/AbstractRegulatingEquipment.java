package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.network.store.model.AbstractRegulatingEquipmentAttributes;
import com.powsybl.network.store.model.InjectionAttributes;
import com.powsybl.network.store.model.Resource;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class AbstractRegulatingEquipment<I extends Injection<I>, D extends InjectionAttributes> extends AbstractInjectionImpl<I, D> implements Injection<I> {

    RegulatingPoint regulatingPoint;

    protected AbstractRegulatingEquipment(NetworkObjectIndex index, Resource<D> resource) {
        super(index, resource);
        regulatingPoint = new RegulatingPoint(index, this, AbstractRegulatingEquipmentAttributes.class::cast);
    }

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

}
