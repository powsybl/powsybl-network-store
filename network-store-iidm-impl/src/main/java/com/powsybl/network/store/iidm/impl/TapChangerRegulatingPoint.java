package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.*;

import java.util.function.Function;

public final class TapChangerRegulatingPoint extends AbstractRegulatingPoint {
    private final AbstractTapChanger tapChanger;

    public TapChangerRegulatingPoint(NetworkObjectIndex index, AbstractTapChanger tapChanger,
                                     Function<Attributes, AbstractRegulatingEquipmentAttributes> attributesGetter) {
        super(index, attributesGetter);
        this.tapChanger = tapChanger;
    }

    @Override
    public RegulatingPointAttributes getAttributes() {
        return attributesGetter.apply(tapChanger.getAttributes()).getRegulatingPoint();
    }

    @Override
    protected AbstractIdentifiableImpl getIdentifiable() {
        return tapChanger.getTransformer();
    }

    @Override
    protected void resetRegulationMode(Terminal regulatingTerminal, Terminal localTerminal) {
        // if localTerminal or regulatingTerminal is not connected then the bus is null
        if (regulatingTerminal != null && localTerminal.isConnected() && regulatingTerminal.isConnected() &&
            !localTerminal.getBusView().getBus().equals(regulatingTerminal.getBusView().getBus())) {
            // the target can be inappropriated if it was a remote regulation
            tapChanger.setRegulating(false);
            switch (getAttributes().getRegulatingResourceSubType()) {
                // for phase tap changer we reset the regulation mode to Fixed Tap
                case PHASE_TAP_CHANGER, PHASE_TAP_CHANGER_SIDE_ONE, PHASE_TAP_CHANGER_SIDE_TWO, PHASE_TAP_CHANGER_SIDE_THREE ->
                    setRegulationMode(String.valueOf(PhaseTapChanger.RegulationMode.FIXED_TAP));
                case RATIO_TAP_CHANGER, RATIO_TAP_CHANGER_SIDE_ONE, RATIO_TAP_CHANGER_SIDE_TWO, RATIO_TAP_CHANGER_SIDE_THREE ->
                    setRegulationMode(null);
                default -> throw new PowsyblException("No tap changer regulation for " + getAttributes().getRegulatingResourceType() + " this kind of equipment");
            }
        }
        // if the regulating equipment was already regulating on his local bus we reallocate the regulating point and we keep the regulation on
    }
}
