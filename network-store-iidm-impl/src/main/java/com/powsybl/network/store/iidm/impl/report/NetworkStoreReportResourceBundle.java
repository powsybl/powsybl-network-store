/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.report;

import com.google.auto.service.AutoService;
import com.powsybl.commons.report.ReportResourceBundle;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
@AutoService(ReportResourceBundle.class)
public class NetworkStoreReportResourceBundle {
    public static final String BASE_NAME = "com.powsybl.network.store.reports";

    public String getBaseName() {
        return BASE_NAME;
    }
}
