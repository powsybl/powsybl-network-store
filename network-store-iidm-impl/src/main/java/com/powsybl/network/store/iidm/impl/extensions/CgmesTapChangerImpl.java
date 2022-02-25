/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl.extensions;

import com.powsybl.cgmes.extensions.CgmesTapChanger;
import com.powsybl.network.store.model.CgmesTapChangerAttributes;

import java.util.Objects;
import java.util.OptionalInt;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CgmesTapChangerImpl implements CgmesTapChanger {

    private final CgmesTapChangerAttributes attributes;

    public CgmesTapChangerImpl(CgmesTapChangerAttributes attributes) {
        this.attributes = Objects.requireNonNull(attributes);
    }

    CgmesTapChangerAttributes getAttributes() {
        return attributes;
    }

    @Override
    public String getId() {
        return attributes.getId();
    }

    @Override
    public String getCombinedTapChangerId() {
        return attributes.getCombinedTapChangerId();
    }

    @Override
    public String getType() {
        return attributes.getType();
    }

    @Override
    public boolean isHidden() {
        return attributes.isHidden();
    }

    @Override
    public OptionalInt getStep() {
        return attributes.getStep() != null ? OptionalInt.of(attributes.getStep()) : OptionalInt.empty();
    }

    @Override
    public String getControlId() {
        return attributes.getControlId();
    }
}
