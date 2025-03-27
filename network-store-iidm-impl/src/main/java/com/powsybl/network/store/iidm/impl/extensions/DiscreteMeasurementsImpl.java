package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.extensions.DiscreteMeasurement;
import com.powsybl.iidm.network.extensions.DiscreteMeasurementAdder;
import com.powsybl.iidm.network.extensions.DiscreteMeasurements;
import com.powsybl.network.store.iidm.impl.AbstractIdentifiableImpl;
import com.powsybl.network.store.model.DiscreteMeasurementsAttributes;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
public class DiscreteMeasurementsImpl<I extends Identifiable<I>> extends AbstractExtension<I> implements DiscreteMeasurements<I> {

    public DiscreteMeasurementsImpl(I identifiable) {
        super(identifiable);
    }

    private AbstractIdentifiableImpl<?, ?> getIdentifiable() {
        return (AbstractIdentifiableImpl<?, ?>) getExtendable();
    }

    DiscreteMeasurementsAttributes getMeasurementsAttributes() {
        return (DiscreteMeasurementsAttributes) getIdentifiable().getResource().getAttributes().getExtensionAttributes().get(DiscreteMeasurements.NAME);
    }

    @Override
    public Collection<DiscreteMeasurement> getDiscreteMeasurements() {
        return getMeasurementsAttributes().getDiscreteMeasurementAttributes().stream()
                .map(discreteMeasurementAttributes -> new DiscreteMeasurementImpl(this, getIdentifiable(), discreteMeasurementAttributes))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<DiscreteMeasurement> getDiscreteMeasurements(DiscreteMeasurement.Type type) {
        return getMeasurementsAttributes().getDiscreteMeasurementAttributes().stream()
                .filter(discreteMeasurementAttributes -> type.equals(discreteMeasurementAttributes.getType()))
                .map(discreteMeasurementAttributes -> new DiscreteMeasurementImpl(this, getIdentifiable(), discreteMeasurementAttributes))
                .collect(Collectors.toList());
    }

    @Override
    public DiscreteMeasurement getDiscreteMeasurement(String id) {
        return getMeasurementsAttributes().getDiscreteMeasurementAttributes().stream()
                .filter(discreteMeasurementAttributes -> id.equals(discreteMeasurementAttributes.getId()))
                .map(discreteMeasurementAttributes -> new DiscreteMeasurementImpl(this, getIdentifiable(), discreteMeasurementAttributes))
                .findFirst().orElse(null);
    }

    @Override
    public DiscreteMeasurementAdder newDiscreteMeasurement() {
        return new DiscreteMeasurementAdderImpl(this);
    }
}

