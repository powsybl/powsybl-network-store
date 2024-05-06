/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.extensions.CgmesMetadataModels;
import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.iidm.impl.NetworkImpl;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Etienne HOMER {@literal <etienne.homer at rte-france.com>}
 */
public class CgmesMetadataModelsImpl extends AbstractExtension<Network> implements CgmesMetadataModels {

    public CgmesMetadataModelsImpl(Network network) {
        super(network);
    }

    private NetworkImpl getNetwork() {
        return (NetworkImpl) getExtendable();
    }

    @Override
    public Collection<CgmesMetadataModel> getModels() {
        return getNetwork().getResource().getAttributes().getCgmesMetadataModels().getModels().stream().map(m -> {
                CgmesMetadataModel model = new CgmesMetadataModel(m.getSubset(), m.getModelingAuthoritySet());
                model.setId(m.getId());
                model.setDescription(m.getDescription());
                model.setVersion(m.getVersion());
                model.addProfiles(m.getProfiles());
                model.addDependentOn(m.getDependentOn());
                model.addSupersedes(m.getSupersedes());
                return model;
            }
        ).collect(Collectors.toList());
    }

    @Override
    public List<CgmesMetadataModel> getSortedModels() {
        return getModels().stream().sorted(
                Comparator.comparing(CgmesMetadataModel::getModelingAuthoritySet)
                        .thenComparing(CgmesMetadataModel::getSubset)
                        .thenComparing(CgmesMetadataModel::getVersion)
                        .thenComparing(CgmesMetadataModel::getId)
        ).toList();
    }

    @Override
    public Optional<CgmesMetadataModel> getModelForSubset(CgmesSubset cgmesSubset) {
        return getModels().stream()
            .filter(m -> m.getSubset().equals(cgmesSubset))
            .findFirst();
    }

    @Override
    public Optional<CgmesMetadataModel> getModelForSubsetModelingAuthoritySet(CgmesSubset cgmesSubset, String s) {
        return getModels().stream()
                .filter(m -> m.getSubset().equals(cgmesSubset) && m.getModelingAuthoritySet().equals(s))
                .findFirst();
    }
}

