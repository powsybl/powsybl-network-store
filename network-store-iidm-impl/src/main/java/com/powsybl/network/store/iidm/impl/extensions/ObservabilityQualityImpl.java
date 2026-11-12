/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.extensions.ObservabilityQuality;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ObservabilityQualityImpl<T> implements ObservabilityQuality<T> {

    private double standardDeviation;

    private Boolean redundant;

    private final Consumer<ObservabilityQuality<?>> setter;

    public ObservabilityQualityImpl(Consumer<ObservabilityQuality<?>> setter, double standardDeviation, Boolean redundant) {
        Objects.requireNonNull(setter);
        this.standardDeviation = standardDeviation;
        this.redundant = redundant;
        this.setter = setter;
    }

    @Override
    public double getStandardDeviation() {
        return standardDeviation;
    }

    @Override
    public ObservabilityQuality<T> setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
        this.setter.accept(this);
        return this;
    }

    @Override
    public Optional<Boolean> isRedundant() {
        return Optional.ofNullable(redundant);
    }

    @Override
    public ObservabilityQuality<T> setRedundant(Boolean redundant) {
        this.redundant = redundant;
        this.setter.accept(this);
        return this;
    }
}
