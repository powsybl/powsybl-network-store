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

    public RegulatingPointAttributes getAttributes() {
        return attributesGetter.apply(tapChanger.getAttributes()).getRegulatingPoint();
    }

    @Override
    public void setRegulatingTerminal(TerminalImpl<?> regulatingTerminal) {
        TerminalImpl<?> oldRegulatingTerminal = (TerminalImpl<?>) TerminalRefUtils.getTerminal(index,
            getAttributes().getRegulatingTerminal());
        if (oldRegulatingTerminal != null) {
            oldRegulatingTerminal.removeRegulatingPoint(this);
        }
        regulatingTerminal.setAsRegulatingPoint(this);
        tapChanger.getTransformer().updateResource(res -> getAttributes()
            .setRegulatingTerminal(TerminalRefUtils.getTerminalRefAttributes(regulatingTerminal)));
        tapChanger.getTransformer().updateResource(res -> getAttributes()
            .setRegulatedResourceType(ResourceType.convert(regulatingTerminal.getConnectable().getType())));
    }

    @Override
    public void resetRegulationToLocalTerminal() {
        tapChanger.getTransformer().updateResource(res -> getAttributes().setRegulatingTerminal(getAttributes().getLocalTerminal()));
        tapChanger.getTransformer().updateResource(res -> getAttributes().setRegulatedResourceType(getAttributes().getRegulatingResourceType()));
    }

    @Override
    public void setRegulationMode(String regulationMode) {
        tapChanger.getTransformer().updateResource(res -> getAttributes().setRegulationMode(regulationMode));
    }

    @Override
    protected void resetRegulationMode(Terminal regulatingTerminal, Terminal localTerminal) {
        // if localTerminal or regulatingTerminal is not connected then the bus is null
        if (regulatingTerminal != null && localTerminal.isConnected() && regulatingTerminal.isConnected() &&
            !localTerminal.getBusView().getBus().equals(regulatingTerminal.getBusView().getBus())) {
            switch (getAttributes().getRegulatingResourceSubType()) {
                // for phase tap changer we reset the regulation mode to Fixed Tap
                case PHASE_TAP_CHANGER, PHASE_TAP_CHANGER_SIDE_ONE, PHASE_TAP_CHANGER_SIDE_TWO, PHASE_TAP_CHANGER_SIDE_THREE ->
                    setRegulationMode(String.valueOf(PhaseTapChanger.RegulationMode.FIXED_TAP));
                case RATIO_TAP_CHANGER, RATIO_TAP_CHANGER_SIDE_ONE, RATIO_TAP_CHANGER_SIDE_TWO, RATIO_TAP_CHANGER_SIDE_THREE ->
                    setRegulationMode(null);
                default -> throw new PowsyblException("No tap changer regulation for " + getAttributes().getRegulatingResourceType() + " this kind of equipment");
            }
        }
    }
}
