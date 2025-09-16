/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ReportNodeContext;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.network.store.model.NetworkAttributes;
import com.powsybl.network.store.model.Resource;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * @author Slimane Amar <slimane.amar at rte-france.com>
 */
public class SubnetworkImpl extends NetworkImpl {
    private final String id;

    private ZonedDateTime caseDate = ZonedDateTime.now(ZoneOffset.UTC);

    private int forecastDistance = 0;

    private final String sourceFormat;

    @Override
    public int getForecastDistance() {
        return forecastDistance;
    }

    @Override
    public Network setForecastDistance(int forecastDistance) {
        ValidationUtil.checkForecastDistance(this, forecastDistance);
        this.forecastDistance = forecastDistance;
        return this;
    }

    @Override
    public ZonedDateTime getCaseDate() {
        return caseDate;
    }

    @Override
    public Network setCaseDate(ZonedDateTime caseDate) {
        ValidationUtil.checkCaseDate(this, caseDate);
        this.caseDate = caseDate;
        return this;
    }

    @Override
    public String getSourceFormat() {
        return sourceFormat;
    }

    SubnetworkImpl(String id, NetworkObjectIndex index, Resource<NetworkAttributes> resource, String sourceFormat) {
        super(index, resource);
        this.id = id;
        this.sourceFormat = Objects.requireNonNull(sourceFormat, "source format is null");
    }

    @Override
    public final Collection<Network> getSubnetworks() {
        return Collections.emptyList();
    }

    @Override
    public ReportNodeContext getReportNodeContext() {
        return getNetwork().getReportNodeContext();
    }

    @Override
    public final Network getSubnetwork(String id) {
        return null;
    }

    @Override
    public Identifiable<?> getIdentifiable(String id) {
        if (this.id.equals(id)) {
            return this;
        }
        return getNetwork().getIdentifiable(id);
    }
}
