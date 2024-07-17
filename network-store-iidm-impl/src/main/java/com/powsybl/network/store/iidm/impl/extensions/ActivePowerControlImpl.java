/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.network.store.iidm.impl.AbstractInjectionImpl;
import com.powsybl.network.store.iidm.impl.PhaseTapChangerAdderImpl;
import com.powsybl.network.store.model.ActivePowerControlAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.OptionalDouble;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class ActivePowerControlImpl<I extends Injection<I>> extends AbstractExtension<I> implements ActivePowerControl<I> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PhaseTapChangerAdderImpl.class);

    record PLimits(double minP, double maxP) { }

    private PLimits getPLimits(I injection) {
        double maxP = Double.MAX_VALUE;
        double minP = -Double.MAX_VALUE;
        if (injection instanceof Generator generator) {
            maxP = generator.getMaxP();
            minP = generator.getMinP();
        } else if (injection instanceof Battery battery) {
            maxP = battery.getMaxP();
            minP = battery.getMinP();
        }
        return new PLimits(minP, maxP);
    }

    public ActivePowerControlImpl(I injection) {
        super(injection);
    }

    private ActivePowerControlAttributes getActivePowerControlAttributes() {
        return (ActivePowerControlAttributes) getInjection().getResource().getAttributes().getExtensionAttributes().get(ActivePowerControl.NAME);
    }

    @Override
    public boolean isParticipate() {
        return getActivePowerControlAttributes().isParticipate();
    }

    @Override
    public void setParticipate(boolean participate) {
        ActivePowerControlAttributes activePowerControlAttributes = (ActivePowerControlAttributes) getInjection().getResource().getAttributes().getExtensionAttributes().get(ActivePowerControl.NAME);
        boolean oldValue = activePowerControlAttributes.isParticipate();
        if (oldValue != participate) {
            getInjection().updateResource(res -> ((ActivePowerControlAttributes) res.getAttributes().getExtensionAttributes().get(ActivePowerControl.NAME)).setParticipate(participate));
            String variantId = getInjection().getNetwork().getVariantManager().getWorkingVariantId();
            getInjection().getNetwork().getIndex().notifyUpdate(getInjection(), "participate", variantId, oldValue, participate);
        }
    }

    @Override
    public double getDroop() {
        return getActivePowerControlAttributes().getDroop();
    }

    @Override
    public void setDroop(double droop) {
        double oldValue = getActivePowerControlAttributes().getDroop();
        if (oldValue != droop) {
            getInjection().updateResource(res -> ((ActivePowerControlAttributes) res.getAttributes().getExtensionAttributes().get(ActivePowerControl.NAME)).setDroop(droop));
            String variantId = getInjection().getNetwork().getVariantManager().getWorkingVariantId();
            getInjection().getNetwork().getIndex().notifyUpdate(getInjection(), "droop", variantId, oldValue, droop);
        }
    }

    @Override
    public double getParticipationFactor() {
        return getActivePowerControlAttributes().getParticipationFactor();
    }

    @Override
    public void setParticipationFactor(double participationFactor) {
        getInjection().updateResource(res -> ((ActivePowerControlAttributes) res.getAttributes().getExtensionAttributes().get(ActivePowerControl.NAME)).setParticipationFactor(participationFactor));
    }

    private double withinPMinMax(double value, I injection) {
        PLimits pLimits = getPLimits(injection);

        if (!Double.isNaN(value) && (value < pLimits.minP || value > pLimits.maxP)) {
            LOGGER.warn("targetP limit is now outside of pMin,pMax for component {}. Returning closest value in [pmin,pMax].", injection.getId());
            return value < pLimits.minP ? pLimits.minP : pLimits.maxP;
        }
        return value;
    }

    @Override
    public OptionalDouble getMinTargetP() {
        Double value = getActivePowerControlAttributes().getMinTargetP();
        return value == null || Double.isNaN(value) ? OptionalDouble.empty() : OptionalDouble.of(withinPMinMax(value, getExtendable()));
    }

    private double checkTargetPLimit(double targetPLimit, String name, I injection) {
        PLimits pLimits = getPLimits(injection);

        if (!Double.isNaN(targetPLimit) && (targetPLimit < pLimits.minP || targetPLimit > pLimits.maxP)) {
            throw new PowsyblException(String.format("%s value (%s) is not between minP and maxP for component %s",
                name,
                targetPLimit,
                injection.getId()));
        }
        return targetPLimit;
    }

    private void checkLimitOrder(double minTargetP, double maxTargetP) {
        if (!Double.isNaN(minTargetP) && !Double.isNaN(maxTargetP) && minTargetP > maxTargetP) {
            throw new PowsyblException("invalid targetP limits [" + minTargetP + ", " + maxTargetP + "]");
        }
    }

    @Override
    public void setMinTargetP(double minTargetP) {
        checkLimitOrder(minTargetP, getMaxTargetP().orElse(Double.NaN));
        getInjection().updateResource(res -> ((ActivePowerControlAttributes) res.getAttributes().getExtensionAttributes().get(ActivePowerControl.NAME)).setMinTargetP(checkTargetPLimit(minTargetP, "minTargetP", getExtendable())));
    }

    @Override
    public OptionalDouble getMaxTargetP() {
        Double value = getActivePowerControlAttributes().getMaxTargetP();
        return value == null || Double.isNaN(value) ? OptionalDouble.empty() : OptionalDouble.of(withinPMinMax(value, getExtendable()));
    }

    @Override
    public void setMaxTargetP(double maxTargetP) {
        checkLimitOrder(getMinTargetP().orElse(Double.NaN), maxTargetP);
        getInjection().updateResource(res -> ((ActivePowerControlAttributes) res.getAttributes().getExtensionAttributes().get(ActivePowerControl.NAME)).setMaxTargetP(checkTargetPLimit(maxTargetP, "maxTargetP", getExtendable())));
    }

    private AbstractInjectionImpl<?, ?> getInjection() {
        return (AbstractInjectionImpl<?, ?>) getExtendable();
    }

}
