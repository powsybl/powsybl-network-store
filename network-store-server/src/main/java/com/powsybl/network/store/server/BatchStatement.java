package com.powsybl.network.store.server;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.powsybl.network.store.server.QueryBuilder.BuiltStatement;

public class BatchStatement {
    public enum Type {
        UNLOGGED,
    }

    public BatchStatement() {

    }

    public BatchStatement(Type t) {

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

    List<BuiltStatement> statements2 = new ArrayList<>();

    public void add(BuiltStatement ps) {
        statements2.add(ps);
    }
}
