/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.iidm.impl;

import com.powsybl.iidm.network.VariantManager;
import com.powsybl.iidm.network.VariantManagerConstants;

import java.util.Collection;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VariantManagerImpl implements VariantManager {

    @Override
    public Collection<String> getVariantIds() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public String getWorkingVariantId() {
        return VariantManagerConstants.INITIAL_VARIANT_ID;
    }

    @Override
    public void setWorkingVariant(String s) {
        // TODO
    }

    @Override
    public void cloneVariant(String s, List<String> list) {
        // TODO
    }

    @Override
    public void cloneVariant(String s, List<String> list, boolean b) {
        // TODO
    }

    @Override
    public void cloneVariant(String s, String s1) {
        // TODO
    }

    @Override
    public void cloneVariant(String s, String s1, boolean b) {
        // TODO
    }

    @Override
    public void removeVariant(String s) {
        // TODO
    }

    @Override
    public void allowVariantMultiThreadAccess(boolean b) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean isVariantMultiThreadAccessAllowed() {
        throw new UnsupportedOperationException("TODO");
    }
}
