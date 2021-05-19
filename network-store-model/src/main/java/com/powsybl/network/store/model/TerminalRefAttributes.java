/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.powsybl.commons.PowsyblException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Chamseddine BENHAMED <chamseddine.benhamed at rte-france.com>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel("TerminalRef attributes")
public class TerminalRefAttributes {

    @ApiModelProperty("connectableId")
    private String connectableId;

    @ApiModelProperty("side")
    private String side;

    // Constructor from string used by jackson key deserialization when deserializing Map<Object, ..> and the key is an object of type TerminalRefAttributes
    // The key is serialized with the toString() method autogenerated by lombok.
    // Example key: "TerminalRefAttributes(connectableId=_a5a962a6-2f47-4ef1-960f-e29131bcba36, side=1)"
    public TerminalRefAttributes(String key) {
        String[] values = key.split("\\(|,| |\\)|=");
        if (values.length != 6) {
            throw new PowsyblException("TerminalRefAttributes deserialization error: " + key + " is not a valid representation.");
        }
        if (!values[0].equals("TerminalRefAttributes")) {
            throw new PowsyblException("TerminalRefAttributes deserialization error: " + values[0] + " is not a valid object.");
        }
        this.connectableId = values[2];
        this.side = values[5];
    }
}
