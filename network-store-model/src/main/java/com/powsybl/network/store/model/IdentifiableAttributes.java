/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface IdentifiableAttributes extends Attributes {

    String getName();

    void setName(String name);

    Map<String, String> getProperties();

    void setProperties(Map<String, String> properties);

    boolean isFictitious();

    void setFictitious(boolean fictitious);

    Set<String> getAliasesWithoutType();

    void setAliasesWithoutType(Set<String> aliasesWithoutType);

    Map<String, String> getAliasByType();

    void setAliasByType(Map<String, String> aliasByType);

    default <U extends SvAttributes> U toSv() {
        throw new UnsupportedOperationException("Not supported");
    }
}
