/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.ShuntCompensatorNonLinearModel;
import com.powsybl.network.store.model.ShuntCompensatorNonLinearModelAttributes;
import com.powsybl.network.store.model.ShuntCompensatorNonLinearSectionAttributes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ShuntCompensatorNonLinearModelImpl implements ShuntCompensatorNonLinearModel {

    static class SectionImpl implements Section {

        private final ShuntCompensatorNonLinearSectionAttributes attributes;

        public SectionImpl(ShuntCompensatorNonLinearSectionAttributes attributes) {
            this.attributes = attributes;
        }

        static SectionImpl create(ShuntCompensatorNonLinearSectionAttributes attributes) {
            return new SectionImpl(attributes);
        }

        @Override
        public double getB() {
            return attributes.getB();
        }

        @Override
        public Section setB(double b) {
            attributes.setB(b);
            return this;
        }

        @Override
        public double getG() {
            return attributes.getG();
        }

        @Override
        public Section setG(double g) {
            attributes.setG(g);
            return this;
        }
    }

    private final ShuntCompensatorNonLinearModelAttributes attributes;

    public ShuntCompensatorNonLinearModelImpl(ShuntCompensatorNonLinearModelAttributes attributes) {
        this.attributes = attributes;
    }

    static ShuntCompensatorNonLinearModelImpl create(ShuntCompensatorNonLinearModelAttributes attributes) {
        return new ShuntCompensatorNonLinearModelImpl(attributes);
    }

    @Override
    public List<Section> getAllSections() {
        return attributes.getSections().stream().map(SectionImpl::create).collect(Collectors.toList());
    }
}
