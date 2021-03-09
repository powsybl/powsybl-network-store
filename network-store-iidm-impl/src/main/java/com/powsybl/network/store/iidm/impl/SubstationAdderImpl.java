/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.SubstationAdder;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.SubstationAttributes;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class SubstationAdderImpl extends AbstractIdentifiableAdder<SubstationAdderImpl> implements SubstationAdder {

    private Country country;

    private String tso;

    private final Set<String> geographicalTags = new LinkedHashSet<>();

    SubstationAdderImpl(NetworkObjectIndex index) {
        super(index);
    }

    @Override
    public SubstationAdder setCountry(Country country) {
        this.country = country;
        return this;
    }

    @Override
    public SubstationAdder setTso(String tso) {
        this.tso = tso;
        return this;
    }

    @Override
    public SubstationAdder setGeographicalTags(String... tags) {
        if (tags != null) {
            for (String tag : tags) {
                this.geographicalTags.add(tag);
            }
        }

        return this;
    }

    @Override
    public Substation add() {
        String id = checkAndGetUniqueId();

        Resource<SubstationAttributes> resource = Resource.substationBuilder(index.getNetwork().getUuid(), index.getResourceUpdater())
                .id(id)
                .attributes(SubstationAttributes.builder()
                                                .name(getName())
                                                .fictitious(isFictitious())
                                                .country(country)
                                                .tso(tso)
                                                .geographicalTags(geographicalTags)
                                                .build())
                .build();
        return getIndex().createSubstation(resource);
    }

    @Override
    protected String getTypeDescription() {
        return "Substation";
    }
}
