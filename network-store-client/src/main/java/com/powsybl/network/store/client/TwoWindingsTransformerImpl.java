/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.TwoWindingsTransformerAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class TwoWindingsTransformerImpl extends AbstractBranchImpl<TwoWindingsTransformer, TwoWindingsTransformerAttributes> implements TwoWindingsTransformer {

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
    public Substation getSubstation() {
        return index.getVoltageLevel(resource.getAttributes().getVoltageLevelId1())
                    .orElseThrow(AssertionError::new)
                    .getSubstation();
    }

    @Override
    public RatioTapChangerAdder newRatioTapChanger() {
        return new RatioTapChangerAdderImpl();
    }

    @Override
    public PhaseTapChangerAdder newPhaseTapChanger() {
        return new PhaseTapChangerAdderImpl(resource);
    }

    @Override
    public RatioTapChanger getRatioTapChanger() {
        return null; // FIXME
    }

    @Override
    public PhaseTapChanger getPhaseTapChanger() {
        if (resource.getAttributes().getPhaseTapChangerAttributes() != null) {
            return new PhaseTapChangerImpl(resource.getAttributes().getPhaseTapChangerAttributes());
        }
        return null;
    }

    @Override
    public double getR() {
        return resource.getAttributes().getR();
    }

    @Override
    public TwoWindingsTransformer setR(double r) {
        resource.getAttributes().setR(r);
        return this;
    }

    @Override
    public double getX() {
        return resource.getAttributes().getX();
    }

    @Override
    public TwoWindingsTransformer setX(double x) {
        resource.getAttributes().setX(x);
        return this;
    }

    @Override
    public double getG() {
        return resource.getAttributes().getG();
    }

    @Override
    public TwoWindingsTransformer setG(double g) {
        resource.getAttributes().setG(g);
        return this;
    }

    @Override
    public double getB() {
        return resource.getAttributes().getB();
    }

    @Override
    public TwoWindingsTransformer setB(double b) {
        resource.getAttributes().setB(b);
        return this;
    }

    @Override
    public double getRatedU1() {
        return resource.getAttributes().getRatedU1();
    }

    @Override
    public TwoWindingsTransformer setRatedU1(double ratedU1) {
        resource.getAttributes().setRatedU1(ratedU1);
        return this;
    }

    @Override
    public double getRatedU2() {
        return resource.getAttributes().getRatedU2();
    }

    @Override
    public TwoWindingsTransformer setRatedU2(double ratedU2) {
        resource.getAttributes().setRatedU2(ratedU2);
        return this;
    }
}
