package com.powsybl.network.store.client;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.TerminalRefAttributes;

public final class TerminalRefUtil {

    private TerminalRefUtil() {

    }

    public static Terminal getRegulatingTerminal(NetworkObjectIndex index, TerminalRefAttributes terminalRefAttributes) {
        Identifiable<?> identifiable = index.getIdentifiable(terminalRefAttributes.getIdEquipment());
        String side = terminalRefAttributes.getSide();

        if (identifiable instanceof Injection) {
            return ((Injection<?>) identifiable).getTerminal();
        } else if (identifiable instanceof Branch) {
            return ((Branch<?>) identifiable).getTerminal(Branch.Side.valueOf(side));
        } else if (identifiable instanceof ThreeWindingsTransformer) {
            ThreeWindingsTransformer twt = (ThreeWindingsTransformer) identifiable;
            return twt.getTerminal(ThreeWindingsTransformer.Side.valueOf(side));
        } else {
            throw new AssertionError("Unexpected Identifiable instance: " + identifiable.getClass());
        }
    }

    public static String getSide(Terminal regulatingTerminal) {
        String side = null;
        if (regulatingTerminal.getConnectable().getTerminals().size() > 1) {
            if (regulatingTerminal.getConnectable() instanceof Branch) {
                Branch<?> branch = (Branch<?>) regulatingTerminal.getConnectable();
                side = branch.getSide(regulatingTerminal).name();
            } else if (regulatingTerminal.getConnectable() instanceof ThreeWindingsTransformer) {
                ThreeWindingsTransformer twt = (ThreeWindingsTransformer) regulatingTerminal.getConnectable();
                side = twt.getSide(regulatingTerminal).name();
            } else {
                throw new AssertionError("Unexpected Connectable instance: " + regulatingTerminal.getConnectable().getClass());
            }
        }
        return side;
    }

    public static TerminalRefAttributes regulatingTerminalToTerminaRefAttributes(Terminal regulatingTerminal) {
        return TerminalRefAttributes.builder()
                .idEquipment(regulatingTerminal.getConnectable().getId())
                .side(getSide(regulatingTerminal))
                .build();
    }
}
