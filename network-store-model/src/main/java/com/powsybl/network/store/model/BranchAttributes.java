/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableSet;
import com.powsybl.commons.PowsyblException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface BranchAttributes extends IdentifiableAttributes, Contained, LimitHolder, RegulatedEquipmentAttributes {

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

    Map<String, OperationalLimitsGroupAttributes> getOperationalLimitsGroups1();

    String getSelectedOperationalLimitsGroupId1();

    void setOperationalLimitsGroups1(Map<String, OperationalLimitsGroupAttributes> operationalLimitsGroups);

    default OperationalLimitsGroupAttributes getOperationalLimitsGroup1(String id) {
        return getOperationalLimitsGroups1().get(id);
    }

    default OperationalLimitsGroupAttributes getOrCreateOperationalLimitsGroup1(String id) {
        return getOperationalLimitsGroups1().computeIfAbsent(id, s -> new OperationalLimitsGroupAttributes(id, null, null, null, null));
    }

    default OperationalLimitsGroupAttributes getOrCreateOperationalLimitsGroup2(String id) {
        return getOperationalLimitsGroups2().computeIfAbsent(id, s -> new OperationalLimitsGroupAttributes(id, null, null, null, null));
    }

    @JsonIgnore
    default OperationalLimitsGroupAttributes getSelectedOperationalLimitsGroup1() {
        return getOperationalLimitsGroup1(getSelectedOperationalLimitsGroupId1());
    }

    void setSelectedOperationalLimitsGroupId1(String id);

    Map<String, OperationalLimitsGroupAttributes> getOperationalLimitsGroups2();

    String getSelectedOperationalLimitsGroupId2();

    void setOperationalLimitsGroups2(Map<String, OperationalLimitsGroupAttributes> operationalLimitsGroups);

    default OperationalLimitsGroupAttributes getOperationalLimitsGroup2(String id) {
        return getOperationalLimitsGroups2().get(id);
    }

    @JsonIgnore
    default OperationalLimitsGroupAttributes getSelectedOperationalLimitsGroup2() {
        return getOperationalLimitsGroup2(getSelectedOperationalLimitsGroupId2());
    }

    void setSelectedOperationalLimitsGroupId2(String id);

    @Override
    default Map<String, OperationalLimitsGroupAttributes> getOperationalLimitsGroups(int side) {
        if (side == 1) {
            return getOperationalLimitsGroups1();
        } else if (side == 2) {
            return getOperationalLimitsGroups2();
        } else {
            throw new IllegalArgumentException(EXCEPTION_UNKNOWN_SIDE);
        }
    }

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
    default LimitsAttributes getCurrentLimits(int side, String operationalLimitsGroupId) {
        if (side == 1) {
            return getOperationalLimitsGroup1(operationalLimitsGroupId) != null
                    ? getOperationalLimitsGroup1(operationalLimitsGroupId).getCurrentLimits()
                    : null;
        }
        if (side == 2) {
            return getOperationalLimitsGroup2(operationalLimitsGroupId) != null
                    ? getOperationalLimitsGroup2(operationalLimitsGroupId).getCurrentLimits()
                    : null;
        }
        throw new IllegalArgumentException(EXCEPTION_UNKNOWN_SIDE);
    }

    @Override
    default LimitsAttributes getApparentPowerLimits(int side, String operationalLimitsGroupId) {
        if (side == 1) {
            return getOperationalLimitsGroup1(operationalLimitsGroupId) != null
                    ? getOperationalLimitsGroup1(operationalLimitsGroupId).getApparentPowerLimits()
                    : null;
        }
        if (side == 2) {
            return getOperationalLimitsGroup2(operationalLimitsGroupId) != null
                    ? getOperationalLimitsGroup2(operationalLimitsGroupId).getApparentPowerLimits()
                    : null;
        }
        throw new IllegalArgumentException(EXCEPTION_UNKNOWN_SIDE);
    }

    @Override
    default LimitsAttributes getActivePowerLimits(int side, String operationalLimitsGroupId) {
        if (side == 1) {
            return getOperationalLimitsGroup1(operationalLimitsGroupId) != null
                    ? getOperationalLimitsGroup1(operationalLimitsGroupId).getActivePowerLimits()
                    : null;
        }
        if (side == 2) {
            return getOperationalLimitsGroup2(operationalLimitsGroupId) != null
                    ? getOperationalLimitsGroup2(operationalLimitsGroupId).getActivePowerLimits()
                    : null;
        }
        throw new IllegalArgumentException(EXCEPTION_UNKNOWN_SIDE);
    }

    @Override
    default void setCurrentLimits(int side, LimitsAttributes limits, String operationalLimitsGroupId) {
        if (side == 1) {
            getOrCreateOperationalLimitsGroup1(operationalLimitsGroupId).setCurrentLimits(limits);
        } else if (side == 2) {
            getOrCreateOperationalLimitsGroup2(operationalLimitsGroupId).setCurrentLimits(limits);
        } else {
            throw new IllegalArgumentException(EXCEPTION_UNKNOWN_SIDE);
        }
    }

    @Override
    default void setApparentPowerLimits(int side, LimitsAttributes limits, String operationalLimitsGroupId) {
        if (side == 1) {
            getOrCreateOperationalLimitsGroup1(operationalLimitsGroupId).setApparentPowerLimits(limits);
        } else if (side == 2) {
            getOrCreateOperationalLimitsGroup2(operationalLimitsGroupId).setApparentPowerLimits(limits);
        } else {
            throw new IllegalArgumentException(EXCEPTION_UNKNOWN_SIDE);
        }
    }

    @Override
    default void setActivePowerLimits(int side, LimitsAttributes limits, String operationalLimitsGroupId) {
        if (side == 1) {
            getOrCreateOperationalLimitsGroup1(operationalLimitsGroupId).setActivePowerLimits(limits);
        } else if (side == 2) {
            getOrCreateOperationalLimitsGroup2(operationalLimitsGroupId).setActivePowerLimits(limits);
        } else {
            throw new IllegalArgumentException(EXCEPTION_UNKNOWN_SIDE);
        }
    }

    @JsonIgnore
    @Override
    default Attributes filter(AttributeFilter filter) {
        if (filter != AttributeFilter.SV) {
            throw new PowsyblException("Unsupported attribute filter: " + filter);
        }
        return new BranchSvAttributes(getP1(), getQ1(), getP2(), getQ2());
    }
}
