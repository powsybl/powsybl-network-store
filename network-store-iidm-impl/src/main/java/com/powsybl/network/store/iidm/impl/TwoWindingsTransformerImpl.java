/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.TwoWindingsTransformerAttributes;

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
    public ConnectableType getType() {
        return ConnectableType.TWO_WINDINGS_TRANSFORMER;
    }

    @Override
    protected TwoWindingsTransformer getBranch() {
        return this;
    }

    @Override
    public Identifiable getTransformer() {
        return this;
    }

    @Override
    public Substation getSubstation() {
        return index.getVoltageLevel(resource.getAttributes().getVoltageLevelId1())
                    .orElseThrow(AssertionError::new)
                    .getSubstation();
    }

    @Override
    public RatioTapChangerAdder newRatioTapChanger() {
        return new RatioTapChangerAdderImpl(this, index, resource.getAttributes(), getId());
    }

    @Override
    public PhaseTapChangerAdder newPhaseTapChanger() {
        return new PhaseTapChangerAdderImpl(this, index, resource.getAttributes(), getId());
    }

    @Override
    public RatioTapChanger getRatioTapChanger() {
        if (resource.getAttributes().getRatioTapChangerAttributes() != null) {
            return new RatioTapChangerImpl(this, index, resource.getAttributes().getRatioTapChangerAttributes());
        }
        return null;
    }

    @Override
    public PhaseTapChanger getPhaseTapChanger() {
        if (resource.getAttributes().getPhaseTapChangerAttributes() != null) {
            return new PhaseTapChangerImpl(this, index, resource.getAttributes().getPhaseTapChangerAttributes());
        }
        return null;
    }

    @Override
    public double getR() {
        return resource.getAttributes().getR();
    }

    @Override
    public TwoWindingsTransformer setR(double r) {
        ValidationUtil.checkR(this, r);
        double oldValue = resource.getAttributes().getR();
        resource.getAttributes().setR(r);
        index.notifyUpdate(this, "r", oldValue, r);
        return this;
    }

    @Override
    public double getX() {
        return resource.getAttributes().getX();
    }

    @Override
    public TwoWindingsTransformer setX(double x) {
        ValidationUtil.checkX(this, x);
        double oldValue = resource.getAttributes().getX();
        resource.getAttributes().setX(x);
        index.notifyUpdate(this, "x", oldValue, x);
        return this;
    }

    @Override
    public double getG() {
        return resource.getAttributes().getG();
    }

    @Override
    public TwoWindingsTransformer setG(double g) {
        ValidationUtil.checkG(this, g);
        double oldValue = resource.getAttributes().getG();
        resource.getAttributes().setG(g);
        index.notifyUpdate(this, "g", oldValue, g);
        return this;
    }

    @Override
    public double getB() {
        return resource.getAttributes().getB();
    }

    @Override
    public TwoWindingsTransformer setB(double b) {
        ValidationUtil.checkB(this, b);
        double oldValue = resource.getAttributes().getB();
        resource.getAttributes().setB(b);
        index.notifyUpdate(this, "b", oldValue, b);
        return this;
    }

    @Override
    public double getRatedU1() {
        return resource.getAttributes().getRatedU1();
    }

    @Override
    public TwoWindingsTransformer setRatedU1(double ratedU1) {
        ValidationUtil.checkRatedU1(this, ratedU1);
        double oldValue = resource.getAttributes().getRatedU1();
        resource.getAttributes().setRatedU1(ratedU1);
        index.notifyUpdate(this, "ratedU1", oldValue, ratedU1);
        return this;
    }

    @Override
    public double getRatedU2() {
        return resource.getAttributes().getRatedU2();
    }

    @Override
    public TwoWindingsTransformer setRatedU2(double ratedU2) {
        ValidationUtil.checkRatedU2(this, ratedU2);
        double oldValue = resource.getAttributes().getRatedU2();
        resource.getAttributes().setRatedU2(ratedU2);
        index.notifyUpdate(this, "ratedU2", oldValue, ratedU2);
        return this;
    }

    @Override
    public double getRatedS() {
        // TODO
        return Double.NaN;
    }

    @Override
    public TwoWindingsTransformer setRatedS(double ratedS) {
        // TODO
        return this;
    }

    @Override
    protected String getTypeDescription() {
        return "2 windings transformer";
    }

    @Override
    public void remove() {
        index.removeTwoWindingsTransformer(resource.getId());
        index.notifyRemoval(this);
    }

    @Override
    public String getTapChangerAttribute() {
        return "TapChanger";
    }
}
