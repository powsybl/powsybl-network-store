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
            switch (getAttributes().getRegulatingResourceType()) {
                // for svc we set the regulation mode to Off if the regulation was not on the same bus than the svc. If the svc is on the same bus were the equipment was remove we keep the regulation
                case PHASE_TAP_CHANGER ->
                    setRegulationMode(String.valueOf(PhaseTapChanger.RegulationMode.FIXED_TAP));
                case RATIO_TAP_CHANGER -> {
                    setRegulationMode(null);
                }
                default -> throw new PowsyblException("No tap changer regulation for " + getAttributes().getRegulatingResourceType() + " this kind of equipment");
            }
        }
    }
}
