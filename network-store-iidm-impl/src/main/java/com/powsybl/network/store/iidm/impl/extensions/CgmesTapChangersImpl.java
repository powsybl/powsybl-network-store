/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.extensions.CgmesTapChanger;
import com.powsybl.cgmes.extensions.CgmesTapChangerAdder;
import com.powsybl.cgmes.extensions.CgmesTapChangers;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.network.store.iidm.impl.AbstractIdentifiableImpl;
import com.powsybl.network.store.model.TransformerAttributes;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CgmesTapChangersImpl<C extends Connectable<C>> extends AbstractExtension<C> implements CgmesTapChangers<C> {

    public CgmesTapChangersImpl(C transformer) {
        super(transformer);
    }

    private TransformerAttributes getAttributes() {
        return (TransformerAttributes) ((AbstractIdentifiableImpl<?, ?>) getExtendable()).checkResource().getAttributes();
    }

    @Override
    public Set<CgmesTapChanger> getTapChangers() {
        return getAttributes().getCgmesTapChangerAttributesList()
                .stream()
                .map(attributes -> (CgmesTapChanger) new CgmesTapChangerImpl(attributes))
                .sorted(Comparator.comparing(CgmesTapChanger::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public CgmesTapChanger getTapChanger(String id) {
        Objects.requireNonNull(id);
        return getTapChangers()
                .stream()
                .filter(tc -> tc.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new PowsyblException("CGMES tap change '" + id + "' not found"));
    }

    @Override
    public CgmesTapChangerAdder newTapChanger() {
        return new CgmesTapChangerAdderImpl(this);
    }

    void putTapChanger(CgmesTapChangerImpl tapChanger) {
        if (getAttributes().getCgmesTapChangerAttributesList().stream().anyMatch(attribute -> attribute.getId().equals(tapChanger.getId()))) {
            throw new PowsyblException(String.format("Tap changer %s has already been added", tapChanger.getId()));
        }
        getAttributes().getCgmesTapChangerAttributesList().add(tapChanger.getAttributes());
    }
}
