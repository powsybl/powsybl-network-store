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
import com.powsybl.network.store.iidm.impl.AbstractInjectionImpl;
import com.powsybl.network.store.model.InjectionObservabilityAttributes;
import com.powsybl.network.store.model.ObservabilityQualityAttributes;

/**
  * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class InjectionObservabilityImpl<I extends Injection<I>> extends AbstractExtension<I> implements InjectionObservability<I> {

    public InjectionObservabilityImpl(I injection) {
        super(injection);
    }

    private AbstractInjectionImpl<?, ?> getInjection() {
        return (AbstractInjectionImpl<?, ?>) getExtendable();
    }

    private InjectionObservabilityAttributes getInjectionObservabilityAttributes() {
        return (InjectionObservabilityAttributes) getInjection().getResource().getAttributes().getExtensionAttributes().get(InjectionObservability.NAME);
    }

    public boolean isObservable() {
        return getInjectionObservabilityAttributes().isObservable();
    }

    @Override
    public InjectionObservability<I> setObservable(boolean observable) {
        getInjection().updateResource(res -> ((InjectionObservabilityAttributes) res.getAttributes().getExtensionAttributes().get(InjectionObservability.NAME)).setObservable(observable));
        return this;
    }

    @Override
    public ObservabilityQuality<I> getQualityP() {
        InjectionObservabilityAttributes injectionObservabilityAttributes = (InjectionObservabilityAttributes) getInjection().getResource().getAttributes().getExtensionAttributes().get(InjectionObservability.NAME);
        if (injectionObservabilityAttributes.getQualityP() != null) {
            return new ObservabilityQualityImpl<>(quality -> setQualityP(quality.getStandardDeviation(), quality.isRedundant().orElse(null)),
                injectionObservabilityAttributes.getQualityP().getStandardDeviation(),
                injectionObservabilityAttributes.getQualityP().getRedundant());
        } else {
            return null;
        }
    }

    @Override
    public InjectionObservability<I> setQualityP(double standardDeviation, Boolean redundant) {
        getInjection().updateResource(res -> {
            InjectionObservabilityAttributes injectionObservabilityAttributes = (InjectionObservabilityAttributes) res.getAttributes().getExtensionAttributes().get(InjectionObservability.NAME);
            injectionObservabilityAttributes.setQualityP(ObservabilityQualityAttributes.builder()
                .standardDeviation(standardDeviation)
                .redundant(redundant)
                .build());
        });
        return this;
    }

    @Override
    public InjectionObservability<I> setQualityP(double standardDeviation) {
        getInjection().updateResource(res -> {
            InjectionObservabilityAttributes injectionObservabilityAttributes = (InjectionObservabilityAttributes) getInjection().getResource().getAttributes().getExtensionAttributes().get(InjectionObservability.NAME);
            if (injectionObservabilityAttributes.getQualityP() == null) {
                injectionObservabilityAttributes.setQualityP(ObservabilityQualityAttributes.builder()
                    .standardDeviation(standardDeviation)
                    .build());
            } else {
                injectionObservabilityAttributes.setQualityP(ObservabilityQualityAttributes.builder()
                    .standardDeviation(standardDeviation)
                    .redundant(injectionObservabilityAttributes.getQualityP().getRedundant())
                    .build());
            }
        });
        return this;
    }

    @Override
    public ObservabilityQuality<I> getQualityQ() {
        InjectionObservabilityAttributes injectionObservabilityAttributes = (InjectionObservabilityAttributes) getInjection().getResource().getAttributes().getExtensionAttributes().get(InjectionObservability.NAME);
        if (injectionObservabilityAttributes.getQualityQ() != null) {
            return new ObservabilityQualityImpl<>(quality -> setQualityQ(quality.getStandardDeviation(), quality.isRedundant().orElse(null)),
                injectionObservabilityAttributes.getQualityQ().getStandardDeviation(),
                injectionObservabilityAttributes.getQualityQ().getRedundant());
        } else {
            return null;
        }
    }

    @Override
    public InjectionObservability<I> setQualityQ(double standardDeviation, Boolean redundant) {
        getInjection().updateResource(res -> {
            InjectionObservabilityAttributes injectionObservabilityAttributes = (InjectionObservabilityAttributes) res.getAttributes().getExtensionAttributes().get(InjectionObservability.NAME);
            injectionObservabilityAttributes.setQualityQ(ObservabilityQualityAttributes.builder()
                .standardDeviation(standardDeviation)
                .redundant(redundant)
                .build());
        });
        return this;
    }

    @Override
    public InjectionObservability<I> setQualityQ(double standardDeviation) {
        getInjection().updateResource(res -> {
            InjectionObservabilityAttributes injectionObservabilityAttributes = (InjectionObservabilityAttributes) getInjection().getResource().getAttributes().getExtensionAttributes().get(InjectionObservability.NAME);
            if (injectionObservabilityAttributes.getQualityQ() == null) {
                injectionObservabilityAttributes.setQualityQ(ObservabilityQualityAttributes.builder()
                    .standardDeviation(standardDeviation)
                    .build());
            } else {
                injectionObservabilityAttributes.setQualityQ(ObservabilityQualityAttributes.builder()
                    .standardDeviation(standardDeviation)
                    .redundant(injectionObservabilityAttributes.getQualityQ().getRedundant())
                    .build());
            }
        });
        return this;
    }

    @Override
    public ObservabilityQuality<I> getQualityV() {
        InjectionObservabilityAttributes injectionObservabilityAttributes = (InjectionObservabilityAttributes) getInjection().getResource().getAttributes().getExtensionAttributes().get(InjectionObservability.NAME);
        if (injectionObservabilityAttributes.getQualityV() != null) {
            return new ObservabilityQualityImpl<>(quality -> setQualityV(quality.getStandardDeviation(), quality.isRedundant().orElse(null)),
                injectionObservabilityAttributes.getQualityV().getStandardDeviation(),
                injectionObservabilityAttributes.getQualityV().getRedundant());
        } else {
            return null;
        }
    }

    @Override
    public InjectionObservability<I> setQualityV(double standardDeviation, Boolean redundant) {
        getInjection().updateResource(res -> {
            InjectionObservabilityAttributes injectionObservabilityAttributes = (InjectionObservabilityAttributes) res.getAttributes().getExtensionAttributes().get(InjectionObservability.NAME);
            injectionObservabilityAttributes.setQualityV(ObservabilityQualityAttributes.builder()
                .standardDeviation(standardDeviation)
                .redundant(redundant)
                .build());
        });
        return this;
    }

    @Override
    public InjectionObservability<I> setQualityV(double standardDeviation) {
        getInjection().updateResource(res -> {
            InjectionObservabilityAttributes injectionObservabilityAttributes = (InjectionObservabilityAttributes) getInjection().getResource().getAttributes().getExtensionAttributes().get(InjectionObservability.NAME);
            if (injectionObservabilityAttributes.getQualityV() == null) {
                injectionObservabilityAttributes.setQualityV(ObservabilityQualityAttributes.builder()
                    .standardDeviation(standardDeviation)
                    .build());
            } else {
                injectionObservabilityAttributes.setQualityV(ObservabilityQualityAttributes.builder()
                    .standardDeviation(standardDeviation)
                    .redundant(injectionObservabilityAttributes.getQualityV().getRedundant())
                    .build());
            }
        });
        return this;
    }
}
