/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License (http://mozilla.org/MPL-2.0/).
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.model;

/**
 * Local voltage target attributes used by the common voltage regulation API.
 */
public interface VoltageRegulationTargetAttributes {

    double getLocalTargetV();

    void setLocalTargetV(double localTargetV);
}
