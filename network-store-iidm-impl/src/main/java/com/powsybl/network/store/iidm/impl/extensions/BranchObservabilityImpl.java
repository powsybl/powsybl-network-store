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

    public boolean isObservable() {
        return getBranchObservabilityAttributes().isObservable();
    }

    @Override
    public BranchObservability<B> setObservable(boolean observable) {
        getBranch().updateResource(res -> ((BranchObservabilityAttributes) res.getAttributes().getExtensionAttributes().get(BranchObservability.NAME)).setObservable(observable));
        return this;
    }

    @Override
    public ObservabilityQuality<B> getQualityP1() {
        BranchObservabilityAttributes branchObservabilityAttributes = (BranchObservabilityAttributes) getBranch().getResource().getAttributes().getExtensionAttributes().get(BranchObservability.NAME);
        if (branchObservabilityAttributes.getQualityP1() != null) {
            return new ObservabilityQualityImpl<>(quality -> setQualityP1(quality.getStandardDeviation(), quality.isRedundant().orElse(null)),
                branchObservabilityAttributes.getQualityP1().getStandardDeviation(),
                branchObservabilityAttributes.getQualityP1().getRedundant());
        } else {
            return null;
        }
    }

    @Override
    public BranchObservability<B> setQualityP1(double standardDeviation, Boolean redundant) {
        getBranch().updateResource(res -> {
            BranchObservabilityAttributes branchObservabilityAttributes = (BranchObservabilityAttributes) res.getAttributes().getExtensionAttributes().get(BranchObservability.NAME);
            branchObservabilityAttributes.setQualityP1(ObservabilityQualityAttributes.builder()
                .standardDeviation(standardDeviation)
                .redundant(redundant)
                .build());
        });
        return this;
    }

    @Override
    public BranchObservability<B> setQualityP1(double standardDeviation) {
        getBranch().updateResource(res -> {
            BranchObservabilityAttributes branchObservabilityAttributes = (BranchObservabilityAttributes) getBranch().getResource().getAttributes().getExtensionAttributes().get(BranchObservability.NAME);
            if (branchObservabilityAttributes.getQualityP1() == null) {
                branchObservabilityAttributes.setQualityP1(ObservabilityQualityAttributes.builder()
                    .standardDeviation(standardDeviation)
                    .build());
            } else {
                branchObservabilityAttributes.setQualityP1(ObservabilityQualityAttributes.builder()
                    .standardDeviation(standardDeviation)
                    .redundant(branchObservabilityAttributes.getQualityP1().getRedundant())
                    .build());
            }
        });
        return this;
    }

    @Override
    public ObservabilityQuality<B> getQualityP2() {
        BranchObservabilityAttributes branchObservabilityAttributes = (BranchObservabilityAttributes) getBranch().getResource().getAttributes().getExtensionAttributes().get(BranchObservability.NAME);
        if (branchObservabilityAttributes.getQualityP2() != null) {
            return new ObservabilityQualityImpl<>(quality -> setQualityP2(quality.getStandardDeviation(), quality.isRedundant().orElse(null)),
                branchObservabilityAttributes.getQualityP2().getStandardDeviation(),
                branchObservabilityAttributes.getQualityP2().getRedundant());
        } else {
            return null;
        }
    }

    @Override
    public BranchObservability<B> setQualityP2(double standardDeviation, Boolean redundant) {
        getBranch().updateResource(res -> {
            BranchObservabilityAttributes branchObservabilityAttributes = (BranchObservabilityAttributes) res.getAttributes().getExtensionAttributes().get(BranchObservability.NAME);
            branchObservabilityAttributes.setQualityP2(ObservabilityQualityAttributes.builder()
                .standardDeviation(standardDeviation)
                .redundant(redundant)
                .build());
        });
        return this;
    }

    @Override
    public BranchObservability<B> setQualityP2(double standardDeviation) {
        getBranch().updateResource(res -> {
            BranchObservabilityAttributes branchObservabilityAttributes = (BranchObservabilityAttributes) getBranch().getResource().getAttributes().getExtensionAttributes().get(BranchObservability.NAME);
            if (branchObservabilityAttributes.getQualityP2() == null) {
                branchObservabilityAttributes.setQualityP2(ObservabilityQualityAttributes.builder()
                    .standardDeviation(standardDeviation)
                    .build());
            } else {
                branchObservabilityAttributes.setQualityP2(ObservabilityQualityAttributes.builder()
                    .standardDeviation(standardDeviation)
                    .redundant(branchObservabilityAttributes.getQualityP2().getRedundant())
                    .build());
            }
        });
        return this;
    }

    @Override
    public ObservabilityQuality<B> getQualityQ1() {
        BranchObservabilityAttributes branchObservabilityAttributes = (BranchObservabilityAttributes) getBranch().getResource().getAttributes().getExtensionAttributes().get(BranchObservability.NAME);
        if (branchObservabilityAttributes.getQualityQ1() != null) {
            return new ObservabilityQualityImpl<>(quality -> setQualityQ1(quality.getStandardDeviation(), quality.isRedundant().orElse(null)),
                branchObservabilityAttributes.getQualityQ1().getStandardDeviation(),
                branchObservabilityAttributes.getQualityQ1().getRedundant());
        } else {
            return null;
        }
    }

    @Override
    public BranchObservability<B> setQualityQ1(double standardDeviation, Boolean redundant) {
        getBranch().updateResource(res -> {
            BranchObservabilityAttributes branchObservabilityAttributes = (BranchObservabilityAttributes) res.getAttributes().getExtensionAttributes().get(BranchObservability.NAME);
            branchObservabilityAttributes.setQualityQ1(ObservabilityQualityAttributes.builder()
                .standardDeviation(standardDeviation)
                .redundant(redundant)
                .build());
        });
        return this;
    }

    @Override
    public BranchObservability<B> setQualityQ1(double standardDeviation) {
        getBranch().updateResource(res -> {
            BranchObservabilityAttributes branchObservabilityAttributes = (BranchObservabilityAttributes) getBranch().getResource().getAttributes().getExtensionAttributes().get(BranchObservability.NAME);
            if (branchObservabilityAttributes.getQualityQ1() == null) {
                branchObservabilityAttributes.setQualityQ1(ObservabilityQualityAttributes.builder()
                    .standardDeviation(standardDeviation)
                    .build());
            } else {
                branchObservabilityAttributes.setQualityQ1(ObservabilityQualityAttributes.builder()
                    .standardDeviation(standardDeviation)
                    .redundant(branchObservabilityAttributes.getQualityQ1().getRedundant())
                    .build());
            }
        });
        return this;
    }

    @Override
    public ObservabilityQuality<B> getQualityQ2() {
        BranchObservabilityAttributes branchObservabilityAttributes = (BranchObservabilityAttributes) getBranch().getResource().getAttributes().getExtensionAttributes().get(BranchObservability.NAME);
        if (branchObservabilityAttributes.getQualityQ2() != null) {
            return new ObservabilityQualityImpl<>(quality -> setQualityQ2(quality.getStandardDeviation(), quality.isRedundant().orElse(null)),
                branchObservabilityAttributes.getQualityQ2().getStandardDeviation(),
                branchObservabilityAttributes.getQualityQ2().getRedundant());
        } else {
            return null;
        }
    }

    @Override
    public BranchObservability<B> setQualityQ2(double standardDeviation, Boolean redundant) {
        getBranch().updateResource(res -> {
            BranchObservabilityAttributes branchObservabilityAttributes = (BranchObservabilityAttributes) res.getAttributes().getExtensionAttributes().get(BranchObservability.NAME);
            branchObservabilityAttributes.setQualityQ2(ObservabilityQualityAttributes.builder()
                .standardDeviation(standardDeviation)
                .redundant(redundant)
                .build());
        });
        return this;
    }

    @Override
    public BranchObservability<B> setQualityQ2(double standardDeviation) {
        getBranch().updateResource(res -> {
            BranchObservabilityAttributes branchObservabilityAttributes = (BranchObservabilityAttributes) getBranch().getResource().getAttributes().getExtensionAttributes().get(BranchObservability.NAME);
            if (branchObservabilityAttributes.getQualityQ2() == null) {
                branchObservabilityAttributes.setQualityQ2(ObservabilityQualityAttributes.builder()
                    .standardDeviation(standardDeviation)
                    .build());
            } else {
                branchObservabilityAttributes.setQualityQ2(ObservabilityQualityAttributes.builder()
                    .standardDeviation(standardDeviation)
                    .redundant(branchObservabilityAttributes.getQualityQ2().getRedundant())
                    .build());
            }
        });
        return this;
    }
}
