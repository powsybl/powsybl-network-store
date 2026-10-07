/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

/**
 * Marker interface for attributes of equipments implementing
 * {@link com.powsybl.iidm.network.regulation.VoltageRegulationHolder}.
 *
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public interface VoltageRegulationHolderAttributes {

    VoltageRegulationAttributes getVoltageRegulation();

    void setVoltageRegulation(VoltageRegulationAttributes voltageRegulation);
}
