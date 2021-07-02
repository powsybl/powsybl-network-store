package com.powsybl.network.store.server;

import java.sql.Connection;
import java.sql.SQLException;

import com.powsybl.network.store.server.QueryBuilder.SimpleStatement;
import com.powsybl.network.store.server.QueryBuilder.OngoingStatement;

public class Session {
    Connection conn;

    public Session(Connection conn) {
        this.conn = conn;
    }

    public PreparedStatement prepare(SimpleStatement o) {
        try {
            String s = o.getQuery();
            return new PreparedStatement(conn.prepareStatement(s));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ResultSet execute(SimpleStatement o) {
        try {
            String s = o.getQuery();
            java.sql.PreparedStatement toto = conn.prepareStatement(s);
            int idx = 0;
            for (Object obj : o.values()) {
                toto.setObject(++idx, obj);
            }
            return new ResultSet(toto.executeQuery());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void execute(BatchStatement batch) {
        for (PreparedStatement statement : batch.preparedStatements) {
            // TODO, postgres ignores setAutocommit for batch statements (uses false)
            // but other vendors do not (mysql, oracle ?). Do we need to add
            // setAutocommit(false) and commit manually ?
            try {
                statement.statement.executeBatch();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        for (SimpleStatement statement : batch.statements2) {
            // TODO, postgres ignores setAutocommit for batch statements (uses false)
            // but other vendors do not (mysql, oracle ?). Do we need to add
            // setAutocommit(false) and commit manually ?
            try {
                ((PreparedStatement) statement).statement.executeBatch();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
