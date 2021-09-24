package com.powsybl.network.store.server;

import java.sql.Connection;
import java.sql.SQLException;

import com.powsybl.network.store.server.QueryBuilder.SimpleStatement;
import com.powsybl.network.store.server.QueryBuilder.BoundStatement;
import com.powsybl.network.store.server.QueryBuilder.Select;

public class Session {
    Connection conn;

    public Session(Connection conn) {
        this.conn = conn;
    }

    public PreparedStatement prepare(SimpleStatement o) {
        String s = o.getQuery();
        return new PreparedStatement(s, conn);
    }

    public ResultSet execute(SimpleStatement o) {
        try {
            String s = o.getQuery();
            java.sql.PreparedStatement toto = conn.prepareStatement(s);
            int idx = 0;
            for (Object obj : o.values()) {
                toto.setObject(++idx, obj);
            }
            if (o instanceof Select) {
                return new ResultSet(toto, toto.executeQuery());
            } else {
                toto.executeUpdate();
                toto.close();
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void execute(BatchStatement batch) {
        for (BoundStatement statement : batch.preparedStatements) {
            // TODO, postgres ignores setAutocommit for batch statements (uses false)
            // but other vendors do not (mysql, oracle ?). Do we need to add
            // setAutocommit(false) and commit manually ?
            try {
                ((PreparedStatement) statement).tlPs.get().executeBatch();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                ((PreparedStatement) statement).tlPs.get().close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                ((PreparedStatement) statement).tlPs.remove();
            }
        }
        for (SimpleStatement statement : batch.statements2) {
            execute(statement);
        }
    }
}
