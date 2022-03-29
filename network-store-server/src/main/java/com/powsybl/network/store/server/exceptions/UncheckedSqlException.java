/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.network.store.server.exceptions;

import java.sql.SQLException;

/**
 *
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class UncheckedSqlException extends RuntimeException {

    private static final long serialVersionUID = 7634995294859704597L;

    public UncheckedSqlException(SQLException cause) {
        super(cause);
    }

    @Override
    public synchronized SQLException getCause() {
        return (SQLException) super.getCause();
    }
}
