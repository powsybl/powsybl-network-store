/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.extensions.CgmesMetadataModels;
import com.powsybl.cgmes.extensions.CgmesMetadataModelsAdder;
import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.iidm.impl.NetworkImpl;
import com.powsybl.network.store.model.CgmesMetadataModelAttributes;
import com.powsybl.network.store.model.CgmesMetadataModelsAttributes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Etienne HOMER {@literal <etienne.homer at rte-france.com>}
 */
public class CgmesMetadataModelsAdderImpl extends AbstractExtensionAdder<Network, CgmesMetadataModels> implements CgmesMetadataModelsAdder {

    class ModelAdderImpl implements ModelAdder {
        private CgmesSubset subset;
        private String id;
        private String description;
        private int version = 0;
        private String modelingAuthoritySet;
        private final Set<String> profiles = new HashSet<>();
        private final Set<String> dependentOn = new HashSet<>();
        private final Set<String> supersedes = new HashSet<>();

        @Override
        public ModelAdder setSubset(CgmesSubset subset) {
            this.subset = subset;
            return this;
        }

        @Override
        public ModelAdder setId(String id) {
            this.id = id;
            return this;
        }

        @Override
        public ModelAdderImpl setDescription(String description) {
            this.description = description;
            return this;
        }

        @Override
        public ModelAdderImpl setVersion(int version) {
            this.version = version;
            return this;
        }

        @Override
        public ModelAdderImpl setModelingAuthoritySet(String modelingAuthoritySet) {
            this.modelingAuthoritySet = modelingAuthoritySet;
            return this;
        }

        @Override
        public ModelAdder addProfile(String profile) {
            profiles.add(Objects.requireNonNull(profile));
            return this;
        }

        @Override
        public ModelAdderImpl addDependentOn(String dependentOn) {
            this.dependentOn.add(Objects.requireNonNull(dependentOn));
            return this;
        }

        @Override
        public ModelAdderImpl addSupersedes(String supersedes) {
            this.supersedes.add(Objects.requireNonNull(supersedes));
            return this;
        }

        @Override
        public CgmesMetadataModelsAdderImpl add() {
            if (subset == null) {
                throw new PowsyblException("Model subset is undefined");
            }
            if (id == null) {
                throw new PowsyblException("Model id is undefined");
            }
            if (modelingAuthoritySet == null) {
                throw new PowsyblException("Model modelingAuthoritySet is undefined");
            }
            if (profiles.isEmpty()) {
                throw new PowsyblException("Model must contain at least one profile");
            }
            models.add(new CgmesMetadataModel(subset, modelingAuthoritySet)
                .setId(id)
                .setDescription(description)
                .setVersion(version)
                .addProfiles(profiles)
                .addDependentOn(dependentOn)
                .addSupersedes(supersedes));
            return CgmesMetadataModelsAdderImpl.this;
        }
    }

    private final List<CgmesMetadataModel> models = new ArrayList<>();

    public CgmesMetadataModelsAdderImpl(Network extendable) {
        super(extendable);
    }

    @Override
    public ModelAdder newModel() {
        return new ModelAdderImpl();
    }

    @Override
    protected CgmesMetadataModels createExtension(Network network) {
        if (models.isEmpty()) {
            throw new PowsyblException("Must contain at least one model");
        }

        var attributes = CgmesMetadataModelsAttributes.builder()
            .models(models.stream().map(m ->
                new CgmesMetadataModelAttributes(m.getSubset(),
                    m.getId(), m.getDescription(), m.getVersion(), m.getModelingAuthoritySet(),
                    new ArrayList<>(m.getProfiles()), new ArrayList<>(m.getDependentOn()), new ArrayList<>(m.getSupersedes())))
            .collect(Collectors.toList()))
            .build();
        ((NetworkImpl) network).updateResource(res -> res.getAttributes().getExtensionAttributes().put(CgmesMetadataModels.NAME, attributes));

        return new CgmesMetadataModelsImpl(network);
    }
}
