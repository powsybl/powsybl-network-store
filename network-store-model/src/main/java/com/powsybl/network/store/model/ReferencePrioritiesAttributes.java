/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Reference priorities attributes")
public class ReferencePrioritiesAttributes {

    @Schema(description = "Reference priorities")
    private List<ReferencePriorityAttributes> referencePriorities = new ArrayList<>();

    public void putReferencePriority(ReferencePriorityAttributes referencePriorityAttributes) {
        Optional<ReferencePriorityAttributes> found = referencePriorities.stream()
            .filter(r -> r.getTerminal().getConnectableId().equals(referencePriorityAttributes.getTerminal().getConnectableId())
                         && (r.getTerminal().getSide() == null ||
                             r.getTerminal().getSide().equals(referencePriorityAttributes.getTerminal().getSide()))).findFirst();
        if (found.isPresent()) {
            found.get().setPriority(referencePriorityAttributes.getPriority());
        } else {
            referencePriorities.add(referencePriorityAttributes);
        }
    }
}
