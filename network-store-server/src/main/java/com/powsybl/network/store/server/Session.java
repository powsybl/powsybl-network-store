/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.network.store.server;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.Map.Entry;
import java.util.List;
import java.util.ArrayList;
import java.time.Instant;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.UncheckedIOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.commons.PowsyblException;
import com.powsybl.network.store.server.QueryBuilder.BoundStatement;
import com.powsybl.network.store.server.QueryBuilder.Select;
import com.powsybl.network.store.server.QueryBuilder.SimpleStatement;
import com.powsybl.network.store.server.exceptions.UncheckedSqlException;

public class Session {

    private static final Logger LOGGER = LoggerFactory.getLogger(Session.class);
    DataSource dataSource;

    public Session(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public PreparedStatement prepare(String s) {
        return new PreparedStatement(s);
    }

    public PreparedStatement prepare(SimpleStatement o) {
        String s = o.getQuery();
        return prepare(s);
    }

    public ResultSet execute(SimpleStatement o) {
        String s = o.getQuery();
        if (o instanceof Select) {
            java.sql.Connection conn = null;
            java.sql.PreparedStatement ps = null;
            //Don't use try-with-ressources because we return the
            //result set, so the caller is responsible for closing everything
            //unless we get an exception before returning, in which case we are responsible.
            try {
                conn = dataSource.getConnection();
                ps = conn.prepareStatement(s);
                bindValues(ps, o.values());
                return new ResultSet(conn, ps, ps.executeQuery());
            } catch (SQLException e) {
                if (ps != null) {
                    closeQuietly(ps, e);
                }
                if (conn != null) {
                    closeQuietly(conn, e);
                }
                throw new UncheckedSqlException(e);
            }
        } else {
            try (
                java.sql.Connection conn = dataSource.getConnection();
                java.sql.PreparedStatement ps = conn.prepareStatement(s);
            ) {
                bindValues(ps, o.values());
                ps.executeUpdate();
                return null;
            } catch (SQLException e) {
                throw new UncheckedSqlException(e);
            }
        }
    }

    private void cleanExecute(boolean mainThrows, java.sql.Connection conn, Collection<java.sql.PreparedStatement> statements) throws SQLException {
        // close all the statements and reset setAutoCommit even if one the first statements throws an exception.
        // Don't bother throwing if main already threw, instead log directly, because this is called in the finally block.
        // This avoids the difficulty of not shadowing any throwable coming from the main block.
        SQLException firstException = null;
        for (java.sql.PreparedStatement statement : statements) {
            try {
                statement.close();
            } catch (SQLException e) {
                if (firstException != null) {
                    firstException.addSuppressed(e);
                } else {
                    firstException = e;
                }
            }
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
        try (
            java.sql.Connection conn = dataSource.getConnection();
        ) {
            boolean mainThrows = true;
            List<java.sql.PreparedStatement> actualStatements = new ArrayList<>();
            try {
                conn.setAutoCommit(false);
                var grouped = batch.preparedStatements.stream().collect(Collectors.groupingBy(BoundStatement::getQuery));
                try {
                    for (Entry<String, List<BoundStatement>> entry : grouped.entrySet()) {
                        String query = entry.getKey();
                        //Don't use try-with-ressource as we have a dynamic number of statements
                        java.sql.PreparedStatement ps = conn.prepareStatement(query);
                        actualStatements.add(ps);
                        for (BoundStatement statement : entry.getValue()) {
                            bindValues(ps, statement.values());
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
                mainThrows = false;
            } finally {
                cleanExecute(mainThrows, conn, actualStatements);
            }
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

    // suppress additional exceptions and add to initial exception, or just log
    private static void closeQuietly(AutoCloseable autoCloseable, Exception firstException) {
        try {
            autoCloseable.close();
        } catch (Exception additionalException) {
            if (firstException != null) {
                firstException.addSuppressed(additionalException);
            } else {
                LOGGER.error("Additional error closing " + autoCloseable, additionalException);
            }
        }
    }

    private static void bindValues(java.sql.PreparedStatement statement, Object[] values) throws SQLException {
        int idx = 0;
        for (Object o : values) {
            if (o instanceof Instant) {
                Instant d = (Instant) o;
                statement.setObject(++idx, new java.sql.Date(d.toEpochMilli()));
            } else if (o == null || !Row.isCustomTypeJsonified(o.getClass())) {
                statement.setObject(++idx, o);
            } else {
                try {
                    statement.setObject(++idx, Row.mapper.writeValueAsString(o));
                } catch (JsonProcessingException e) {
                    throw new UncheckedIOException(e);
                }

            }
        }
    }
}
