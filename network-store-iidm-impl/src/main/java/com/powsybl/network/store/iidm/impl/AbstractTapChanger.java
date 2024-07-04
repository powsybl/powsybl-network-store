/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ValidationLevel;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.TapChangerAttributes;
import com.powsybl.network.store.model.TapChangerStepAttributes;
import com.powsybl.network.store.model.TerminalRefAttributes;

import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Supplier;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
abstract class AbstractTapChanger<H extends TapChangerParent, C extends AbstractTapChanger<H, C, A>, A extends TapChangerAttributes> {

    protected final H parent;

    protected final NetworkObjectIndex index;

    private final String type;

    AbstractTapChanger(H parent, NetworkObjectIndex index, String type) {
        this.parent = parent;
        this.index = index;
        this.type = Objects.requireNonNull(type);
    }

    AbstractIdentifiableImpl<?, ?> getTransformer() {
        return parent.getTransformer();
    }

    protected Resource<?> getResource() {
        return getTransformer().getResource();
    }

    protected abstract TapChangerAttributes getAttributes();

    protected abstract TapChangerAttributes getAttributes(Resource<?> resource);

    protected void notifyUpdate(Supplier<String> attribute, Object oldValue, Object newValue) {
        notifyUpdate(attribute.get(), oldValue, newValue);
    }

    protected void notifyUpdate(String attribute, Object oldValue, Object newValue) {
        index.notifyUpdate(getTransformer(), attribute, oldValue, newValue);
    }

    protected void notifyUpdate(Supplier<String> attribute, String variantId, Object oldValue, Object newValue) {
        index.notifyUpdate(getTransformer(), attribute.get(), variantId, oldValue, newValue);
    }

    public int getLowTapPosition() {
        return getAttributes().getLowTapPosition();
    }

    public C setLowTapPosition(int lowTapPosition) {
        int oldValue = getAttributes().getLowTapPosition();
        if (lowTapPosition != oldValue) {
            getTransformer().updateResource(res -> getAttributes(res).setLowTapPosition(lowTapPosition));
            notifyUpdate(() -> getTapChangerAttribute() + ".lowTapPosition", oldValue, lowTapPosition);
        }
        return (C) this;
    }

    public int getTapPosition() {
        return getAttributes().getTapPosition();
    }

    public C setTapPosition(int tapPosition) {
        if (tapPosition < getLowTapPosition()
                || tapPosition > getHighTapPosition()) {
            throw new ValidationException(parent, "incorrect tap position "
                    + tapPosition + " [" + getLowTapPosition() + ", "
                    + getHighTapPosition() + "]");
        }
        int oldValue = getAttributes().getTapPosition();
        if (tapPosition != oldValue) {
            getTransformer().updateResource(res -> getAttributes(res).setTapPosition(tapPosition));
            notifyUpdate(() -> getTapChangerAttribute() + ".tapPosition", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, tapPosition);
        }
        return (C) this;
    }

    public boolean isRegulating() {
        return getAttributes().isRegulating();
    }

    public C setRegulating(boolean regulating) {
        ValidationUtil.checkTargetDeadband(parent, type, regulating, getTargetDeadband(), ValidationLevel.STEADY_STATE_HYPOTHESIS);
        boolean oldValue = getAttributes().isRegulating();
        if (regulating != oldValue) {
            getTransformer().updateResource(res -> getAttributes(res).setRegulating(regulating));
            notifyUpdate(() -> getTapChangerAttribute() + ".regulating", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, regulating);
        }
        return (C) this;
    }

    public Terminal getRegulationTerminal() {
        TerminalRefAttributes terminalRefAttributes = getAttributes().getRegulatingTerminal();
        return TerminalRefUtils.getTerminal(index, terminalRefAttributes);
    }

    public C setRegulationTerminal(Terminal regulatingTerminal) {
        if (regulatingTerminal != null && regulatingTerminal.getVoltageLevel().getNetwork() != parent.getNetwork()) {
            throw new ValidationException(parent, "regulation terminal is not part of the network");
        }
        TerminalRefAttributes oldValue = getAttributes().getRegulatingTerminal();
        getTransformer().updateResource(res -> getAttributes(res).setRegulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal)));
        notifyUpdate(() -> getTapChangerAttribute() + ".regulationTerminal", oldValue, TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal));
        return (C) this;
    }

    public double getTargetDeadband() {
        return getAttributes().getTargetDeadband();
    }

    public C setTargetDeadband(double targetDeadBand) {
        ValidationUtil.checkTargetDeadband(parent, type, isRegulating(), targetDeadBand, ValidationLevel.STEADY_STATE_HYPOTHESIS);
        double oldValue = getAttributes().getTargetDeadband();
        if (Double.compare(targetDeadBand, oldValue) != 0) {
            getTransformer().updateResource(res -> getAttributes(res).setTargetDeadband(targetDeadBand));
            notifyUpdate(() -> getTapChangerAttribute() + ".targetDeadband", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, targetDeadBand);
        }
        return (C) this;
    }

    abstract String getTapChangerAttribute();

    abstract int getHighTapPosition();

    public OptionalInt getNeutralPosition() {
        TapChangerAttributes attributes = getAttributes();
        Integer relativeNeutralPosition = attributes.getRelativeNeutralPosition();
        return relativeNeutralPosition != null ? OptionalInt.of(attributes.getLowTapPosition() + relativeNeutralPosition) : OptionalInt.empty();
    }

    protected C setSteps(List<TapChangerStepAttributes> steps) {
        if (steps == null || steps.isEmpty()) {
            throw new ValidationException(parent, "a tap changer shall have at least one step");
        }
        steps.forEach(step -> C.validateStep(step, parent));

        // We check if the tap position is still correct
        int newHighTapPosition = getLowTapPosition() + steps.size() - 1;
        if (getTapPosition() > newHighTapPosition) {
            throw new ValidationException(parent, "incorrect tap position "
                + getTapPosition() + " [" + getLowTapPosition() + ", "
                + newHighTapPosition + "]");
        }

        List<TapChangerStepAttributes> oldValue = getAttributes().getSteps();
        getTransformer().updateResource(res -> getAttributes(res).setSteps(steps));
        notifyUpdate(() -> getTapChangerAttribute() + ".steps", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, steps);
        return (C) this;
    }

    public static void validateStep(TapChangerStepAttributes step, TapChangerParent parent) {
        if (Double.isNaN(step.getRho())) {
            throw new ValidationException(parent, "step rho is not set");
        }
        if (Double.isNaN(step.getR())) {
            throw new ValidationException(parent, "step r is not set");
        }
        if (Double.isNaN(step.getX())) {
            throw new ValidationException(parent, "step x is not set");
        }
        if (Double.isNaN(step.getG())) {
            throw new ValidationException(parent, "step g is not set");
        }
        if (Double.isNaN(step.getB())) {
            throw new ValidationException(parent, "step b is not set");
        }
    }
}
