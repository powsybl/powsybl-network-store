/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.powsybl.commons.PowsyblException;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface InjectionAttributes extends IdentifiableAttributes, Contained {

    String getVoltageLevelId();

    void setVoltageLevelId(String voltageLevelId);

    Integer getNode();

    void setNode(Integer node);

    String getBus();

    void setBus(String bus);

    String getConnectableBus();

    void setConnectableBus(String bus);

    double getP();

    void setP(double p);

    double getQ();

    void setQ(double q);

    ConnectablePositionAttributes getPosition();

    void setPosition(ConnectablePositionAttributes position);

    @JsonIgnore
    default Set<String> getContainerIds() {
        return Collections.singleton(getVoltageLevelId());
    }

    Map<String, ResourceType> getRegulatingEquipments();

    void setRegulatingEquipments(Map<String, ResourceType> regulatingEquipments);

    @JsonIgnore
    @Override
    default Attributes filter(AttributeFilter filter) {
        if (filter != AttributeFilter.SV) {
            throw new PowsyblException("Unsupported attribute filter: " + filter);
        }
        return new InjectionSvAttributes(getP(), getQ());
    }
}
