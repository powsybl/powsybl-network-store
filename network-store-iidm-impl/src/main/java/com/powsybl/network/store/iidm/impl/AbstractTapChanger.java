/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.network.store.model.TapChangerAttributes;
import com.powsybl.network.store.model.TerminalRefAttributes;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
abstract class AbstractTapChanger<H extends TapChangerParent, C extends AbstractTapChanger<H, C, A>, A extends TapChangerAttributes> {

    protected final H parent;

    protected final NetworkObjectIndex index;

    protected final A attributes;

    private final String type;

    AbstractTapChanger(H parent, NetworkObjectIndex index, A attributes, String type) {
        this.parent = parent;
        this.index = index;
        this.attributes = attributes;
        this.type = Objects.requireNonNull(type);
    }

    void updateResource() {
        parent.getTransformer().updateResource();
    }

    protected void notifyUpdate(Supplier<String> attribute, Object oldValue, Object newValue) {
        notifyUpdate(attribute.get(), oldValue, newValue);
    }

    protected void notifyUpdate(String attribute, Object oldValue, Object newValue) {
        index.notifyUpdate(parent.getTransformer(), attribute, oldValue, newValue);
    }

    protected void notifyUpdate(Supplier<String> attribute, String variantId, Object oldValue, Object newValue) {
        index.notifyUpdate(parent.getTransformer(), attribute.get(), variantId, oldValue, newValue);
    }

    public int getLowTapPosition() {
        return attributes.getLowTapPosition();
    }

    public C setLowTapPosition(int lowTapPosition) {
        int oldValue = attributes.getLowTapPosition();
        attributes.setLowTapPosition(lowTapPosition);
        updateResource();
        notifyUpdate(() -> getTapChangerAttribute() + ".lowTapPosition", oldValue, lowTapPosition);
        return (C) this;
    }

    public int getTapPosition() {
        return attributes.getTapPosition();
    }

    public C setTapPosition(int tapPosition) {
        if (tapPosition < getLowTapPosition()
                || tapPosition > getHighTapPosition()) {
            throw new ValidationException(parent, "incorrect tap position "
                    + tapPosition + " [" + getLowTapPosition() + ", "
                    + getHighTapPosition() + "]");
        }
        int oldValue = attributes.getTapPosition();
        attributes.setTapPosition(tapPosition);
        updateResource();
        notifyUpdate(() -> getTapChangerAttribute() + ".tapPosition", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, tapPosition);
        return (C) this;
    }

    public boolean isRegulating() {
        return attributes.isRegulating();
    }

    public C setRegulating(boolean regulating) {
        ValidationUtil.checkTargetDeadband(parent, type, regulating, getTargetDeadband());
        boolean oldValue = attributes.isRegulating();
        attributes.setRegulating(regulating);
        updateResource();
        notifyUpdate(() -> getTapChangerAttribute() + ".regulating", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, regulating);
        return (C) this;
    }

    public Terminal getRegulationTerminal() {
        TerminalRefAttributes terminalRefAttributes = attributes.getRegulatingTerminal();
        return TerminalRefUtils.getTerminal(index, terminalRefAttributes);
    }

    public C setRegulationTerminal(Terminal regulatingTerminal) {
        if (regulatingTerminal != null && regulatingTerminal.getVoltageLevel().getNetwork() != parent.getNetwork()) {
            throw new ValidationException(parent, "regulation terminal is not part of the network");
        }
        TerminalRefAttributes oldValue = attributes.getRegulatingTerminal();
        attributes.setRegulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal));
        updateResource();
        notifyUpdate(() -> getTapChangerAttribute() + ".regulationTerminal", oldValue, TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal));
        return (C) this;
    }

    public double getTargetDeadband() {
        return attributes.getTargetDeadband();
    }

    public C setTargetDeadband(double targetDeadBand) {
        ValidationUtil.checkTargetDeadband(parent, type, isRegulating(), targetDeadBand);
        double oldValue = attributes.getTargetDeadband();
        attributes.setTargetDeadband(targetDeadBand);
        updateResource();
        notifyUpdate(() -> getTapChangerAttribute() + ".targetDeadband", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, targetDeadBand);
        return (C) this;
    }

    abstract String getTapChangerAttribute();

    abstract int getHighTapPosition();
}
