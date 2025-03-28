/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.extensions.BranchObservability;
import com.powsybl.iidm.network.extensions.BranchObservabilityAdder;
import com.powsybl.network.store.iidm.impl.AbstractBranchImpl;
import com.powsybl.network.store.model.BranchObservabilityAttributes;
import com.powsybl.network.store.model.ObservabilityQualityAttributes;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class BranchObservabilityAdderImpl<B extends Branch<B>>
        extends AbstractIidmExtensionAdder<B, BranchObservability<B>>
        implements BranchObservabilityAdder<B> {

    private boolean observable = false;

    private double standardDeviationP1 = Double.NaN;

    private double standardDeviationP2 = Double.NaN;

    private double standardDeviationQ1 = Double.NaN;

    private double standardDeviationQ2 = Double.NaN;

    private Boolean redundantP1;

    private Boolean redundantP2;

    private Boolean redundantQ1;

    private Boolean redundantQ2;

    public BranchObservabilityAdderImpl(B extendable) {
        super(extendable);
    }

    @Override
    protected BranchObservability<B> createExtension(B branch) {
        var oldValue = ((AbstractBranchImpl<?, ?>) branch).getResource().getAttributes().getExtensionAttributes().get(BranchObservability.NAME);
        BranchObservabilityAttributes attributes = BranchObservabilityAttributes.builder()
            .observable(observable)
            .qualityP1(!Double.isNaN(standardDeviationP1) ? ObservabilityQualityAttributes.builder()
                .standardDeviation(standardDeviationP1)
                .redundant(redundantP1)
                .build() : null)
            .qualityP2(!Double.isNaN(standardDeviationP2) ? ObservabilityQualityAttributes.builder()
                .standardDeviation(standardDeviationP2)
                .redundant(redundantP2)
                .build() : null)
            .qualityQ1(!Double.isNaN(standardDeviationQ1) ? ObservabilityQualityAttributes.builder()
                .standardDeviation(standardDeviationQ1)
                .redundant(redundantQ1)
                .build() : null)
            .qualityQ2(!Double.isNaN(standardDeviationQ2) ? ObservabilityQualityAttributes.builder()
                .standardDeviation(standardDeviationQ2)
                .redundant(redundantQ2)
                .build() : null)
            .build();
        ((AbstractBranchImpl<?, ?>) branch).updateResource(res -> res.getAttributes().getExtensionAttributes().put(BranchObservability.NAME, attributes),
            "branchObservability", oldValue, attributes);
        return new BranchObservabilityImpl<>(branch);
    }

    @Override
    public BranchObservabilityAdder<B> withObservable(boolean observable) {
        this.observable = observable;
        return this;
    }

    @Override
    public BranchObservabilityAdder<B> withStandardDeviationP1(double standardDeviationP1) {
        this.standardDeviationP1 = standardDeviationP1;
        return this;
    }

    @Override
    public BranchObservabilityAdder<B> withStandardDeviationP2(double standardDeviationP2) {
        this.standardDeviationP2 = standardDeviationP2;
        return this;
    }

    @Override
    public BranchObservabilityAdder<B> withRedundantP1(Boolean redundantP1) {
        this.redundantP1 = redundantP1;
        return this;
    }

    @Override
    public BranchObservabilityAdder<B> withRedundantP2(Boolean redundantP2) {
        this.redundantP2 = redundantP2;
        return this;
    }

    @Override
    public BranchObservabilityAdder<B> withStandardDeviationQ1(double standardDeviationQ1) {
        this.standardDeviationQ1 = standardDeviationQ1;
        return this;
    }

    @Override
    public BranchObservabilityAdder<B> withStandardDeviationQ2(double standardDeviationQ2) {
        this.standardDeviationQ2 = standardDeviationQ2;
        return this;
    }

    @Override
    public BranchObservabilityAdder<B> withRedundantQ1(Boolean redundantQ1) {
        this.redundantQ1 = redundantQ1;
        return this;
    }

    @Override
    public BranchObservabilityAdder<B> withRedundantQ2(Boolean redundantQ2) {
        this.redundantQ2 = redundantQ2;
        return this;
    }
}
