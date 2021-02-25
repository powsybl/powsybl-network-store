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
public interface BranchAttributes extends IdentifiableAttributes, Contained {

    String getVoltageLevelId1();

    String getVoltageLevelId2();

    Integer getNode1();

    String getBus1();

    void setBus1(String bus1);

    String getConnectableBus1();

    void setConnectableBus1(String bus1);

    Integer getNode2();

    String getBus2();

    void setBus2(String bus2);

    String getConnectableBus2();

    void setConnectableBus2(String bus2);

    double getP1();

    void setP1(double p1);

    double getQ1();

    void setQ1(double q1);

    double getP2();

    void setP2(double p2);

    double getQ2();

    void setQ2(double q2);

    ConnectablePositionAttributes getPosition1();

    void setPosition1(ConnectablePositionAttributes position);

    ConnectablePositionAttributes getPosition2();

    void setPosition2(ConnectablePositionAttributes position);

    CurrentLimitsAttributes getCurrentLimits1();

    void setCurrentLimits1(CurrentLimitsAttributes currentLimits);

    CurrentLimitsAttributes getCurrentLimits2();

    void setCurrentLimits2(CurrentLimitsAttributes currentLimits);

    @JsonIgnore
    default Set<String> getContainerIds() {
        return ImmutableSet.<String>builder()
                .add(getVoltageLevelId1())
                .add(getVoltageLevelId2())
                .build();
    }
}
