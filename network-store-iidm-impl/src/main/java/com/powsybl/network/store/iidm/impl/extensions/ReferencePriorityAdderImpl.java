/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.extensions.ReferencePriority;
import com.powsybl.iidm.network.extensions.ReferencePriorityAdder;
import com.powsybl.network.store.iidm.impl.AbstractConnectableImpl;
import com.powsybl.network.store.iidm.impl.TerminalRefUtils;
import com.powsybl.network.store.model.ReferencePriorityAttributes;

import java.util.Objects;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ReferencePriorityAdderImpl implements ReferencePriorityAdder {
    private final ReferencePrioritiesImpl<? extends Connectable<?>> referencePriorities;

    private Terminal terminal;
    private int priority;

    public ReferencePriorityAdderImpl(ReferencePrioritiesImpl<? extends Connectable<?>> referencePriorities) {
        this.referencePriorities = Objects.requireNonNull(referencePriorities);
    }

    @Override
    public ReferencePriorityAdder setTerminal(Terminal terminal) {
        this.terminal = terminal;
        return this;
    }

    @Override
    public ReferencePriorityAdder setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public ReferencePriority add() {
        if (terminal == null) {
            throw new NullPointerException("Terminal needs to be set for ReferencePriority extension");
        }
        if (priority < 0) {
            throw new PowsyblException(String.format("Priority (%s) of terminal (equipment %s) should be zero or positive for ReferencePriority extension",
                priority, terminal.getConnectable().getId()));
        }
        AbstractConnectableImpl<?, ?> extendable = (AbstractConnectableImpl<?, ?>) referencePriorities.getExtendable();
        if (!extendable.getTerminals().contains(terminal)) {
            throw new PowsyblException(String.format("The provided terminal does not belong to the connectable %s",
                    extendable.getId()));
        }

        ReferencePriorityAttributes attributes = ReferencePriorityAttributes.builder()
            .terminal(TerminalRefUtils.getTerminalRefAttributes(terminal))
            .priority(priority)
            .build();

        extendable.updateResourceExtension(referencePriorities, resource -> referencePriorities.getAttributes().putReferencePriority(attributes),
                "referencePriorities.referencePriority", null, attributes);
        return new ReferencePriorityImpl(attributes, extendable.getIndex());
    }
}
