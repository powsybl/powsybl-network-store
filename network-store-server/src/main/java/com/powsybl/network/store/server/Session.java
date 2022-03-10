package com.powsybl.network.store.server;

import java.util.UUID;
import java.sql.Connection;
import java.sql.SQLException;

import com.powsybl.network.store.server.QueryBuilder.SimpleStatement;
import com.powsybl.network.store.server.QueryBuilder.BoundStatement;
import com.powsybl.network.store.server.QueryBuilder.Select;

public class Session {
    DatabaseAdapterService databaseAdapterService;
    Connection conn;

    public Session(DatabaseAdapterService databaseAdapterService, Connection conn) {
        this.databaseAdapterService = databaseAdapterService;
        this.conn = conn;
    }

    public PreparedStatement prepare(SimpleStatement o) {
        String s = o.getQuery();
        return new PreparedStatement(databaseAdapterService, s, conn);
    }

    public ResultSet execute(SimpleStatement o) {
        try {
            String s = o.getQuery();
            java.sql.PreparedStatement ps = conn.prepareStatement(s);
            int idx = 0;
            for (Object obj : o.values()) {
                if (obj instanceof UUID) {
                    ps.setObject(++idx, databaseAdapterService.adaptUUID((UUID) obj));
                } else if (obj instanceof Double && Double.isNaN((Double) obj)) {
                    ps.setObject(++idx, null);
                } else if (obj instanceof Float && Float.isNaN((Float) obj)) {
                    ps.setObject(++idx, null);
                } else {
                    ps.setObject(++idx, obj);
                }
            }
            if (o instanceof Select) {
                return new ResultSet(ps, ps.executeQuery());
            } else {
                ps.executeUpdate();
                ps.close();
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
                conn.setAutoCommit(false);
                ((PreparedStatement) statement).tlPs.get().executeBatch();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    conn.setAutoCommit(true);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
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
