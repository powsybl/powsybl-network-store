/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.ShuntCompensatorNonLinearModel;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.ShuntCompensatorAttributes;
import com.powsybl.network.store.model.ShuntCompensatorNonLinearModelAttributes;
import com.powsybl.network.store.model.ShuntCompensatorNonLinearSectionAttributes;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ShuntCompensatorNonLinearModelImpl implements ShuntCompensatorNonLinearModel {

    class SectionImpl implements Section {

        private final ShuntCompensatorImpl shuntCompensator;

        private final int i;

        public SectionImpl(ShuntCompensatorImpl shuntCompensator, int i) {
            this.shuntCompensator = Objects.requireNonNull(shuntCompensator);
            this.i = i;
        }

        private ShuntCompensatorNonLinearSectionAttributes getSectionAttributes() {
            return getAttributes().getSections().get(i);
        }

        @Override
        public double getB() {
            return getSectionAttributes().getB();
        }

        @Override
        public Section setB(double b) {
            ValidationUtil.checkB(shuntCompensator, b);
            shuntCompensator.updateResource(res -> getSectionAttributes().setB(b));
            return this;
        }

        @Override
        public double getG() {
            return getSectionAttributes().getG();
        }

        @Override
        public Section setG(double g) {
            ValidationUtil.checkG(shuntCompensator, g);
            shuntCompensator.updateResource(res -> getSectionAttributes().setG(g));
            return this;
        }
    }

    private final ShuntCompensatorImpl shuntCompensator;

    public ShuntCompensatorNonLinearModelImpl(ShuntCompensatorImpl shuntCompensator) {
        this.shuntCompensator = Objects.requireNonNull(shuntCompensator);
    }

    private Resource<ShuntCompensatorAttributes> getResource() {
        return shuntCompensator.checkResource();
    }

    private static ShuntCompensatorNonLinearModelAttributes getAttributes(Resource<ShuntCompensatorAttributes> resource) {
        return (ShuntCompensatorNonLinearModelAttributes) resource.getAttributes().getModel();
    }

    private ShuntCompensatorNonLinearModelAttributes getAttributes() {
        return getAttributes(getResource());
    }

    @Override
    public List<Section> getAllSections() {
        return IntStream.range(0, getAttributes().getSections().size())
                .boxed()
                .map((Function<Integer, Section>) i -> new SectionImpl(shuntCompensator, i))
                .collect(Collectors.toList());
    }
}
