/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.extensions.BranchObservability;
import com.powsybl.iidm.network.extensions.ObservabilityQuality;
import com.powsybl.network.store.iidm.impl.AbstractBranchImpl;
import com.powsybl.network.store.model.BranchObservabilityAttributes;
import com.powsybl.network.store.model.ObservabilityQualityAttributes;
import lombok.NonNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class BranchObservabilityImpl<B extends Branch<B>> extends AbstractExtension<B> implements BranchObservability<B> {

    public BranchObservabilityImpl(B branch) {
        super(branch);
    }

    private AbstractBranchImpl<?, ?> getBranch() {
        return (AbstractBranchImpl<?, ?>) getExtendable();
    }

    private BranchObservabilityAttributes getBranchObservabilityAttributes() {
        return (BranchObservabilityAttributes) getBranch().getResource().getAttributes().getExtensionAttributes().get(BranchObservability.NAME);
    }

    @Override
    public boolean isObservable() {
        return getBranchObservabilityAttributes().isObservable();
    }

    @Override
    public BranchObservability<B> setObservable(boolean observable) {
        boolean oldValue = isObservable();
        if (oldValue != observable) {
            getBranch().updateResourceExtension(this, res -> getBranchObservabilityAttributes().setObservable(observable), "observable", oldValue, observable);
        }
        return this;
    }

    private ObservabilityQuality<B> getQuality(final @NonNull Function<BranchObservabilityAttributes,
                                               ObservabilityQualityAttributes> getterQuality,
                                               final @NonNull BiConsumer<Double, Boolean> setterQuality) {
        final ObservabilityQualityAttributes qualityP = getterQuality.apply(getBranchObservabilityAttributes());
        if (qualityP != null) {
            return new ObservabilityQualityImpl<>(
                quality -> setterQuality.accept(quality.getStandardDeviation(), quality.isRedundant().orElse(null)),
                qualityP.getStandardDeviation(),
                qualityP.getRedundant());
        } else {
            return null;
        }
    }

    @Override
    public ObservabilityQuality<B> getQualityP1() {
        return getQuality(BranchObservabilityAttributes::getQualityP1, this::setQualityP1);
    }

    @Override
    public ObservabilityQuality<B> getQualityP2() {
        return getQuality(BranchObservabilityAttributes::getQualityP2, this::setQualityP2);
    }

    @Override
    public ObservabilityQuality<B> getQualityQ1() {
        return getQuality(BranchObservabilityAttributes::getQualityQ1, this::setQualityQ1);
    }

    @Override
    public ObservabilityQuality<B> getQualityQ2() {
        return getQuality(BranchObservabilityAttributes::getQualityQ2, this::setQualityQ2);
    }

    private BranchObservability<B> setQuality(final double standardDeviation, final Boolean redundant,
                                              final @NonNull BiConsumer<BranchObservabilityAttributes, ObservabilityQualityAttributes> setterQuality,
                                              final @NonNull Function<BranchObservabilityAttributes, ObservabilityQualityAttributes> getterQuality,
                                              String suffix) {
        ObservabilityQualityAttributes oldValue = getterQuality.apply(getBranchObservabilityAttributes());
        ObservabilityQualityAttributes qualityAttributes = ObservabilityQualityAttributes.builder().standardDeviation(standardDeviation).redundant(redundant).build();
        getBranch().updateResourceExtension(this, res -> setterQuality.accept(getBranchObservabilityAttributes(), qualityAttributes), "quality" + suffix, oldValue, qualityAttributes);
        return this;
    }

    @Override
    public BranchObservability<B> setQualityP1(final double standardDeviation, final Boolean redundant) {
        return setQuality(standardDeviation, redundant, BranchObservabilityAttributes::setQualityP1, BranchObservabilityAttributes::getQualityP1, "P1");
    }

    @Override
    public BranchObservability<B> setQualityP2(final double standardDeviation, final Boolean redundant) {
        return setQuality(standardDeviation, redundant, BranchObservabilityAttributes::setQualityP2, BranchObservabilityAttributes::getQualityP2, "P2");
    }

    @Override
    public BranchObservability<B> setQualityQ1(final double standardDeviation, final Boolean redundant) {
        return setQuality(standardDeviation, redundant, BranchObservabilityAttributes::setQualityQ1, BranchObservabilityAttributes::getQualityQ1, "Q1");
    }

    @Override
    public BranchObservability<B> setQualityQ2(final double standardDeviation, final Boolean redundant) {
        return setQuality(standardDeviation, redundant, BranchObservabilityAttributes::setQualityQ2, BranchObservabilityAttributes::getQualityQ2, "Q2");
    }

    private BranchObservability<B> setQuality(final double standardDeviation,
                                              final @NonNull Function<BranchObservabilityAttributes,
                                              ObservabilityQualityAttributes> getterQuality,
                                              final @NonNull BiConsumer<BranchObservabilityAttributes,
                                              ObservabilityQualityAttributes> setterQuality,
                                              String suffix) {
        ObservabilityQualityAttributes oldValue = getterQuality.apply(getBranchObservabilityAttributes());
        ObservabilityQualityAttributes.ObservabilityQualityAttributesBuilder builder = ObservabilityQualityAttributes.builder().standardDeviation(standardDeviation);
        if (oldValue != null) {
            builder = builder.redundant(oldValue.getRedundant());
        }
        ObservabilityQualityAttributes qualityAttributes = builder.build();
        getBranch().updateResourceExtension(this, res -> setterQuality.accept(getBranchObservabilityAttributes(), qualityAttributes), "quality" + suffix, oldValue, qualityAttributes);
        return this;
    }

    @Override
    public BranchObservability<B> setQualityP1(double standardDeviation) {
        return setQuality(standardDeviation, BranchObservabilityAttributes::getQualityP1, BranchObservabilityAttributes::setQualityP1, "P1");
    }

    @Override
    public BranchObservability<B> setQualityP2(double standardDeviation) {
        return setQuality(standardDeviation, BranchObservabilityAttributes::getQualityP2, BranchObservabilityAttributes::setQualityP2, "P2");
    }

    @Override
    public BranchObservability<B> setQualityQ1(double standardDeviation) {
        return setQuality(standardDeviation, BranchObservabilityAttributes::getQualityQ1, BranchObservabilityAttributes::setQualityQ1, "Q1");
    }

    @Override
    public BranchObservability<B> setQualityQ2(double standardDeviation) {
        return setQuality(standardDeviation, BranchObservabilityAttributes::getQualityQ2, BranchObservabilityAttributes::setQualityQ2, "Q2");
    }
}
