package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.ActivePowerControl;
import com.powsybl.network.store.iidm.impl.*;
import com.powsybl.network.store.model.InjectionAttributes;

public class ActivePowerControlImpl<I extends Injection<I>> implements ActivePowerControl<I> {

    private I injection;

    public ActivePowerControlImpl(I injection) {
        this.injection = injection;
    }

    public ActivePowerControlImpl(I injection, boolean isParticipate, float droop) {
        this(injection);
        getInjectionResources().getActivePowerControl().setDroop(droop);
        getInjectionResources().getActivePowerControl().setParticipate(isParticipate);
    }

    @Override
    public boolean isParticipate() {
        return getInjectionResources().getActivePowerControl().isParticipate();
    }

    @Override
    public void setParticipate(boolean isParticipate) {
        getInjectionResources().getActivePowerControl().setParticipate(isParticipate);
    }

    @Override
    public float getDroop() {
        return getInjectionResources().getActivePowerControl().getDroop();
    }

    @Override
    public void setDroop(float droop) {
        getInjectionResources().getActivePowerControl().setDroop(droop);
    }

    @Override
    public I getExtendable() {
        return injection;
    }

    @Override
    public void setExtendable(I i) {
        this.injection = i;
    }

    private InjectionAttributes getInjectionResources() {
        if (injection instanceof GeneratorImpl) {
            return ((GeneratorImpl) injection).getResource().getAttributes();
        } else if (injection instanceof BatteryImpl) {
            return ((BatteryImpl) injection).getResource().getAttributes();
        } else if (injection instanceof DanglingLineImpl) {
            return ((DanglingLineImpl) injection).getResource().getAttributes();
        } else if (injection instanceof LccConverterStationImpl) {
            return ((LccConverterStationImpl) injection).getResource().getAttributes();
        } else if (injection instanceof LoadImpl) {
            return ((LoadImpl) injection).getResource().getAttributes();
        } else if (injection instanceof ShuntCompensatorImpl) {
            return ((ShuntCompensatorImpl) injection).getResource().getAttributes();
        } else if (injection instanceof StaticVarCompensatorImpl) {
            return ((StaticVarCompensatorImpl) injection).getResource().getAttributes();
        } else if (injection instanceof VscConverterStationImpl) {
            return ((VscConverterStationImpl) injection).getResource().getAttributes();
        } else {
            throw new PowsyblException("Cannot convert to injection");
        } /*else if (injection instanceof BusbarSectionImpl) {
            //FIXME BusbarSection implements injection but busbarsection attribute does not implements injectionAttribute.
            return ((BusbarSectionImpl) injection).getResource().getAttributes();
        } */
    }
}
