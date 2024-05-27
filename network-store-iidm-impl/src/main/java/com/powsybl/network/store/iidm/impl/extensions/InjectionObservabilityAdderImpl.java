/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.InjectionObservability;
import com.powsybl.iidm.network.extensions.InjectionObservabilityAdder;
import com.powsybl.network.store.iidm.impl.AbstractInjectionImpl;
import com.powsybl.network.store.model.InjectionObservabilityAttributes;
import com.powsybl.network.store.model.ObservabilityQualityAttributes;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class InjectionObservabilityAdderImpl<I extends Injection<I>>
        extends AbstractExtensionAdder<I, InjectionObservability<I>>
        implements InjectionObservabilityAdder<I> {

    private boolean observable = false;

    private double standardDeviationP = Double.NaN;

    private double standardDeviationQ = Double.NaN;

    private double standardDeviationV = Double.NaN;

    private Boolean redundantP;

    private Boolean redundantQ;

    private Boolean redundantV;

    public InjectionObservabilityAdderImpl(I extendable) {
        super(extendable);
    }

    @Override
    protected InjectionObservability<I> createExtension(I injection) {
        InjectionObservabilityAttributes attributes = InjectionObservabilityAttributes.builder()
            .observable(observable)
            .qualityP(!Double.isNaN(standardDeviationP) ? ObservabilityQualityAttributes.builder()
                .standardDeviation(standardDeviationP)
                .redundant(redundantP)
                .build() : null)
            .qualityQ(!Double.isNaN(standardDeviationQ) ? ObservabilityQualityAttributes.builder()
                .standardDeviation(standardDeviationQ)
                .redundant(redundantQ)
                .build() : null)
            .qualityV(!Double.isNaN(standardDeviationV) ? ObservabilityQualityAttributes.builder()
                .standardDeviation(standardDeviationV)
                .redundant(redundantV)
                .build() : null)
            .build();
        ((AbstractInjectionImpl<?, ?>) extendable).updateResource(res -> res.getAttributes().getExtensionAttributes().put(InjectionObservability.NAME, attributes));
        return new InjectionObservabilityImpl<>(injection);
    }

    @Override
    public InjectionObservabilityAdder<I> withObservable(boolean observable) {
        this.observable = observable;
        return this;
    }

    @Override
    public InjectionObservabilityAdder<I> withStandardDeviationP(double standardDeviationP) {
        this.standardDeviationP = standardDeviationP;
        return this;
    }

    @Override
    public InjectionObservabilityAdder<I> withRedundantP(Boolean redundant) {
        this.redundantP = redundant;
        return this;
    }

    @Override
    public InjectionObservabilityAdder<I> withStandardDeviationQ(double standardDeviationQ) {
        this.standardDeviationQ = standardDeviationQ;
        return this;
    }

    @Override
    public InjectionObservabilityAdder<I> withRedundantQ(Boolean redundant) {
        this.redundantQ = redundant;
        return this;
    }

    @Override
    public InjectionObservabilityAdder<I> withStandardDeviationV(double standardDeviationV) {
        this.standardDeviationV = standardDeviationV;
        return this;
    }

    @Override
    public InjectionObservabilityAdder<I> withRedundantV(Boolean redundant) {
        this.redundantV = redundant;
        return this;
    }
}
