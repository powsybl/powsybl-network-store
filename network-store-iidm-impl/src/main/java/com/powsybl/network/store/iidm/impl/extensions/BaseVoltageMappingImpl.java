package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.extensions.Source;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.iidm.impl.NetworkImpl;
import com.powsybl.cgmes.extensions.BaseVoltageMapping;
import com.powsybl.network.store.model.BaseVoltageMappingAttributes;
import com.powsybl.network.store.model.BaseVoltageSourceAttribute;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BaseVoltageMappingImpl extends AbstractExtension<Network> implements BaseVoltageMapping {

    public BaseVoltageMappingImpl(Network network, Map<Double, BaseVoltageSourceAttribute> resourcesBaseVoltages) {
        super(network);
        getNetwork().getResource().getAttributes().setBaseVoltageMapping(new BaseVoltageMappingAttributes(resourcesBaseVoltages));
    }

    private NetworkImpl getNetwork() {
        return (NetworkImpl) getExtendable();
    }

    private Map<Double, BaseVoltageSourceAttribute> getResourcesBaseVoltages() {
        return getNetwork().getResource().getAttributes().getBaseVoltageMapping().getBaseVoltages();
    }

    @Override
    public Map<Double, BaseVoltageSource> getBaseVoltages() {
        return Collections.unmodifiableMap(getResourcesBaseVoltages());
    }

    @Override
    public BaseVoltageSource getBaseVoltage(double nominalVoltage) {
        return getResourcesBaseVoltages().get(nominalVoltage);
    }

    @Override
    public boolean isBaseVoltageMapped(double nominalVoltage) {
        return getResourcesBaseVoltages().containsKey(nominalVoltage);
    }

    @Override
    public boolean isBaseVoltageEmpty() {
        return getResourcesBaseVoltages().isEmpty();
    }

    @Override
    public BaseVoltageMapping addBaseVoltage(double nominalVoltage, String baseVoltageId, Source source) {
        Map<Double, BaseVoltageSourceAttribute> resourcesBaseVoltages = getResourcesBaseVoltages();
        if (resourcesBaseVoltages.containsKey(nominalVoltage)) {
            if (resourcesBaseVoltages.get(nominalVoltage).getSource().equals(Source.IGM) && source.equals(Source.BOUNDARY)) {
                resourcesBaseVoltages.put(nominalVoltage, new BaseVoltageSourceAttribute(baseVoltageId, nominalVoltage, source));
            }
        } else {
            resourcesBaseVoltages.put(nominalVoltage, new BaseVoltageSourceAttribute(baseVoltageId, nominalVoltage, source));
        }
        return this;
    }

    @Override
    public Map<Double, BaseVoltageSource> baseVoltagesByNominalVoltageMap() {
        return new HashMap<>(getBaseVoltages());
    }
}
