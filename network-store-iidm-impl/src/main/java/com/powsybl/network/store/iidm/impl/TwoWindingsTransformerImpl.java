/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.cgmes.extensions.CgmesTapChangers;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerPhaseAngleClock;
import com.powsybl.network.store.iidm.impl.extensions.CgmesTapChangersImpl;
import com.powsybl.network.store.iidm.impl.extensions.TwoWindingsTransformerPhaseAngleClockImpl;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.TwoWindingsTransformerAttributes;
import com.powsybl.network.store.model.TwoWindingsTransformerPhaseAngleClockAttributes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public class TwoWindingsTransformerImpl extends AbstractBranchImpl<TwoWindingsTransformer, TwoWindingsTransformerAttributes> implements TwoWindingsTransformer, TapChangerParent {

    public TwoWindingsTransformerImpl(NetworkObjectIndex index, Resource<TwoWindingsTransformerAttributes> resource) {
        super(index, resource);
    }

    static TwoWindingsTransformerImpl create(NetworkObjectIndex index, Resource<TwoWindingsTransformerAttributes> resource) {
        return new TwoWindingsTransformerImpl(index, resource);
    }

    @Override
    protected TwoWindingsTransformer getBranch() {
        return this;
    }

    @Override
    public TwoWindingsTransformerImpl getTransformer() {
        return this;
    }

    @Override
    public Optional<Substation> getSubstation() {
        return index.getVoltageLevel(checkResource().getAttributes().getVoltageLevelId1())
                .orElseThrow(AssertionError::new)
                .getSubstation();
    }

    @Override
    public RatioTapChangerAdder newRatioTapChanger() {
        return new RatioTapChangerAdderImpl(this, index, checkResource().getAttributes(), getId());
    }

    @Override
    public PhaseTapChangerAdder newPhaseTapChanger() {
        return new PhaseTapChangerAdderImpl(this, index, checkResource().getAttributes(), getId());
    }

    @Override
    public RatioTapChanger getRatioTapChanger() {
        var resource = checkResource();
        if (resource.getAttributes().getRatioTapChangerAttributes() != null) {
            return new RatioTapChangerImpl(this, index, resource.getAttributes().getRatioTapChangerAttributes());
        }
        return null;
    }

    @Override
    public PhaseTapChanger getPhaseTapChanger() {
        var resource = checkResource();
        if (resource.getAttributes().getPhaseTapChangerAttributes() != null) {
            return new PhaseTapChangerImpl(this, index, resource.getAttributes().getPhaseTapChangerAttributes());
        }
        return null;
    }

    @Override
    public double getR() {
        return checkResource().getAttributes().getR();
    }

    @Override
    public TwoWindingsTransformer setR(double r) {
        var resource = checkResource();
        ValidationUtil.checkR(this, r);
        double oldValue = resource.getAttributes().getR();
        resource.getAttributes().setR(r);
        updateResource();
        index.notifyUpdate(this, "r", oldValue, r);
        return this;
    }

    @Override
    public double getX() {
        return checkResource().getAttributes().getX();
    }

    @Override
    public TwoWindingsTransformer setX(double x) {
        var resource = checkResource();
        ValidationUtil.checkX(this, x);
        double oldValue = resource.getAttributes().getX();
        resource.getAttributes().setX(x);
        updateResource();
        index.notifyUpdate(this, "x", oldValue, x);
        return this;
    }

    @Override
    public double getG() {
        return checkResource().getAttributes().getG();
    }

    @Override
    public TwoWindingsTransformer setG(double g) {
        var resource = checkResource();
        ValidationUtil.checkG(this, g);
        double oldValue = resource.getAttributes().getG();
        resource.getAttributes().setG(g);
        updateResource();
        index.notifyUpdate(this, "g", oldValue, g);
        return this;
    }

    @Override
    public double getB() {
        return checkResource().getAttributes().getB();
    }

    @Override
    public TwoWindingsTransformer setB(double b) {
        var resource = checkResource();
        ValidationUtil.checkB(this, b);
        double oldValue = resource.getAttributes().getB();
        resource.getAttributes().setB(b);
        updateResource();
        index.notifyUpdate(this, "b", oldValue, b);
        return this;
    }

    @Override
    public double getRatedU1() {
        return checkResource().getAttributes().getRatedU1();
    }

    @Override
    public TwoWindingsTransformer setRatedU1(double ratedU1) {
        var resource = checkResource();
        ValidationUtil.checkRatedU1(this, ratedU1);
        double oldValue = resource.getAttributes().getRatedU1();
        resource.getAttributes().setRatedU1(ratedU1);
        updateResource();
        index.notifyUpdate(this, "ratedU1", oldValue, ratedU1);
        return this;
    }

    @Override
    public double getRatedU2() {
        return checkResource().getAttributes().getRatedU2();
    }

    @Override
    public TwoWindingsTransformer setRatedU2(double ratedU2) {
        var resource = checkResource();
        ValidationUtil.checkRatedU2(this, ratedU2);
        double oldValue = resource.getAttributes().getRatedU2();
        resource.getAttributes().setRatedU2(ratedU2);
        updateResource();
        index.notifyUpdate(this, "ratedU2", oldValue, ratedU2);
        return this;
    }

    @Override
    public double getRatedS() {
        return checkResource().getAttributes().getRatedS();
    }

    @Override
    public TwoWindingsTransformer setRatedS(double ratedS) {
        var resource = checkResource();
        ValidationUtil.checkRatedS(this, ratedS);
        double oldValue = resource.getAttributes().getRatedS();
        resource.getAttributes().setRatedS(ratedS);
        updateResource();
        index.notifyUpdate(this, "ratedS", oldValue, ratedS);
        return this;
    }

    @Override
    public void remove(boolean removeDanglingSwitches) {
        var resource = checkResource();
        index.notifyBeforeRemoval(this);
        index.removeTwoWindingsTransformer(resource.getId());
        getTerminal1().getVoltageLevel().invalidateCalculatedBuses();
        getTerminal2().getVoltageLevel().invalidateCalculatedBuses();
        index.notifyAfterRemoval(resource.getId());
        if (removeDanglingSwitches) {
            getTerminal1().removeDanglingSwitches();
            getTerminal2().removeDanglingSwitches();
        }
    }

    @Override
    public String getTapChangerAttribute() {
        return "TapChanger";
    }

    @Override
    public <E extends Extension<TwoWindingsTransformer>> void addExtension(Class<? super E> type, E extension) {
        var resource = checkResource();
        if (type == TwoWindingsTransformerPhaseAngleClock.class) {
            TwoWindingsTransformerPhaseAngleClock twoWindingsTransformerPhaseAngleClock = (TwoWindingsTransformerPhaseAngleClock) extension;
            resource.getAttributes().setPhaseAngleClockAttributes(TwoWindingsTransformerPhaseAngleClockAttributes.builder()
                    .phaseAngleClock(twoWindingsTransformerPhaseAngleClock.getPhaseAngleClock()).build());
        } else if (type == CgmesTapChangers.class) {
            resource.getAttributes().setCgmesTapChangerAttributesList(new ArrayList<>());
        }
        super.addExtension(type, extension);
    }

    @Override
    public <E extends Extension<TwoWindingsTransformer>> Collection<E> getExtensions() {
        Collection<E> extensions = super.getExtensions();
        E extension = createPhaseAngleClock();
        if (extension != null) {
            extensions.add(extension);
        }
        extension = createCgmesTapChangers();
        if (extension != null) {
            extensions.add(extension);
        }
        return extensions;
    }

    @Override
    public <E extends Extension<TwoWindingsTransformer>> E getExtension(Class<? super E> type) {
        if (type == TwoWindingsTransformerPhaseAngleClock.class) {
            return createPhaseAngleClock();
        } else if (type == CgmesTapChangers.class) {
            return createCgmesTapChangers();
        }
        return super.getExtension(type);
    }

    @Override
    public <E extends Extension<TwoWindingsTransformer>> E getExtensionByName(String name) {
        E extension;
        if (name.equals(TwoWindingsTransformerPhaseAngleClock.NAME)) {
            extension = createPhaseAngleClock();
        } else if (name.equals(CgmesTapChangers.NAME)) {
            extension = createCgmesTapChangers();
        } else {
            extension = super.getExtensionByName(name);
        }
        return extension;
    }

    @SuppressWarnings("unchecked")
    private <E extends Extension<TwoWindingsTransformer>> E createPhaseAngleClock() {
        E extension = null;
        var resource = checkResource();
        TwoWindingsTransformerPhaseAngleClockAttributes phaseAngleClockAttributes = resource.getAttributes().getPhaseAngleClockAttributes();
        if (phaseAngleClockAttributes != null) {
            extension = (E) new TwoWindingsTransformerPhaseAngleClockImpl(this, phaseAngleClockAttributes.getPhaseAngleClock());
        }
        return extension;
    }

    private <E extends Extension<TwoWindingsTransformer>> E createCgmesTapChangers() {
        E extension = null;
        var resource = checkResource();
        if (resource.getAttributes().getCgmesTapChangerAttributesList() != null) {
            extension = (E) new CgmesTapChangersImpl(this);
        }
        return extension;
    }
}
