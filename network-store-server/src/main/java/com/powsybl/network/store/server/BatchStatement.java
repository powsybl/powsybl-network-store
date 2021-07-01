package com.powsybl.network.store.server;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.powsybl.network.store.server.QueryBuilder.BatchType;
import com.powsybl.network.store.server.QueryBuilder.BoundStatement;
import com.powsybl.network.store.server.QueryBuilder.OngoingStatement;
import com.powsybl.network.store.server.QueryBuilder.SimpleStatement;

public class BatchStatement {


    public BatchStatement() {

    }

    Set<PreparedStatement> preparedStatements = new HashSet<>();

    public void add(PreparedStatement ps) {
        try {
            ps.statement.addBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        preparedStatements.add(ps);
    }

    List<SimpleStatement> statements2 = new ArrayList<>();

    public BatchStatement add(SimpleStatement ps) {
        statements2.add(ps);
        return this;
    }

    public BatchStatement addAll(List<BoundStatement> ps) {
        statements2.addAll(ps);
        return this;
    }

    public static BatchStatement newInstance(BatchType unlogged) {
        return new BatchStatement();
    }
}
