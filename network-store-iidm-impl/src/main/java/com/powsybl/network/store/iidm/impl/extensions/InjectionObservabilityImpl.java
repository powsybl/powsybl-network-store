/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Injection;
import com.powsybl.iidm.network.extensions.InjectionObservability;
import com.powsybl.iidm.network.extensions.ObservabilityQuality;
import com.powsybl.network.store.iidm.impl.AbstractIdentifiableImpl;
import com.powsybl.network.store.model.InjectionObservabilityAttributes;
import com.powsybl.network.store.model.ObservabilityQualityAttributes;
import lombok.NonNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
  * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class InjectionObservabilityImpl<I extends Injection<I>> extends AbstractExtension<I> implements InjectionObservability<I> {

    public InjectionObservabilityImpl(I injection) {
        super(injection);
    }

    private AbstractIdentifiableImpl<?, ?> getInjection() {
        return (AbstractIdentifiableImpl<?, ?>) getExtendable();
    }

    private InjectionObservabilityAttributes getInjectionObservabilityAttributes() {
        return (InjectionObservabilityAttributes) getInjection().getResource().getAttributes().getExtensionAttributes().get(InjectionObservability.NAME);
    }

    @Override
    public boolean isObservable() {
        return getInjectionObservabilityAttributes().isObservable();
    }

    @Override
    public InjectionObservability<I> setObservable(boolean observable) {
        boolean oldValue = isObservable();
        if (oldValue != observable) {
            getInjection().updateResourceExtension(this, res -> getInjectionObservabilityAttributes().setObservable(observable), "observable", oldValue, observable);
        }
        return this;
    }

    private ObservabilityQuality<I> getQuality(final @NonNull Function<InjectionObservabilityAttributes,
                                               ObservabilityQualityAttributes> getterQuality,
                                               final @NonNull BiConsumer<Double, Boolean> setterQuality) {
        final ObservabilityQualityAttributes qualityAttr = getterQuality.apply(getInjectionObservabilityAttributes());
        if (qualityAttr != null) {
            return new ObservabilityQualityImpl<>(
                quality -> setterQuality.accept(quality.getStandardDeviation(), quality.isRedundant().orElse(null)),
                qualityAttr.getStandardDeviation(),
                qualityAttr.getRedundant());
        } else {
            return null;
        }
    }

    @Override
    public ObservabilityQuality<I> getQualityP() {
        return getQuality(InjectionObservabilityAttributes::getQualityP, this::setQualityP);
    }

    @Override
    public ObservabilityQuality<I> getQualityQ() {
        return getQuality(InjectionObservabilityAttributes::getQualityQ, this::setQualityQ);
    }

    @Override
    public ObservabilityQuality<I> getQualityV() {
        return getQuality(InjectionObservabilityAttributes::getQualityV, this::setQualityV);
    }

    private InjectionObservability<I> setQuality(final double standardDeviation, final Boolean redundant,
                                                 final @NonNull BiConsumer<InjectionObservabilityAttributes,
                                                 ObservabilityQualityAttributes> setterQuality,
                                                 final @NonNull Function<InjectionObservabilityAttributes, ObservabilityQualityAttributes> getterQuality,
                                                 String suffix) {
        ObservabilityQualityAttributes oldValue = getterQuality.apply(getInjectionObservabilityAttributes());
        ObservabilityQualityAttributes qualityAttributes = ObservabilityQualityAttributes.builder().standardDeviation(standardDeviation).redundant(redundant).build();
        getInjection().updateResourceExtension(this, res -> setterQuality.accept(getInjectionObservabilityAttributes(), qualityAttributes), "quality" + suffix, oldValue, qualityAttributes);
        return this;
    }

    @Override
    public InjectionObservability<I> setQualityP(final double standardDeviation, final Boolean redundant) {
        return setQuality(standardDeviation, redundant, InjectionObservabilityAttributes::setQualityP, InjectionObservabilityAttributes::getQualityP, "P");
    }

    @Override
    public InjectionObservability<I> setQualityQ(final double standardDeviation, final Boolean redundant) {
        return setQuality(standardDeviation, redundant, InjectionObservabilityAttributes::setQualityQ, InjectionObservabilityAttributes::getQualityQ, "Q");
    }

    @Override
    public InjectionObservability<I> setQualityV(final double standardDeviation, final Boolean redundant) {
        return setQuality(standardDeviation, redundant, InjectionObservabilityAttributes::setQualityV, InjectionObservabilityAttributes::getQualityV, "V");
    }

    private InjectionObservability<I> setQuality(final double standardDeviation,
                                                 final @NonNull Function<InjectionObservabilityAttributes,
                                                 ObservabilityQualityAttributes> getterQuality,
                                                 final @NonNull BiConsumer<InjectionObservabilityAttributes,
                                                 ObservabilityQualityAttributes> setterQuality,
                                                 String suffix) {
        ObservabilityQualityAttributes oldValue = getterQuality.apply(getInjectionObservabilityAttributes());
        ObservabilityQualityAttributes.ObservabilityQualityAttributesBuilder builder = ObservabilityQualityAttributes.builder().standardDeviation(standardDeviation);
        if (oldValue != null) {
            builder = builder.redundant(oldValue.getRedundant());
        }
        ObservabilityQualityAttributes qualityAttributes = builder.build();
        getInjection().updateResourceExtension(this, res -> setterQuality.accept(getInjectionObservabilityAttributes(), qualityAttributes), "quality" + suffix, oldValue, qualityAttributes);
        return this;
    }

    @Override
    public InjectionObservability<I> setQualityP(final double standardDeviation) {
        return setQuality(standardDeviation, InjectionObservabilityAttributes::getQualityP, InjectionObservabilityAttributes::setQualityP, "P");
    }

    @Override
    public InjectionObservability<I> setQualityQ(final double standardDeviation) {
        return setQuality(standardDeviation, InjectionObservabilityAttributes::getQualityQ, InjectionObservabilityAttributes::setQualityQ, "Q");
    }

    @Override
    public InjectionObservability<I> setQualityV(final double standardDeviation) {
        return setQuality(standardDeviation, InjectionObservabilityAttributes::getQualityV, InjectionObservabilityAttributes::setQualityV, "V");
    }
}
