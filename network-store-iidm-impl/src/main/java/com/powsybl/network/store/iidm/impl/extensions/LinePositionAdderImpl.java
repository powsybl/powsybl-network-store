/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.LinePosition;
import com.powsybl.iidm.network.extensions.LinePositionAdder;
import com.powsybl.network.store.iidm.impl.AbstractIdentifiableImpl;
import com.powsybl.network.store.model.LinePositionAttributes;

import java.util.List;
import java.util.Objects;

/**
 * @author Seddik Yengui <seddik.yengui_externe at rte-france.com>
 */

public class LinePositionAdderImpl<T extends Identifiable<T>> extends AbstractExtensionAdder<T, LinePosition<T>> implements LinePositionAdder<T> {

    private List<Coordinate> coordinates;

    public LinePositionAdderImpl(T extendable) {
        super(extendable);
    }

    @Override
    protected LinePosition<T> createExtension(T extendable) {
        if (!(extendable instanceof Line) && !(extendable instanceof DanglingLine)) {
            throw new PowsyblException("Line position extension only supported for lines and dangling lines");
        }
        List<com.powsybl.network.store.model.Coordinate> coordinates = this.coordinates.stream()
                .map(coordinate -> new com.powsybl.network.store.model.Coordinate(coordinate.getLatitude(), coordinate.getLongitude()))
                .toList();
        var attributes = new LinePositionAttributes(coordinates);
        ((AbstractIdentifiableImpl<?, ?>) extendable).updateResource(res -> res.getAttributes().getExtensionAttributes().put(LinePosition.NAME, attributes));
        return new LinePositionImpl<>(extendable);
    }

    @Override
    public LinePositionAdder<T> withCoordinates(List<Coordinate> coordinates) {
        this.coordinates = Objects.requireNonNull(coordinates);
        return this;
    }
}
