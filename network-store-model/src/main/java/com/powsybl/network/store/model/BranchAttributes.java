/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface BranchAttributes extends IdentifiableAttributes, Contained, LimitSelector {

    String getVoltageLevelId1();

    void setVoltageLevelId1(String voltageLevelId1);

    String getVoltageLevelId2();

    void setVoltageLevelId2(String voltageLevelId2);

    Integer getNode1();

    void setNode1(Integer node1);

    String getBus1();

    void setBus1(String bus1);

    String getConnectableBus1();

    void setConnectableBus1(String bus1);

    Integer getNode2();

    void setNode2(Integer node2);

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

    LimitsAttributes getCurrentLimits1();

    void setCurrentLimits1(LimitsAttributes currentLimits);

    LimitsAttributes getCurrentLimits2();

    void setCurrentLimits2(LimitsAttributes currentLimits);

    LimitsAttributes getApparentPowerLimits1();

    void setApparentPowerLimits1(LimitsAttributes apparentPowerLimit);

    LimitsAttributes getApparentPowerLimits2();

    void setApparentPowerLimits2(LimitsAttributes apparentPowerLimit);

    LimitsAttributes getActivePowerLimits1();

    void setActivePowerLimits1(LimitsAttributes activePowerLimits);

    LimitsAttributes getActivePowerLimits2();

    void setActivePowerLimits2(LimitsAttributes activePowerLimits);

    String getBranchStatus();

    void setBranchStatus(String branchStatus);

    @JsonIgnore
    default Set<String> getContainerIds() {
        return ImmutableSet.<String>builder()
                .add(getVoltageLevelId1())
                .add(getVoltageLevelId2())
                .build();
    }

    @Override
    @JsonIgnore
    default List<Integer> getSideList() {
        return List.of(1, 2);
    }

    @Override
    default LimitsAttributes getCurrentLimits(int side) {
        if (side == 1) {
            return getCurrentLimits1();
        }
        return getCurrentLimits2();
    }

    @Override
    default LimitsAttributes getApparentPowerLimits(int side) {
        if (side == 1) {
            return getApparentPowerLimits1();
        }
        return getApparentPowerLimits2();
    }

    @Override
    default LimitsAttributes getActivePowerLimits(int side) {
        if (side == 1) {
            return getActivePowerLimits1();
        }
        return getActivePowerLimits2();
    }

    @Override
    default void setCurrentLimits(int side, LimitsAttributes limits) {
        if (side == 1) {
            setCurrentLimits1(limits);
        } else {
            setCurrentLimits2(limits);
        }
    }

    @Override
    default void setApparentPowerLimits(int side, LimitsAttributes limits) {
        if (side == 1) {
            setApparentPowerLimits1(limits);
        } else {
            setApparentPowerLimits2(limits);
        }
    }

    @Override
    default void setActivePowerLimits(int side, LimitsAttributes limits) {
        if (side == 1) {
            setActivePowerLimits1(limits);
        } else {
            setActivePowerLimits2(limits);
        }
    }
}
