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

    private String danglingLine1;
    private String danglingLine2;

    public TieLineAdderImpl(NetworkObjectIndex index) {
        this(index, index.getNetwork().getId());
    }

    public TieLineAdderImpl(NetworkObjectIndex index, String parentNetwork) {
        super(index, parentNetwork);
    }

    @Override
    public TieLineAdder setDanglingLine1(String danglingLine1) {
        this.danglingLine1 = danglingLine1;
        return this;
    }

    @Override
    public TieLineAdder setDanglingLine2(String danglingLine2) {
        this.danglingLine2 = danglingLine2;
        return this;
    }

    @Override
    public TieLine add() {
        String id = checkAndGetUniqueId();
        if (danglingLine1 == null || danglingLine2 == null) {
            throw new ValidationException(this, "undefined dangling line");
        }

        DanglingLineImpl dl1 = index.getDanglingLine(danglingLine1).orElseThrow(() -> new ValidationException(this, danglingLine1 + " are not dangling lines in the network"));
        DanglingLineImpl dl2 = index.getDanglingLine(danglingLine2).orElseThrow(() -> new ValidationException(this, danglingLine2 + " are not dangling lines in the network"));

        if (dl1 == dl2) {
            throw new ValidationException(this, "danglingLine1 and danglingLine2 are identical (" + dl1.getId() + ")");
        }
        if (dl1.getTieLine().isPresent() || dl2.getTieLine().isPresent()) {
            throw new ValidationException(this, "danglingLine1 (" + danglingLine1 + ") and/or danglingLine2 (" + danglingLine2 + ") already has a tie line");
        }
        if (dl1.getPairingKey() != null && dl2.getPairingKey() != null && !Objects.equals(dl1.getPairingKey(), dl2.getPairingKey())) {
            throw new ValidationException(this, "pairingKey is not consistent");
        }

        Resource<TieLineAttributes> resource = Resource.tieLineBuilder()
                .id(id)
                .variantNum(index.getWorkingVariantNum())
                .parentNetwork(getParentNetwork())
                .attributes(TieLineAttributes.builder()
                        .danglingLine1Id(dl1.getId())
                        .danglingLine2Id(dl2.getId())
                        .name(getName())
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
