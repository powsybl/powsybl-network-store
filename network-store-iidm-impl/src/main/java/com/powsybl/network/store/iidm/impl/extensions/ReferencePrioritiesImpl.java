/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.ReferencePriorities;
import com.powsybl.iidm.network.extensions.ReferencePriority;
import com.powsybl.iidm.network.extensions.ReferencePriorityAdder;
import com.powsybl.network.store.iidm.impl.AbstractBranchImpl;
import com.powsybl.network.store.iidm.impl.AbstractIdentifiableImpl;
import com.powsybl.network.store.iidm.impl.AbstractInjectionImpl;
import com.powsybl.network.store.iidm.impl.BusbarSectionImpl;
import com.powsybl.network.store.iidm.impl.ThreeWindingsTransformerImpl;
import com.powsybl.network.store.model.ReferencePrioritiesAttributes;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class ReferencePrioritiesImpl<C extends Connectable<C>> extends AbstractExtension<C> implements ReferencePriorities<C> {

    public ReferencePrioritiesImpl(C extendable) {
        super(extendable);
    }

    private ReferencePrioritiesAttributes getAttributes() {
        if (getExtendable() instanceof BusbarSectionImpl) {
            return (ReferencePrioritiesAttributes) ((BusbarSectionImpl) getExtendable()).getResource().getAttributes().getExtensionAttributes().get(ReferencePriorities.NAME);
        } else if (getExtendable() instanceof AbstractInjectionImpl) {
            return (ReferencePrioritiesAttributes) ((AbstractInjectionImpl<?, ?>) getExtendable()).getResource().getAttributes().getExtensionAttributes().get(ReferencePriorities.NAME);
        } else if (getExtendable() instanceof AbstractBranchImpl) {
            return (ReferencePrioritiesAttributes) ((AbstractBranchImpl<?, ?>) getExtendable()).getResource().getAttributes().getExtensionAttributes().get(ReferencePriorities.NAME);
        } else if (getExtendable() instanceof ThreeWindingsTransformerImpl) {
            return (ReferencePrioritiesAttributes) ((ThreeWindingsTransformerImpl) getExtendable()).getResource().getAttributes().getExtensionAttributes().get(ReferencePriorities.NAME);
        }
        return null;
    }

    @Override
    public ReferencePriorityAdder newReferencePriority() {
        return new ReferencePriorityAdderImpl(this);
    }

    @Override
    public List<ReferencePriority> getReferencePriorities() {
        return getAttributes().getReferencePriorities()
                .stream()
                .map(attributes -> (ReferencePriority) new ReferencePriorityImpl(attributes, ((AbstractIdentifiableImpl<?, ?>) getExtendable()).getIndex()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteReferencePriorities() {
        getAttributes().getReferencePriorities().clear();
    }

    void putReferencePriority(ReferencePriorityImpl referencePriority) {
        getAttributes().putReferencePriority(referencePriority.getAttributes());
    }
}
