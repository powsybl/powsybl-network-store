package com.powsybl.network.store.server;

import java.sql.SQLException;
import java.sql.Connection;
import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.powsybl.network.store.server.QueryBuilder.BoundStatement;

public class PreparedStatement implements BoundStatement {
    Connection conn;
    String query;
    ThreadLocal<java.sql.PreparedStatement> tlPs = new ThreadLocal<>();

    public PreparedStatement(String query, Connection conn) {
        this.query = query;
        this.conn = conn;
    }

    public PreparedStatement bind(Object... values) {
        int idx = 0;
        try {
            java.sql.PreparedStatement statement = tlPs.get();
            if (statement == null) {
                statement = conn.prepareStatement(query);
                tlPs.set(statement);
            }
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
                        throw new RuntimeException(e);
                    }

                }
            }
            // TODO this is a hack, we only use bind() for batches for now.
            // the new cassandra immutable API creates one BoundStatement per
            // query, and to avoid o(n^2) behavior in batchStatement.add(), we
            // put them all in a list and use batchStatement.addAll(). This
            // makes it hard to make an equivalent with a jdbc PreparedStatement,
            // where one statement holds all the data for the different queries
            statement.addBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    @Override
    public String getQuery() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<Object> values() {
        throw new RuntimeException("Not implemented");
    }

}
