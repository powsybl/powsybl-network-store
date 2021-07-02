package com.powsybl.network.store.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.powsybl.network.store.server.QueryBuilder.BatchType;
import com.powsybl.network.store.server.QueryBuilder.BoundStatement;
import com.powsybl.network.store.server.QueryBuilder.SimpleStatement;

public class BatchStatement {

    public BatchStatement() {

    }

    Set<BoundStatement> preparedStatements = new HashSet<>();

    public BatchStatement addAll(List<BoundStatement> ps) {
        preparedStatements.addAll(ps);
        return this;
    }

    List<SimpleStatement> statements2 = new ArrayList<>();

    public BatchStatement add(SimpleStatement ps) {
        statements2.add(ps);
        return this;
    }

    public static BatchStatement newInstance(BatchType unlogged) {
        return new BatchStatement();
    }
}
