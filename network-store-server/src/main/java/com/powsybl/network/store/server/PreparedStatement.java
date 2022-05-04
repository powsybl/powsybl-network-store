/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.server;

import com.powsybl.network.store.server.QueryBuilder.BoundStatement;

public class PreparedStatement implements BoundStatement {
    String query;
    Object[] values;

    public PreparedStatement(String query) {
        this.query = query;
    }

    public PreparedStatement bind(Object... values) {
        PreparedStatement ps = new PreparedStatement(this.query);
        ps.values = values;
        return ps;
    }

    @Override
    public String getQuery() {
        return query;
    }

    @Override
    public Object[] values() {
        return values;
    }

}
