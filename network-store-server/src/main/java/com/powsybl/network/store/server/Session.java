/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.commons.PowsyblException;
import com.powsybl.network.store.server.QueryBuilder.BoundStatement;
import com.powsybl.network.store.server.QueryBuilder.Select;
import com.powsybl.network.store.server.QueryBuilder.SimpleStatement;

public class Session {

    private static final Logger LOGGER = LoggerFactory.getLogger(Session.class);
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
            java.sql.PreparedStatement ps = conn.prepareStatement(s);
            int idx = 0;
            for (Object obj : o.values()) {
                ps.setObject(++idx, obj);
            }
            if (o instanceof Select) {
                return new ResultSet(ps, ps.executeQuery());
            } else {
                ps.executeUpdate();
                ps.close();
                return null;
            }
        } catch (SQLException e) {
            throw new PowsyblException(e);
        }
    }

    private void cleanExecute(boolean mainThrows, Collection<BoundStatement> statements) throws SQLException {
        // close all the statements and reset setAutoCommit even if one the first statements throws an exception.
        // Don't bother throwing if main already threw, instead log directly, because this is called in the finally block.
        // This avoids the difficulty of not shadowing any throwable coming from the main block.
        SQLException firstException = null;
        for (BoundStatement statement : statements) {
            try {
                ((PreparedStatement) statement).tlPs.get().close();
            } catch (SQLException e) {
                if (firstException != null) {
                    firstException.addSuppressed(e);
                } else {
                    firstException = e;
                }
            }
            ((PreparedStatement) statement).tlPs.remove();
        }
        try {
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            if (firstException != null) {
                firstException.addSuppressed(e);
            } else {
                firstException = e;
            }
        }
        if (firstException != null) {
            if (mainThrows) {
                LOGGER.error("Additional error closing SQL batch statement", firstException);
            } else {
                throw firstException;
            }
        }
    }

    private void doExecute(BatchStatement batch) throws SQLException {
        boolean mainThrows = true;
        try {
            conn.setAutoCommit(false);
            for (BoundStatement statement : batch.preparedStatements) {
                ((PreparedStatement) statement).tlPs.get().executeBatch();
            }
            conn.commit();
            mainThrows = false;
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            cleanExecute(mainThrows, batch.preparedStatements);
        }
        for (SimpleStatement statement : batch.statements2) {
            execute(statement);
        }
    }

    public void execute(BatchStatement batch) {
        try {
            doExecute(batch);
        } catch (SQLException e) {
            throw new PowsyblException(e);
        }
    }

}
