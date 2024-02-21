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
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface BranchAttributes extends IdentifiableAttributes, Contained, OperatingStatusHolder {

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

    List<OperationalLimitGroupAttributes> getOperationalLimitsGroups1();

    String getSelectedOperationalLimitsGroupId1();

    void setOperationalLimitsGroups1(List<OperationalLimitGroupAttributes> operationalLimitsGroups);

    default OperationalLimitGroupAttributes getOperationalLimitsGroup1(String id) {
        return getOperationalLimitsGroups1() != null ? getOperationalLimitsGroups1().stream()
                .filter(group -> group.getId().equals(id))
                .findFirst()
                .orElse(null) : null;
    }

    default OperationalLimitGroupAttributes getSelectedOperationalLimitsGroup1() {
        return getOperationalLimitsGroup1(getSelectedOperationalLimitsGroupId1());
    }

    void setSelectedOperationalLimitsGroupId1(String id);

    List<OperationalLimitGroupAttributes> getOperationalLimitsGroups2();

    String getSelectedOperationalLimitsGroupId2();

    void setOperationalLimitsGroups2(List<OperationalLimitGroupAttributes> operationalLimitsGroups);

    default OperationalLimitGroupAttributes getOperationalLimitsGroup2(String id) {
        return getOperationalLimitsGroups2() != null ? getOperationalLimitsGroups2().stream()
                .filter(group -> group.getId().equals(id))
                .findFirst()
                .orElse(null) : null;
    }

    default OperationalLimitGroupAttributes getSelectedOperationalLimitsGroup2() {
        return getOperationalLimitsGroup2(getSelectedOperationalLimitsGroupId2());
    }

    void setSelectedOperationalLimitsGroupId2(String id);

    @JsonIgnore
    default Set<String> getContainerIds() {
        return ImmutableSet.<String>builder()
                .add(getVoltageLevelId1())
                .add(getVoltageLevelId2())
                .build();
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
