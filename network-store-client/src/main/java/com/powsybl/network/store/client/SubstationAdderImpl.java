/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.client;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.SubstationAdder;
import com.powsybl.network.store.model.Resource;
import com.powsybl.network.store.model.SubstationAttributes;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class SubstationAdderImpl implements SubstationAdder {

    private final NetworkObjectIndex index;

    private String id;

    private String name;

    private Country country;

    private String tso;

    SubstationAdderImpl(NetworkObjectIndex index) {
        this.index = index;
    }

    @Override
    public SubstationAdder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public SubstationAdder setEnsureIdUnicity(boolean ensureIdUnicity) {
        // TODO
        return this;
    }

    @Override
    public SubstationAdder setName(String name) {
        this.name = name;
        return this;
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
        // TODO
        return this;
    }

    @Override
    public Substation add() {
        // TODO validation
        Resource<SubstationAttributes> resource = Resource.substationBuilder()
                .id(id)
                .attributes(SubstationAttributes.builder()
                                                .name(name)
                                                .country(country)
                                                .tso(tso)
                                                .build())
                .build();
        return index.createSubstation(resource);
    }
}
