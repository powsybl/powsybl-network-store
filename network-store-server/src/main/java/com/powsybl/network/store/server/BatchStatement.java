/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.powsybl.network.store.server.QueryBuilder.BatchType;
import com.powsybl.network.store.server.QueryBuilder.BoundStatement;
import com.powsybl.network.store.server.QueryBuilder.SimpleStatement;

public class BatchStatement {

    //for BatchStatements with preparedStatement.bind() (right now: insert, updates)
    Set<BoundStatement> preparedStatements = new HashSet<>();

    public BatchStatement addAll(List<BoundStatement> ps) {
        preparedStatements.addAll(ps);
        return this;
    }

    List<SimpleStatement> statements2 = new ArrayList<>();

    // for BatchStatements with inline statements (right now, delete network only)
    public BatchStatement add(SimpleStatement ps) {
        statements2.add(ps);
        return this;
    }

    public static BatchStatement newInstance(BatchType unlogged) {
        return new BatchStatement();
    }
}
