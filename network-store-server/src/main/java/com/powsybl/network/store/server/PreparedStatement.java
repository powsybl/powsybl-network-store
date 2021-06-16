package com.powsybl.network.store.server;

import java.sql.SQLException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;

public class PreparedStatement {
    java.sql.PreparedStatement statement;

    public PreparedStatement(java.sql.PreparedStatement statement) {
        this.statement = statement;
    }

    public PreparedStatement bind(Object... values) {

        int idx = 0;
        for (Object o : values) {
            try {
                if (o instanceof Date) {
                    Date d = (Date) o;
                    statement.setObject(++idx, new java.sql.Date(d.getTime()));
                } else if (o == null || !Row.isCustomTypeJsonified(o.getClass())) {
                    statement.setObject(++idx, o);
                } else {
                    try {
                        statement.setObject(++idx, Row.mapper.writeValueAsString(o));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }
}
