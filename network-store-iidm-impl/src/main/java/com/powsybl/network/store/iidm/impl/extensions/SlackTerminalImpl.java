/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.network.store.iidm.impl.TerminalImpl;
import com.powsybl.network.store.iidm.impl.TerminalRefUtils;
import com.powsybl.network.store.iidm.impl.VoltageLevelImpl;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Nicolas Noir <nicolas.noir at rte-france.com>
 */
public class SlackTerminalImpl extends AbstractIidmExtension<VoltageLevel> implements SlackTerminal {
    private final Set<Terminal> registeredTerminals = new HashSet<>();

    public SlackTerminalImpl(VoltageLevelImpl voltageLevel) {
        super(Objects.requireNonNull(voltageLevel));
        syncReferrers();
    }

    @Override
    public Terminal getTerminal() {
        return ((VoltageLevelImpl) getExtendable()).getTerminal(((VoltageLevelImpl) getExtendable()).getResource().getAttributes().getSlackTerminal());
    }

    @Override
    public SlackTerminal setTerminal(Terminal terminal) {
        if (terminal != null && !terminal.getVoltageLevel().equals(getExtendable())) {
            throw new PowsyblException("Terminal given is not in the right VoltageLevel ("
                    + terminal.getVoltageLevel().getId() + " instead of " + getExtendable().getId() + ")");
        }
        ((VoltageLevelImpl) getExtendable()).updateResourceExtension(this, res -> res.getAttributes().setSlackTerminal(TerminalRefUtils.getTerminalRefAttributes(terminal)), "terminal",
                TerminalRefUtils.getTerminalRefAttributes(getTerminal()), TerminalRefUtils.getTerminalRefAttributes(terminal));
        syncReferrers();
        return this;
    }

    @Override
    public boolean isEmpty() {
        return Objects.isNull(getTerminal());
    }

    @Override
    public void onReferencedRemoval(Terminal removedTerminal) {
        var variantManager = getExtendable().getNetwork().getVariantManager();
        String workingVariantId = variantManager.getWorkingVariantId();
        try {
            for (String variantId : variantManager.getVariantIds()) {
                variantManager.setWorkingVariant(variantId);
                Terminal currentTerminal = getTerminal();
                if (sameTerminal(currentTerminal, removedTerminal)) {
                    setTerminal(null);
                }
            }
        } finally {
            variantManager.setWorkingVariant(workingVariantId);
        }
    }

    @Override
    public void onReferencedReplacement(Terminal oldReferenced, Terminal newReferenced) {
        Terminal oldTerminal = getTerminal();
        if (oldTerminal == null || oldTerminal.getConnectable().getId().equals(oldReferenced.getConnectable().getId())) {
            setTerminal(newReferenced);
        }
    }

    @Override
    public void cleanup() {
        for (Terminal terminal : registeredTerminals) {
            ((TerminalImpl<?>) terminal).getReferrerManager().unregister(this);
        }
        registeredTerminals.clear();
    }

    private void syncReferrers() {
        var variantManager = getExtendable().getNetwork().getVariantManager();
        String workingVariantId = variantManager.getWorkingVariantId();
        Set<Terminal> referencedTerminals = new HashSet<>();
        try {
            for (String variantId : variantManager.getVariantIds()) {
                variantManager.setWorkingVariant(variantId);
                Terminal terminal = getTerminal();
                if (terminal != null) {
                    referencedTerminals.add(terminal);
                }
            }
        } finally {
            variantManager.setWorkingVariant(workingVariantId);
        }

        for (Terminal terminal : new HashSet<>(registeredTerminals)) {
            if (!referencedTerminals.contains(terminal)) {
                ((TerminalImpl<?>) terminal).getReferrerManager().unregister(this);
                registeredTerminals.remove(terminal);
            }
        }
        for (Terminal terminal : referencedTerminals) {
            if (registeredTerminals.add(terminal)) {
                ((TerminalImpl<?>) terminal).getReferrerManager().register(this);
            }
        }
    }

    private boolean sameTerminal(Terminal first, Terminal second) {
        return first != null && second != null
            && Objects.equals(TerminalRefUtils.getTerminalRefAttributes(first), TerminalRefUtils.getTerminalRefAttributes(second));
    }
}
