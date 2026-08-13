/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.network.store.model;

import com.powsybl.iidm.network.extensions.ObservabilityArea;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Characteristics attributes")
public class CharacteristicsAttributes {

    private int areaNumber;

    private ObservabilityArea.ObservabilityStatus status;
}
