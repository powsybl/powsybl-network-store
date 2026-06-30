/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.network.store.model.*;

import java.util.Objects;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */

public class TieLineAdderImpl extends AbstractIdentifiableAdder<TieLineAdderImpl> implements TieLineAdder {

    private String boundaryLine1;
    private String boundaryLine2;

    public TieLineAdderImpl(NetworkObjectIndex index) {
        super(index);
    }

    @Override
    public TieLineAdder setBoundaryLine1(String boundaryLine1) {
        this.boundaryLine1 = boundaryLine1;
        return this;
    }

    @Override
    public TieLineAdder setBoundaryLine2(String boundaryLine2) {
        this.boundaryLine2 = boundaryLine2;
        return this;
    }

    @Override
    public TieLine add() {
        String id = checkAndGetUniqueId();
        if (boundaryLine1 == null || boundaryLine2 == null) {
            throw new ValidationException(this, "undefined boundary line");
        }

        BoundaryLineImpl dl1 = index.getBoundaryLine(boundaryLine1).orElseThrow(() -> new ValidationException(this, boundaryLine1 + " are not boundary lines in the network"));
        BoundaryLineImpl dl2 = index.getBoundaryLine(boundaryLine2).orElseThrow(() -> new ValidationException(this, boundaryLine2 + " are not boundary lines in the network"));

        if (dl1 == dl2) {
            throw new ValidationException(this, "boundaryLine1 and boundaryLine2 are identical (" + dl1.getId() + ")");
        }
        if (dl1.getTieLine().isPresent() || dl2.getTieLine().isPresent()) {
            throw new ValidationException(this, "boundaryLine1 (" + boundaryLine1 + ") and/or boundaryLine2 (" + boundaryLine2 + ") already has a tie line");
        }
        if (dl1.getPairingKey() != null && dl2.getPairingKey() != null && !Objects.equals(dl1.getPairingKey(), dl2.getPairingKey())) {
            throw new ValidationException(this, "pairingKey is not consistent");
        }

        Resource<TieLineAttributes> resource = Resource.tieLineBuilder()
                .id(id)
                .variantNum(index.getWorkingVariantNum())
                .attributes(TieLineAttributes.builder()
                        .boundaryLine1Id(dl1.getId())
                        .boundaryLine2Id(dl2.getId())
                        .build()).build();
        getIndex().createTieLine(resource);
        TieLineImpl tieLine = new TieLineImpl(getIndex(), resource);
        dl1.setTieLine(tieLine);
        dl2.setTieLine(tieLine);
        return tieLine;
    }

    @Override
    protected String getTypeDescription() {
        return ResourceType.TIE_LINE.getDescription();
    }
}
