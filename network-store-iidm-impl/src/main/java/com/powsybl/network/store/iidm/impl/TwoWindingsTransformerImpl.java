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
import com.powsybl.network.store.model.*;

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
        return index.getVoltageLevel(getResource().getAttributes().getVoltageLevelId1())
                .orElseThrow(AssertionError::new)
                .getSubstation();
    }

    @Override
    public RatioTapChangerAdder newRatioTapChanger() {
        return new RatioTapChangerAdderImpl(this, index, TapChangerParentAttributes.class::cast);
    }

    @Override
    public PhaseTapChangerAdder newPhaseTapChanger() {
        return new PhaseTapChangerAdderImpl(this, index, TapChangerParentAttributes.class::cast);
    }

    @Override
    public void setPhaseTapChanger(PhaseTapChangerAttributes attributes) {
        PhaseTapChangerAttributes oldValue = getResource().getAttributes().getPhaseTapChangerAttributes();
        updateResource(res -> res.getAttributes().setPhaseTapChangerAttributes(attributes),
                AttributeFilter.BASIC, "phaseTapChanger", oldValue, attributes);
    }

    @Override
    public void setRatioTapChanger(RatioTapChangerAttributes attributes) {
        RatioTapChangerAttributes oldValue = getResource().getAttributes().getRatioTapChangerAttributes();
        updateResource(res -> res.getAttributes().setRatioTapChangerAttributes(attributes),
                AttributeFilter.BASIC, "ratioTapChanger", oldValue, attributes);
    }

    @Override
    public RatioTapChanger getRatioTapChanger() {
        var resource = getResource();
        if (resource.getAttributes().getRatioTapChangerAttributes() != null) {
            return new RatioTapChangerImpl(this, index, TapChangerParentAttributes.class::cast);
        }
        return null;
    }

    @Override
    public PhaseTapChanger getPhaseTapChanger() {
        var resource = getResource();
        if (resource.getAttributes().getPhaseTapChangerAttributes() != null) {
            return new PhaseTapChangerImpl(this, index, TapChangerParentAttributes.class::cast);
        }
        return null;
    }

    @Override
    public double getR() {
        return getResource().getAttributes().getR();
    }

    @Override
    public TwoWindingsTransformer setR(double r) {
        ValidationUtil.checkR(this, r);
        double oldValue = getResource().getAttributes().getR();
        if (r != oldValue) {
            updateResource(res -> res.getAttributes().setR(r),
                    AttributeFilter.BASIC, "r", oldValue, r);
        }
        return this;
    }

    @Override
    public double getX() {
        return getResource().getAttributes().getX();
    }

    @Override
    public TwoWindingsTransformer setX(double x) {
        ValidationUtil.checkX(this, x);
        double oldValue = getResource().getAttributes().getX();
        if (x != oldValue) {
            updateResource(res -> res.getAttributes().setX(x),
                    AttributeFilter.BASIC, "x", oldValue, x);
        }
        return this;
    }

    @Override
    public double getG() {
        return getResource().getAttributes().getG();
    }

    @Override
    public TwoWindingsTransformer setG(double g) {
        ValidationUtil.checkG(this, g);
        double oldValue = getResource().getAttributes().getG();
        if (g != oldValue) {
            updateResource(res -> res.getAttributes().setG(g),
                    AttributeFilter.BASIC, "g", oldValue, g);
        }
        return this;
    }

    @Override
    public double getB() {
        return getResource().getAttributes().getB();
    }

    @Override
    public TwoWindingsTransformer setB(double b) {
        ValidationUtil.checkB(this, b);
        double oldValue = getResource().getAttributes().getB();
        if (b != oldValue) {
            updateResource(res -> res.getAttributes().setB(b),
                    AttributeFilter.BASIC, "b", oldValue, b);
        }
        return this;
    }

    @Override
    public double getRatedU1() {
        return getResource().getAttributes().getRatedU1();
    }

    @Override
    public TwoWindingsTransformer setRatedU1(double ratedU1) {
        ValidationUtil.checkRatedU1(this, ratedU1);
        double oldValue = getResource().getAttributes().getRatedU1();
        if (ratedU1 != oldValue) {
            updateResource(res -> res.getAttributes().setRatedU1(ratedU1),
                    AttributeFilter.BASIC, "ratedU1", oldValue, ratedU1);
        }
        return this;
    }

    @Override
    public double getRatedU2() {
        return getResource().getAttributes().getRatedU2();
    }

    @Override
    public TwoWindingsTransformer setRatedU2(double ratedU2) {
        ValidationUtil.checkRatedU2(this, ratedU2);
        double oldValue = getResource().getAttributes().getRatedU2();
        if (ratedU2 != oldValue) {
            updateResource(res -> res.getAttributes().setRatedU2(ratedU2),
                    AttributeFilter.BASIC, "ratedU2", oldValue, ratedU2);
        }
        return this;
    }

    @Override
    public double getRatedS() {
        return getResource().getAttributes().getRatedS();
    }

    @Override
    public TwoWindingsTransformer setRatedS(double ratedS) {
        ValidationUtil.checkRatedS(this, ratedS);
        double oldValue = getResource().getAttributes().getRatedS();
        if (Double.compare(ratedS, oldValue) != 0) {
            updateResource(res -> res.getAttributes().setRatedS(ratedS),
                    AttributeFilter.BASIC, "ratedS", oldValue, ratedS);
        }
        return this;
    }

    @Override
    public void remove() {
        var resource = getResource();
        index.notifyBeforeRemoval(this);
        for (Terminal terminal : getTerminals()) {
            ((TerminalImpl<?>) terminal).removeAsRegulatingPoint();
            ((TerminalImpl<?>) terminal).getReferrerManager().notifyOfRemoval();
        }
        // invalidate calculated buses before removal otherwise voltage levels won't be accessible anymore for topology invalidation!
        invalidateCalculatedBuses(getTerminals());
        index.removeTwoWindingsTransformer(resource.getId());
        index.notifyAfterRemoval(resource.getId());
    }

    @Override
    public String getTapChangerAttribute() {
        return "TapChanger";
    }

    @Override
    public <E extends Extension<TwoWindingsTransformer>> void addExtension(Class<? super E> type, E extension) {
        var resource = getResource();
        if (type == CgmesTapChangers.class) {
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
        var resource = getResource();
        TwoWindingsTransformerPhaseAngleClockAttributes phaseAngleClockAttributes = resource.getAttributes().getPhaseAngleClockAttributes();
        if (phaseAngleClockAttributes != null) {
            extension = (E) new TwoWindingsTransformerPhaseAngleClockImpl(this);
        }
        return extension;
    }

    private <E extends Extension<TwoWindingsTransformer>> E createCgmesTapChangers() {
        E extension = null;
        var resource = getResource();
        if (resource.getAttributes().getCgmesTapChangerAttributesList() != null) {
            extension = (E) new CgmesTapChangersImpl(this);
        }
        return extension;
    }
}
