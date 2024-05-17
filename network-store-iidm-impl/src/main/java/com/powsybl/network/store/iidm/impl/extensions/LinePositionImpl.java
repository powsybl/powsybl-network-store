/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.LinePosition;
import com.powsybl.network.store.iidm.impl.AbstractIdentifiableImpl;
import com.powsybl.network.store.model.LinePositionAttributes;

import java.util.List;

/**
 * @author Seddik Yengui <seddik.yengui_externe at rte-france.com>
 */

public class LinePositionImpl<T extends Identifiable<T>> extends AbstractExtension<T> implements LinePosition<T> {

    LinePositionImpl(T line) {
        super(line);
    }

    public LinePositionImpl(Line line) {
        this((T) line);
    }

    public LinePositionImpl(DanglingLine danglingLine) {
        this((T) danglingLine);
    }

    private AbstractIdentifiableImpl<?, ?> getIdentifiable() {
        return (AbstractIdentifiableImpl<?, ?>) getExtendable();
    }

    private LinePositionAttributes getLinePositionAttributes() {
        return (LinePositionAttributes) getIdentifiable().getResource().getAttributes().getExtensionAttributes().get(LinePosition.NAME);
    }

    @Override
    public List<Coordinate> getCoordinates() {
        return getLinePositionAttributes().getCoordinates()
                .stream()
                .map(coordinate -> new Coordinate(coordinate.getLatitude(), coordinate.getLongitude()))
                .toList();
    }
}
