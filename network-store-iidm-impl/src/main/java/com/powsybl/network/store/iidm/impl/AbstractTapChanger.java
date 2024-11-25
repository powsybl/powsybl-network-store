/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.AbstractRegulatingEquipmentAttributes;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.TapChangerAttributes;
import com.powsybl.network.store.model.TapChangerStepAttributes;
import lombok.Getter;

import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Supplier;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
abstract class AbstractTapChanger<H extends TapChangerParent, C extends AbstractTapChanger<H, C, A>, A extends TapChangerAttributes> implements Validable {

    protected final H parent;

    protected final NetworkObjectIndex index;

    private final String type;
    @Getter
    protected final TapChangerRegulatingPoint regulatingPoint;

    AbstractTapChanger(H parent, NetworkObjectIndex index, String type) {
        this.parent = parent;
        this.index = index;
        this.type = Objects.requireNonNull(type);
        regulatingPoint = new TapChangerRegulatingPoint(index, this, AbstractRegulatingEquipmentAttributes.class::cast);
    }

    AbstractIdentifiableImpl<?, ?> getTransformer() {
        return parent.getTransformer();
    }

    protected H getParent() {
        return parent;
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
        ValidationUtil.checkTargetDeadband(parent, type, regulating, getTargetDeadband(), ValidationLevel.STEADY_STATE_HYPOTHESIS, parent.getNetwork().getReportNodeContext().getReportNode());
        boolean oldValue = getAttributes().isRegulating();
        if (regulating != oldValue) {
            getTransformer().updateResource(res -> getAttributes(res).setRegulating(regulating));
            notifyUpdate(() -> getTapChangerAttribute() + ".regulating", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, regulating);
        }
        return (C) this;
    }

    public C setRegulationTerminal(Terminal regulatingTerminal) {
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, index.getNetwork());
        if (regulatingTerminal instanceof TerminalImpl<?>) {
            regulatingPoint.setRegulatingTerminal((TerminalImpl<?>) regulatingTerminal);
        } else {
            regulatingPoint.setRegulatingTerminalAsLocalTerminalAndRemoveRegulation();
        }
        return (C) this;
    }

    public Terminal getRegulationTerminal() {
        return regulatingPoint.getRegulatingTerminal();
    }

    public double getTargetDeadband() {
        return getAttributes().getTargetDeadband();
    }

    public C setTargetDeadband(double targetDeadBand) {
        ValidationUtil.checkTargetDeadband(parent, type, isRegulating(), targetDeadBand, ValidationLevel.STEADY_STATE_HYPOTHESIS, parent.getNetwork().getReportNodeContext().getReportNode());
        double oldValue = getAttributes().getTargetDeadband();
        if (Double.compare(targetDeadBand, oldValue) != 0) {
            getTransformer().updateResource(res -> getAttributes(res).setTargetDeadband(targetDeadBand));
            notifyUpdate(() -> getTapChangerAttribute() + ".targetDeadband", index.getNetwork().getVariantManager().getWorkingVariantId(), oldValue, targetDeadBand);
        }
        return (C) this;
    }

    abstract String getTapChangerAttribute();

    abstract int getHighTapPosition();

    protected abstract Integer getRelativeNeutralPosition();

    public OptionalInt getNeutralPosition() {
        var relativeNeutralPosition = getRelativeNeutralPosition();
        return relativeNeutralPosition != null ? OptionalInt.of(getLowTapPosition() + relativeNeutralPosition) : OptionalInt.empty();
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

    @Override
    public String getMessageHeader() {
        return getTransformer().getMessageHeader();
    }
}
