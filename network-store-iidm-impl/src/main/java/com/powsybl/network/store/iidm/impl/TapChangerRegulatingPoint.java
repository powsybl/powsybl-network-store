package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.network.store.model.*;

import java.util.Objects;
import java.util.function.Function;

public final class TapChangerRegulatingPoint implements RegulatingPoint {
    private final NetworkObjectIndex index;
    private final AbstractTapChanger tapChanger;
    private final Function<Attributes, AbstractRegulatingEquipmentAttributes> attributesGetter;

    public TapChangerRegulatingPoint(NetworkObjectIndex index, AbstractTapChanger tapChanger,
                                     Function<Attributes, AbstractRegulatingEquipmentAttributes> attributesGetter) {
        this.index = index;
        this.attributesGetter = Objects.requireNonNull(attributesGetter);
        this.tapChanger = tapChanger;
    }

    public RegulatingPointAttributes getAttributes() {
        return attributesGetter.apply(tapChanger.getAttributes()).getRegulatingPoint();
    }

    public Terminal getRegulatingTerminal() {
        Terminal regulatingTerminal = TerminalRefUtils.getTerminal(index, getAttributes().getRegulatingTerminal());
        Terminal localTerminal = TerminalRefUtils.getTerminal(index, getAttributes().getLocalTerminal());
        return regulatingTerminal != null ? regulatingTerminal : localTerminal;
    }

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

    public void setRegulatingTerminalAsLocalTerminalAndRemoveRegulation() {
        TerminalImpl<?> oldRegulatingTerminal = (TerminalImpl<?>) TerminalRefUtils.getTerminal(index,
            getAttributes().getRegulatingTerminal());
        if (oldRegulatingTerminal != null) {
            oldRegulatingTerminal.removeRegulatingPoint(this);
        }
        resetRegulationToLocalTerminal();
    }

    public void resetRegulationToLocalTerminal() {
        tapChanger.getTransformer().updateResource(res -> getAttributes().setRegulatingTerminal(getAttributes().getLocalTerminal()));
        tapChanger.getTransformer().updateResource(res -> getAttributes().setRegulatedResourceType(getAttributes().getRegulatingResourceType()));
    }

    public void setRegulationMode(String regulationMode) {
        tapChanger.getTransformer().updateResource(res -> getAttributes().setRegulationMode(regulationMode));
    }

    public void removeRegulation() {
        Terminal terminal = TerminalRefUtils.getTerminal(index,
            getAttributes().getLocalTerminal());
        if (terminal instanceof TerminalImpl<?> localTerminal) {
            Terminal regulatingTerminal = TerminalRefUtils.getTerminal(index, getAttributes().getRegulatingTerminal());
            // set local terminal as regulating terminal
            resetRegulationToLocalTerminal();
            // rest regulation mode for equipment having one
            resetRegulationMode(regulatingTerminal, localTerminal);
        } else {
            throw new PowsyblException("Cannot remove regulation because the local terminal is null");
        }
    }

    private void resetRegulationMode(Terminal regulatingTerminal, Terminal localTerminal) {
        // if localTerminal or regulatingTerminal is not connected then the bus is null
        if (regulatingTerminal != null && localTerminal.isConnected() && regulatingTerminal.isConnected() &&
            !localTerminal.getBusView().getBus().equals(regulatingTerminal.getBusView().getBus())) {
            switch (getAttributes().getRegulatingResourceType()) {
                // for svc we set the regulation mode to Off if the regulation was not on the same bus than the svc. If the svc is on the same bus were the equipment was remove we keep the regulation
                case STATIC_VAR_COMPENSATOR ->
                    setRegulationMode(String.valueOf(StaticVarCompensator.RegulationMode.OFF));
                case GENERATOR, SHUNT_COMPENSATOR, VSC_CONVERTER_STATION -> {
                }
                default -> throw new PowsyblException("No regulation for this kind of equipment");
            }
        }
    }

    void remove() {
        TerminalImpl<?> regulatingTerminal = (TerminalImpl<?>) TerminalRefUtils.getTerminal(index, getAttributes().getRegulatingTerminal());
        regulatingTerminal.removeRegulatingPoint(this);
    }

    public String getRegulatingEquipmentId() {
        return getAttributes().getRegulatingEquipmentId();
    }

    public ResourceType getRegulatingEquipmentType() {
        return getAttributes().getRegulatingResourceType();
    }
}
