/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface InjectionAttributes extends IdentifiableAttributes, RelatedVoltageLevelsAttributes {

    String getVoltageLevelId();

    Integer getNode();

    String getBus();

    String getConnectableBus();

    double getP();

    void setP(double p);

    double getQ();

    void setQ(double q);

    ConnectablePositionAttributes getPosition();

    void setPosition(ConnectablePositionAttributes position);

    @JsonIgnore
    default Set<String> getVoltageLevels() {
        return ImmutableSet.<String>builder().add(getVoltageLevelId()).build();
    }
}
