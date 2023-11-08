/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.util.Identifiables;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 *
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
abstract class AbstractIdentifiableAdder<T extends AbstractIdentifiableAdder<T>> implements Validable {

    protected final NetworkObjectIndex index;

    private String id;

    private boolean ensureIdUnicity = false;

    private String name;

    private boolean fictitious = false;

    private final String parentNetwork;

    AbstractIdentifiableAdder(NetworkObjectIndex index, String parentNetwork) {
        this.index = index;
        this.parentNetwork = parentNetwork;
    }

    protected NetworkImpl getNetwork() {
        return index.getNetwork();
    }

    protected NetworkObjectIndex getIndex() {
        return index;
    }

    protected abstract String getTypeDescription();

    protected String getId() {
        return id;
    }

    public T setId(String id) {
        this.id = id;
        return (T) this;
    }

    public T setEnsureIdUnicity(boolean ensureIdUnicity) {
        this.ensureIdUnicity = ensureIdUnicity;
        return (T) this;
    }

    protected String getName() {
        return name;
    }

    public T setName(String name) {
        this.name = name;
        return (T) this;
    }

    protected boolean isFictitious() {
        return fictitious;
    }

    public T setFictitious(boolean fictitious) {
        this.fictitious = fictitious;
        return (T) this;
    }

    protected String getParentNetwork() {
        return parentNetwork;
    }

    protected String checkAndGetUniqueId() {
        if (id == null) {
            throw new PowsyblException(getTypeDescription() + " id is not set");
        }
        String uniqueId;
        if (ensureIdUnicity) {
            uniqueId = Identifiables.getUniqueId(id, getNetwork().getIndex()::contains);
        } else {
            if (getNetwork().getIndex().contains(id)) {
                Identifiable<? extends Identifiable<?>> obj = getNetwork().getIndex().getIdentifiable(id);
                throw new PowsyblException("The network " + getNetwork().getId()
                        + " already contains an object '" + obj.getClass().getSimpleName()
                        + "' with the id '" + id + "'");
            }
            uniqueId = id;
        }
        return uniqueId;
    }

    protected String checkAndGetDefaultVoltageLevelId(String connectableBusId) {
        if (connectableBusId == null) {
            return null;
        }
        ConfiguredBusImpl bus = (ConfiguredBusImpl) getNetwork().getBusBreakerView().getBus(connectableBusId);
        if (bus == null) {
            throw new ValidationException(this, "configured bus '" + connectableBusId + "' not found");
        }
        return bus.getVoltageLevel().getId();
    }

    @Override
    public String getMessageHeader() {
        return getTypeDescription() + " '" + id + "': ";
    }

    protected static String computeParentNetwork(Network network, VoltageLevelImpl... voltageLevels) {
        if (voltageLevels.length == 0) {
            return network.getId();
        }
        // We support only one level of subnetworks.
        // Thus, if the subnetworkIds of all the voltageLevels are the same (and not null), the ref is the one of
        // the subnetwork. Else, it is the root network's one.
        String subnetworkId = voltageLevels[0].getResource().getParentNetwork();
        if (subnetworkId == null) {
            return network.getId();
        }
        boolean existDifferentSubnetworkId = Arrays.stream(voltageLevels, 1, voltageLevels.length)
                .map(vl -> vl.getResource().getParentNetwork())
                .anyMatch(Predicate.not(subnetworkId::equals));
        if (existDifferentSubnetworkId) {
            return network.getId();
        }
        return voltageLevels[0].getResource().getParentNetwork();
    }
}
