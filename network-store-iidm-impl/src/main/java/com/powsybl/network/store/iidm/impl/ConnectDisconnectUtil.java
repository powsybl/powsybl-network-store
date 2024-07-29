/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public final class ConnectDisconnectUtil {

    private ConnectDisconnectUtil() {
    }

    /**
     * Connect the specified terminals. It will connect all the specified terminals or none if at least one cannot be
     * connected.
     * @param identifiable network element to connect. It can be a connectable, a tie line or an HVDC line
     * @param terminals list of the terminals that should be connected. For a connectable, it should be its own
     *                  terminals, while for a tie line (respectively an HVDC line) it should be the terminals of the
     *                  underlying dangling lines (respectively converter stations)
     * @param isTypeSwitchToOperate type of switches that can be operated
     * @param reportNode report node
     * @return {@code true} if all the specified terminals have been connected, else {@code false}.
     */
    static boolean connectAllTerminals(AbstractIdentifiableImpl<?, ?> identifiable, List<Terminal> terminals, Predicate<Switch> isTypeSwitchToOperate, ReportNode reportNode) {

        // Boolean used to stop the execution early if needed
        boolean isAlreadyConnected = true;

        // Initialisation of a list to open in case some terminals are in node-breaker view
        Set<SwitchImpl> switchForConnection = new HashSet<>();

        // We try to connect each terminal
        for (Terminal terminal : terminals) {
            // Check if the terminal is already connected
            if (terminal.isConnected()) {
                // If the terminal is already connected, log and continue on other terminals
                reportNode.newReportNode()
                    .withMessageTemplate("alreadyConnectedTerminal", "A terminal of identifiable ${identifiable} is already connected.")
                    .withUntypedValue("identifiable", identifiable.getId())
                    .withSeverity(TypedValue.WARN_SEVERITY)
                    .add();
                continue;
            } else {
                isAlreadyConnected = false;
            }

            // If it's a node-breaker terminal, the switches to connect are added to a set
            if (terminal instanceof TerminalImpl<?> terminalImpl && terminalImpl.isNodeBeakerTopologyKind()
                && !terminalImpl.getConnectingSwitches(isTypeSwitchToOperate, switchForConnection)) {
                // Exit if the terminal cannot be connected
                return false;
            }

            // If it's a bus-breaker terminal, there is nothing to do now
        }

        // Exit if the connectable is already fully connected
        if (isAlreadyConnected) {
            return false;
        }

        // Connect all terminals
        for (Terminal terminal : terminals) {
            if (!terminal.isConnected()
                && terminal instanceof TerminalImpl<?> terminalImpl
                && !terminalImpl.isNodeBeakerTopologyKind()) {
                terminalImpl.connectBusBreaker();
            }
        }

        // The switches are now closed in node-breaker terminals
        closeSwitches(identifiable.getIndex(), switchForConnection);

        return true;
    }

    public static void closeSwitches(NetworkObjectIndex index, Set<SwitchImpl> switchesToClose) {
        switchesToClose.forEach(switchImpl -> {
            // Close
            switchImpl.setOpen(false);
            // Update the resource
            index.updateSwitchResource(switchImpl.getResource());
            // Notify update
            index.notifyUpdate(index.getSwitch(switchImpl.getResource().getId()).orElseThrow(), "open", true, false);
        });
    }

    /**
     * Disconnect the specified terminals. It will disconnect all the specified terminals or none if at least one cannot
     * be disconnected.
     * @param identifiable network element to disconnect. It can be a connectable, a tie line or an HVDC line
     * @param terminals list of the terminals that should be disconnected. For a connectable, it should be its own
     *                  terminals, while for a tie line (respectively an HVDC line) it should be the terminals of the
     *                  underlying dangling lines (respectively converter stations)
     * @param isSwitchOpenable type of switches that can be operated
     * @param reportNode report node
     * @return {@code true} if all the specified terminals have been disconnected, else {@code false}.
     */
    static boolean disconnectAllTerminals(AbstractIdentifiableImpl<?, ?> identifiable, List<Terminal> terminals, Predicate<Switch> isSwitchOpenable, ReportNode reportNode) {
        // Boolean used to stop the execution early if needed
        boolean isAlreadyDisconnected = true;

        // Initialisation of a list to open in case some terminals are in node-breaker view
        Set<SwitchImpl> switchForDisconnection = new HashSet<>();

        // We try to disconnect each terminal
        for (Terminal terminal : terminals) {
            // Check if the terminal is already disconnected
            if (!terminal.isConnected()) {
                reportNode.newReportNode()
                    .withMessageTemplate("alreadyDisconnectedTerminal", "A terminal of identifiable ${identifiable} is already disconnected.")
                    .withUntypedValue("identifiable", identifiable.getId())
                    .withSeverity(TypedValue.WARN_SEVERITY)
                    .add();
                continue;
            }
            // The terminal is connected
            isAlreadyDisconnected = false;

            // If it's a node-breaker terminal, the switches to disconnect are added to a set
            if (terminal instanceof TerminalImpl<?> terminalImpl && terminalImpl.isNodeBeakerTopologyKind()
                && !terminalImpl.getDisconnectingSwitches(isSwitchOpenable, switchForDisconnection)) {
                // Exit if the terminal cannot be disconnected
                return false;
            }
            // If it's a bus-breaker terminal, there is nothing to do
        }

        // Exit if the connectable is already fully disconnected
        if (isAlreadyDisconnected) {
            return false;
        }

        // Disconnect all bus-breaker terminals
        for (Terminal terminal : terminals) {
            if (terminal.isConnected()
                && terminal instanceof TerminalImpl<?> terminalImpl
                && !terminalImpl.isNodeBeakerTopologyKind()) {
                terminalImpl.disconnectBusBreaker();
            }
        }

        // The switches are now open in node-breaker terminals
        openSwitches(identifiable.getIndex(), switchForDisconnection);

        return true;
    }

    public static void openSwitches(NetworkObjectIndex index, Set<SwitchImpl> switchesToOpen) {
        switchesToOpen.forEach(switchImpl -> {
            // Open
            switchImpl.setOpen(true);
            // Update the resource
            index.updateSwitchResource(switchImpl.getResource());
            // Notify update
            index.notifyUpdate(index.getSwitch(switchImpl.getResource().getId()).orElseThrow(), "open", false, true);
        });
    }
}

