/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

/**
 * @author Etienne HOMER {@literal <etienne.homer at rte-france.com>}
 */
//public class CgmesMetadataModelsImpl extends AbstractExtension<Network> implements CgmesMetadataModels {
//
//    public CgmesMetadataModelsImpl(NetworkImpl network) {
//        super(network);
//    }
//
//    private NetworkImpl getNetwork() {
//        return (NetworkImpl) getExtendable();
//    }
//
//    @Override
//    public Collection<CgmesMetadataModel> getModels() {
//        return getNetwork().getResource().getAttributes().getCgmesMetadataModels().getModels();
//    }
//
//    @Override
//    public List<CgmesMetadataModel> getSortedModels() {
//        return getModels().stream().sorted(
//                Comparator.comparing(CgmesMetadataModel::getModelingAuthoritySet)
//                        .thenComparing(CgmesMetadataModel::getSubset)
//                        .thenComparing(CgmesMetadataModel::getVersion)
//                        .thenComparing(CgmesMetadataModel::getId)
//        ).toList();
//    }
//
//    @Override
//    public Optional<CgmesMetadataModel> getModelForSubset(CgmesSubset cgmesSubset) {
//        return Optional.ofNullable(getNetwork().getResource().getAttributes().getCgmesMetadataModels().getSubsetModel().get(cgmesSubset));
//    }
//
//    @Override
//    public Optional<CgmesMetadataModel> getModelForSubsetModelingAuthoritySet(CgmesSubset cgmesSubset, String s) {
//        return getModels().stream()
//                .filter(m -> m.getSubset().equals(cgmesSubset) && m.getModelingAuthoritySet().equals(s))
//                .findFirst();
//    }
//}

public class CgmesMetadataModelsImpl {
}
